package com.rbkmoney.cashreg.service.management.aggregate;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_domain.AccountInfo;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.CreatedChange;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.VersionedObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

import static com.rbkmoney.cashreg.service.management.model.ExtraField.RUSSIAN_LEGAL_ENTITY_EMAIL;
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
        cashReg.setId(params.getId());
        cashReg.setPaymentInfo(params.getPaymentInfo());
        cashReg.setType(params.getType());
        cashReg.setShopId(params.getShopId());
        cashReg.setPartyId(params.getPartyId());
        cashReg.setStatus(Status.pending(new Pending()));

        Shop shop = partyManagementService.getShop(params.getShopId(), params.getPartyId());
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(params.getPartyId(), shop.getContractId());

        VersionedObject versionedObject = dominantService.getVersionedObjectFromPaymentInstitution(paymentInstitutionRef);
        Map<String, String> aggregateOptions = aggregateOptions(versionedObject);

        AccountInfo accountInfo = new AccountInfo();
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId());
        accountInfo.setLegalEntity(prepareLegalEntity(contract, aggregateOptions));
        cashReg.setAccountInfo(accountInfo);

        created.setCashreg(cashReg);
        return Change.created(created);
    }

    private Map<String, String> aggregateOptions(VersionedObject versionedObject) {
        ProxyObject proxyObject = extractProxyObject(versionedObject);
        ProviderObject providerObject = extractProviderObject(versionedObject);
        TerminalObject terminalObject = extractTerminalObject(versionedObject);

        Map<String, String> proxyOptions = proxyObject.getData().getOptions();
        proxyOptions.putAll(providerObject.getData().getProxy().getAdditional());
        proxyOptions.putAll(terminalObject.getData().getOptions());
        return proxyOptions;
    }

    public Map<String, String> aggregateOptions(String shopId, String partyId) {
        Shop shop = partyManagementService.getShop(shopId, partyId);
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(partyId, shop.getContractId());

        VersionedObject versionedObject = dominantService.getVersionedObjectFromPaymentInstitution(paymentInstitutionRef);
        return aggregateOptions(versionedObject);
    }

    private com.rbkmoney.damsel.cashreg_domain.LegalEntity prepareLegalEntity(Contract contract, Map<String, String> proxyOptions) {
        com.rbkmoney.damsel.domain.RussianLegalEntity russianLegalEntityDomain = contract.getContractor().getLegalEntity().getRussianLegalEntity();

        com.rbkmoney.damsel.cashreg_domain.LegalEntity legalEntity = new com.rbkmoney.damsel.cashreg_domain.LegalEntity();
        com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity russianLegalEntity = new com.rbkmoney.damsel.cashreg_domain.RussianLegalEntity();

        russianLegalEntity.setEmail(proxyOptions.get(RUSSIAN_LEGAL_ENTITY_EMAIL));
        russianLegalEntity.setActualAddress(russianLegalEntityDomain.getActualAddress());
        russianLegalEntity.setInn(russianLegalEntityDomain.getInn());
        russianLegalEntity.setPostAddress(russianLegalEntityDomain.getPostAddress());
        russianLegalEntity.setRegisteredName(russianLegalEntityDomain.getRegisteredName());
        russianLegalEntity.setRepresentativeDocument(russianLegalEntityDomain.getRepresentativeDocument());
        russianLegalEntity.setRepresentativeFullName(russianLegalEntityDomain.getRepresentativeFullName());
        russianLegalEntity.setRepresentativePosition(russianLegalEntityDomain.getRepresentativePosition());

        com.rbkmoney.damsel.cashreg_domain.RussianBankAccount russianBankAccount = new com.rbkmoney.damsel.cashreg_domain.RussianBankAccount();

        russianLegalEntity.setRussianBankAccount(russianBankAccount);
        russianLegalEntity.setTaxMode(extractTaxModeFromOptions(proxyOptions));

        legalEntity.setRussianLegalEntity(russianLegalEntity);
        return legalEntity;
    }

    public ProxyObject extractProxyObject(CashReg cashReg) {
        Shop shop = partyManagementService.getShop(cashReg.getShopId(), cashReg.getPartyId());
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(cashReg.getPartyId(), shop.getContractId());

        VersionedObject versionedObject = dominantService.getVersionedObjectFromPaymentInstitution(paymentInstitutionRef);
        return extractProxyObject(versionedObject);
    }

    private ProxyObject extractProxyObject(VersionedObject versionedObject) {
        ProxyObject object = dominantService.getProxyObject(versionedObject);
        if (!object.isSetData()) {
            throw new IllegalStateException("ProxyObject not found; versionedObject: " + versionedObject.getVersion());
        }
        return object;
    }

    private ProviderObject extractProviderObject(VersionedObject versionedObject) {
        ProviderObject object = dominantService.getProviderObject(versionedObject);
        if (!object.isSetData()) {
            throw new IllegalStateException("ProviderObject not found; versionedObject: " + versionedObject.getVersion());
        }
        return object;
    }

    private TerminalObject extractTerminalObject(VersionedObject versionedObject) {
        TerminalObject object = dominantService.getTerminalObject(versionedObject);
        if (!object.isSetData()) {
            throw new IllegalStateException("TerminalObject not found; versionedObject: " + versionedObject.getVersion());
        }
        return object;
    }

}
