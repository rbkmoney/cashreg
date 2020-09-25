package com.rbkmoney.cashreg.service.management.aggregate;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.cashreg.service.management.model.ExtraField;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.cashreg.creators.CashRegProviderCreators;
import com.rbkmoney.damsel.cashreg.domain.AccountInfo;
import com.rbkmoney.damsel.cashreg.processing.CashRegisterProvider;
import com.rbkmoney.damsel.cashreg.processing.*;
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
        log.info("toCashRegCreatedChange params {}", params);

        Long domainRevision = null;
        Long partyRevision = partyManagementService.getPartyRevision(params.getPartyId());
        Shop shop = partyManagementService.getShop(params.getPartyId(), params.getShopId(), partyRevision);

        // TODO: select provider, but now get first in list
        CashRegisterProvider cashRegisterProvider = params.getProviders().get(0);
        log.info("toCashRegCreatedChange partyRevision {}, shop {}, cashRegisterProvider {}", partyRevision, shop, cashRegisterProvider);

        ResponseDominantWrapper<CashRegisterProviderObject> providerObject = dominantService.getCashRegisterProviderObject(
                CashRegProviderCreators.createCashregProviderRef(cashRegisterProvider.getProviderId()),
                domainRevision
        );

        domainRevision = providerObject.getRevisionVersion();
        Map<String, String> aggregateOptions = aggregateOptions(providerObject, cashRegisterProvider);
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId(), partyRevision);

        AccountInfo accountInfo = new AccountInfo()
                .setLegalEntity(prepareLegalEntity(contract, aggregateOptions));

        Receipt receipt = new Receipt()
                .setCashregProvider(cashRegisterProvider)
                .setReceiptId(params.getReceiptId())
                .setPaymentInfo(params.getPaymentInfo())
                .setType(params.getType())
                .setShopId(params.getShopId())
                .setPartyId(params.getPartyId())
                .setStatus(Status.pending(new Pending()))
                .setAccountInfo(accountInfo)
                .setDomainRevision(domainRevision)
                .setPartyRevision(partyRevision);

        created.setReceipt(receipt);
        return Change.created(created);
    }

    private Map<String, String> aggregateOptions(ResponseDominantWrapper<CashRegisterProviderObject> wrapperProviderObject, CashRegisterProvider cashRegisterProvider) {
        Proxy proxy = wrapperProviderObject.getResponse().getData().getProxy();
        ResponseDominantWrapper<ProxyObject> wrapperProxyObject = dominantService.getProxyObject(proxy.getRef(), wrapperProviderObject.getRevisionVersion());
        Map<String, String> proxyOptions = wrapperProxyObject.getResponse().getData().getOptions();
        proxyOptions.putAll(proxy.getAdditional());
        if(cashRegisterProvider != null) {
            proxyOptions.putAll(cashRegisterProvider.getProviderParams());
        }
        return proxyOptions;
    }

    public Map<String, String> aggregateOptions(com.rbkmoney.damsel.domain.CashRegisterProviderRef providerRef, Long domainRevision) {
        ResponseDominantWrapper<CashRegisterProviderObject> wrapperProviderObject = dominantService.getCashRegisterProviderObject(providerRef, domainRevision);
        return aggregateOptions(wrapperProviderObject, null);
    }

    private com.rbkmoney.damsel.cashreg.domain.LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        com.rbkmoney.damsel.domain.RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg.domain.RussianLegalEntity()
                .setEmail(proxyOptions.get(ExtraField.RUSSIAN_LEGAL_ENTITY_EMAIL.getField()))
                .setActualAddress(russianLegalEntityDomain.getActualAddress())
                .setInn(russianLegalEntityDomain.getInn())
                .setRegisteredNumber(russianLegalEntityDomain.getRegisteredNumber())
                .setPostAddress(russianLegalEntityDomain.getPostAddress())
                .setRegisteredName(russianLegalEntityDomain.getRegisteredName())
                .setRepresentativeDocument(russianLegalEntityDomain.getRepresentativeDocument())
                .setRepresentativeFullName(russianLegalEntityDomain.getRepresentativeFullName())
                .setRepresentativePosition(russianLegalEntityDomain.getRepresentativePosition());

        RussianBankAccount russianBankAccountIncome = russianLegalEntityDomain.getRussianBankAccount();
        com.rbkmoney.damsel.cashreg.domain.RussianBankAccount russianBankAccount = new com.rbkmoney.damsel.cashreg.domain.RussianBankAccount()
                .setAccount(russianBankAccountIncome.getAccount())
                .setBankBik(russianBankAccountIncome.getBankBik())
                .setBankName(russianBankAccountIncome.getBankName())
                .setBankPostAccount(russianBankAccountIncome.getBankPostAccount());

        russianLegalEntity.setRussianBankAccount(russianBankAccount);
        russianLegalEntity.setTaxMode(extractTaxModeFromOptions(proxyOptions));

        com.rbkmoney.damsel.cashreg.domain.LegalEntity legalEntity = new com.rbkmoney.damsel.cashreg.domain.LegalEntity();
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
