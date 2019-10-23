package com.rbkmoney.cashreg.service.management.aggregate;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.cashreg.service.management.model.ExtraField;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.cashreg.creators.CashRegProviderCreators;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_domain.AccountInfo;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.CreatedChange;
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

    public Change toCashRegCreatedChange(CashRegParams params) {
        CreatedChange created = new CreatedChange();
        CashReg cashReg = new CashReg();
        cashReg.setCashregProviderId(params.getCashregProviderId());
        cashReg.setCashregId(params.getCashregId());
        cashReg.setPaymentInfo(params.getPaymentInfo());
        cashReg.setType(params.getType());
        cashReg.setShopId(params.getShopId());
        cashReg.setPartyId(params.getPartyId());
        cashReg.setStatus(Status.pending(new Pending()));

        Long partyRevision = partyManagementService.getPartyRevision(params.getPartyId());
        Long domainRevision = null;

        Shop shop = partyManagementService.getShop(params.getShopId(), params.getPartyId(), partyRevision);

        ResponseDominantWrapper<CashRegProviderObject> providerObject = dominantService.getCashRegProviderObject(
                CashRegProviderCreators.createCashregProviderRef(cashReg.getCashregProviderId()),
                domainRevision
        );
        domainRevision = providerObject.getRevisionVersion();

        Map<String, String> aggregateOptions = aggregateOptions(providerObject);
        AccountInfo accountInfo = new AccountInfo();
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId(), partyRevision);
        accountInfo.setLegalEntity(prepareLegalEntity(contract, aggregateOptions));
        cashReg.setAccountInfo(accountInfo);
        cashReg.setDomainRevision(domainRevision);
        cashReg.setPartyRevision(partyRevision);

        created.setCashreg(cashReg);
        return Change.created(created);
    }

    private Map<String, String> aggregateOptions(ResponseDominantWrapper<CashRegProviderObject> wrapperProviderObject) {
        Proxy proxy = wrapperProviderObject.getResponse().getData().getProxy();
        ResponseDominantWrapper<ProxyObject> wrapperProxyObject = dominantService.getProxyObject(proxy.getRef(), wrapperProviderObject.getRevisionVersion());
        Map<String, String> proxyOptions = wrapperProxyObject.getResponse().getData().getOptions();
        proxyOptions.putAll(proxy.getAdditional());
        return proxyOptions;
    }

    public Map<String, String> aggregateOptions(com.rbkmoney.damsel.domain.CashRegProviderRef providerRef, Long domainRevision) {
        ResponseDominantWrapper<CashRegProviderObject> wrapperProviderObject = dominantService.getCashRegProviderObject(providerRef, domainRevision);
        return aggregateOptions(wrapperProviderObject);
    }

    private com.rbkmoney.damsel.cashreg_domain.LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        com.rbkmoney.damsel.domain.RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        com.rbkmoney.damsel.cashreg_domain.LegalEntity legalEntity = new com.rbkmoney.damsel.cashreg_domain.LegalEntity();
        com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity();

        russianLegalEntity.setEmail(proxyOptions.get(ExtraField.RUSSIAN_LEGAL_ENTITY_EMAIL.getField()));
        russianLegalEntity.setActualAddress(russianLegalEntityDomain.getActualAddress());
        russianLegalEntity.setInn(russianLegalEntityDomain.getInn());
        russianLegalEntity.setRegisteredNumber(russianLegalEntityDomain.getRegisteredNumber());
        russianLegalEntity.setPostAddress(russianLegalEntityDomain.getPostAddress());
        russianLegalEntity.setRegisteredName(russianLegalEntityDomain.getRegisteredName());
        russianLegalEntity.setRepresentativeDocument(russianLegalEntityDomain.getRepresentativeDocument());
        russianLegalEntity.setRepresentativeFullName(russianLegalEntityDomain.getRepresentativeFullName());
        russianLegalEntity.setRepresentativePosition(russianLegalEntityDomain.getRepresentativePosition());

        com.rbkmoney.damsel.cashreg_domain.RussianBankAccount russianBankAccount = new com.rbkmoney.damsel.cashreg_domain.RussianBankAccount();
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

    public ProxyObject extractProxyObject(CashReg cashReg) {
        ResponseDominantWrapper<CashRegProviderObject> wrapperProviderObject = dominantService.getCashRegProviderObject(
                CashRegProviderCreators.createCashregProviderRef(cashReg.getCashregProviderId()),
                cashReg.getDomainRevision()
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
