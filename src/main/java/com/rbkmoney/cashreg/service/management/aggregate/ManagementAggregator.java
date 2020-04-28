package com.rbkmoney.cashreg.service.management.aggregate;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.cashreg.service.management.model.ExtraField;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.cashreg.creators.CashRegProviderCreators;
import com.rbkmoney.damsel.cashreg.domain.AccountInfo;
import com.rbkmoney.damsel.cashreg.processing.CashRegisterProvider;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.CreatedChange;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
import com.rbkmoney.damsel.cashreg.processing.ReceiptParams;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import com.rbkmoney.damsel.domain.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.cashreg.utils.cashreg.extractors.TaxModeExtractor.extractTaxModeFromOptions;

@Slf4j
@Component
@RequiredArgsConstructor
public class ManagementAggregator {

    private final PartyManagementService partyManagementService;
    private final DominantService dominantService;

    public Change toCashRegCreatedChange(ReceiptParams params) {
        CreatedChange created = new CreatedChange();
        Receipt receipt = new Receipt();

        // TODO: select provider, but now get first in list
        CashRegisterProvider cashRegisterProvider = params.getProviders().get(0);


        receipt.setCashregProvider(cashRegisterProvider);
        receipt.setReceiptId(params.getReceiptId());
        receipt.setPaymentInfo(params.getPaymentInfo());
        receipt.setType(params.getType());
        receipt.setShopId(params.getShopId());
        receipt.setPartyId(params.getPartyId());
        receipt.setStatus(Status.pending(new Pending()));

        Long partyRevision = partyManagementService.getPartyRevision(params.getPartyId());
        Long domainRevision = null;

        Shop shop = partyManagementService.getShop(params.getShopId(), params.getPartyId(), partyRevision);

        ResponseDominantWrapper<CashRegisterProviderObject> providerObject = dominantService.getCashRegisterProviderObject(
                CashRegProviderCreators.createCashregProviderRef(cashRegisterProvider.getProviderId()),
                domainRevision
        );
        domainRevision = providerObject.getRevisionVersion();

        Map<String, String> aggregateOptions = aggregateOptions(providerObject);
        AccountInfo accountInfo = new AccountInfo();
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId(), partyRevision);
        accountInfo.setLegalEntity(prepareLegalEntity(contract, aggregateOptions));
        receipt.setAccountInfo(accountInfo);
        receipt.setDomainRevision(domainRevision);
        receipt.setPartyRevision(partyRevision);

        created.setReceipt(receipt);
        return Change.created(created);
    }

    private Map<String, String> aggregateOptions(ResponseDominantWrapper<CashRegisterProviderObject> wrapperProviderObject) {
        Proxy proxy = wrapperProviderObject.getResponse().getData().getProxy();
        ResponseDominantWrapper<ProxyObject> wrapperProxyObject = dominantService.getProxyObject(proxy.getRef(), wrapperProviderObject.getRevisionVersion());
        Map<String, String> proxyOptions = wrapperProxyObject.getResponse().getData().getOptions();
        proxyOptions.putAll(proxy.getAdditional());
        return proxyOptions;
    }

    public Map<String, String> aggregateOptions(com.rbkmoney.damsel.domain.CashRegisterProviderRef providerRef, Long domainRevision) {
        ResponseDominantWrapper<CashRegisterProviderObject> wrapperProviderObject = dominantService.getCashRegisterProviderObject(providerRef, domainRevision);
        return aggregateOptions(wrapperProviderObject);
    }

    private com.rbkmoney.damsel.cashreg.domain.LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        com.rbkmoney.damsel.domain.RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        com.rbkmoney.damsel.cashreg.domain.LegalEntity legalEntity = new com.rbkmoney.damsel.cashreg.domain.LegalEntity();
        com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity();

        russianLegalEntity.setEmail(proxyOptions.get(ExtraField.RUSSIAN_LEGAL_ENTITY_EMAIL.getField()));
        russianLegalEntity.setActualAddress(russianLegalEntityDomain.getActualAddress());
        russianLegalEntity.setInn(russianLegalEntityDomain.getInn());
        russianLegalEntity.setRegisteredNumber(russianLegalEntityDomain.getRegisteredNumber());
        russianLegalEntity.setPostAddress(russianLegalEntityDomain.getPostAddress());
        russianLegalEntity.setRegisteredName(russianLegalEntityDomain.getRegisteredName());
        russianLegalEntity.setRepresentativeDocument(russianLegalEntityDomain.getRepresentativeDocument());
        russianLegalEntity.setRepresentativeFullName(russianLegalEntityDomain.getRepresentativeFullName());
        russianLegalEntity.setRepresentativePosition(russianLegalEntityDomain.getRepresentativePosition());

        com.rbkmoney.damsel.cashreg.domain.RussianBankAccount russianBankAccount = new com.rbkmoney.damsel.cashreg.domain.RussianBankAccount();
        RussianBankAccount russianBankAccountIncome = russianLegalEntityDomain.getRussianBankAccount();
        russianBankAccount.setAccount(russianBankAccountIncome.getAccount());
        russianBankAccount.setBankBik(russianBankAccountIncome.getBankBik());
        russianBankAccount.setBankName(russianBankAccountIncome.getBankName());
        russianBankAccount.setBankPostAccount(russianBankAccountIncome.getBankPostAccount());
        russianLegalEntity.setRussianBankAccount(russianBankAccount);
        russianLegalEntity.setTaxMode(extractTaxModeFromOptions(proxyOptions));

        legalEntity.setRussianLegalEntity(russianLegalEntity);
        return legalEntity;
    }

    public ProxyObject extractProxyObject(Receipt receipt) {
        ResponseDominantWrapper<CashRegisterProviderObject> wrapperProviderObject = dominantService.getCashRegisterProviderObject(
                CashRegProviderCreators.createCashregProviderRef(receipt.getCashregProvider().getProviderId()),
                receipt.getDomainRevision()
        );
        return extractProxyObject(wrapperProviderObject.getResponse().getData().getProxy().getRef(), wrapperProviderObject.getRevisionVersion());
    }

    private ProxyObject extractProxyObject(ProxyRef proxyRef, Long revisionVersion) {
        ResponseDominantWrapper<ProxyObject> object = dominantService.getProxyObject(proxyRef, revisionVersion);
        if (!object.getResponse().isSetData()) {
            throw new IllegalStateException("ProxyObject not found; proxyRef: " + proxyRef);
        }
        return object.getResponse();
    }

}
