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
import static com.rbkmoney.damsel.domain.Reference.payment_institution;

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

        VersionedObject versionedObject = dominantService.getVersionedObjectFromReference(payment_institution(paymentInstitutionRef));
        ProviderRef providerRef = versionedObject.getObject().getPaymentInstitution().getData().getProviders().getValue().iterator().next();

        ProviderObject providerObject = dominantService.getProviderObject(providerRef);

        Map<String, String> aggregateOptions = aggregateOptions(providerObject);

        AccountInfo accountInfo = new AccountInfo();
        Contract contract = partyManagementService.getContract(params.getPartyId(), shop.getContractId());
        accountInfo.setLegalEntity(prepareLegalEntity(contract, aggregateOptions));
        cashReg.setAccountInfo(accountInfo);

        created.setCashreg(cashReg);
        return Change.created(created);
    }

    private Map<String, String> aggregateOptions(ProviderObject providerObject) {
        Proxy proxy = providerObject.getData().getProxy();
        ProxyObject proxyObject = dominantService.getProxyObject(proxy.getRef());

        ProviderTerminalRef providerTerminalRef = providerObject.getData().getTerminal().getValue().iterator().next();
        TerminalRef terminalRef = new TerminalRef().setId(providerTerminalRef.getId());
        TerminalObject terminalObject = dominantService.getTerminalObject(terminalRef);

        Map<String, String> proxyOptions = proxyObject.getData().getOptions();
        proxyOptions.putAll(proxy.getAdditional());
        proxyOptions.putAll(terminalObject.getData().getOptions());
        return proxyOptions;
    }

    public Map<String, String> aggregateOptions(String shopId, String partyId) {
        Shop shop = partyManagementService.getShop(shopId, partyId);
        PaymentInstitutionRef paymentInstitutionRef = partyManagementService.getPaymentInstitutionRef(partyId, shop.getContractId());

        VersionedObject versionedObject = dominantService.getVersionedObjectFromReference(payment_institution(paymentInstitutionRef));
        return aggregateOptions(versionedObject.getObject().getProvider());
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

        PaymentInstitutionObject paymentInstitution = dominantService.getPaymentInstitutionRef(paymentInstitutionRef);
        ProviderObject provider = dominantService.getProviderObject(paymentInstitution.getData().getProviders().getValue().iterator().next());

        return extractProxyObject(provider.getData().getProxy().getRef());
    }

    private ProxyObject extractProxyObject(ProxyRef proxyRef) {
        ProxyObject object = dominantService.getProxyObject(proxyRef);
        if (!object.isSetData()) {
            throw new IllegalStateException("ProxyObject not found; proxyRef: " + proxyRef);
        }
        return object;
    }

}
