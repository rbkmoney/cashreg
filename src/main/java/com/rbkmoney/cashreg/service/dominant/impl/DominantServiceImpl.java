package com.rbkmoney.cashreg.service.dominant.impl;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.Reference;
import com.rbkmoney.damsel.domain_config.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

import static com.rbkmoney.damsel.domain.Reference.payment_institution;
import static com.rbkmoney.damsel.domain.Reference.system_account_set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DominantServiceImpl implements DominantService {

    private final RepositoryClientSrv.Iface dominantClient;
    private final RetryTemplate retryTemplate;

    @Override
    public PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException {
        log.info("Trying to get payment institution, paymentInstitutionRef='{}'", paymentInstitutionRef);
        VersionedObject versionedObject = getVersionedObjectFromPaymentInstitution(paymentInstitutionRef);
        versionedObject.getObject().getProvider().getData().getProxy();
        PaymentInstitution paymentInstitution = versionedObject.getObject().getPaymentInstitution().getData();
        log.info("Payment institution has been found, PaymentInstitutionRef='{}', paymentInstitution='{}'", paymentInstitutionRef, paymentInstitution);
        return paymentInstitution;
    }

    @Override
    public VersionedObject getVersionedObjectFromPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException {
        log.info("Trying to get VersionedObject, paymentInstitutionRef='{}'", paymentInstitutionRef);
        try {
            VersionedObject versionedObject = checkoutObject(
                    Reference.head(new Head()),
                    payment_institution(paymentInstitutionRef)
            );
            log.info("VersionedObject {} has been found, paymentInstitutionRef='{}'", versionedObject, paymentInstitutionRef);
            return versionedObject;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        }
    }

    @Override
    public VersionedObject getVersionedObjectFromSystemAccount(SystemAccountSetRef systemAccountSetRef) throws NotFoundException {
        log.info("Trying to get VersionedObject, systemAccountSetRef='{}'", systemAccountSetRef);
        try {
            VersionedObject versionedObject = checkoutObject(
                    Reference.head(new Head()),
                    system_account_set(systemAccountSetRef)
            );
            log.info("VersionedObject has been found, systemAccountSetRef='{}'", versionedObject, systemAccountSetRef);
            return versionedObject;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, systemAccountSetRef='%s'", systemAccountSetRef), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, systemAccountSetRef='%s'", systemAccountSetRef), ex);
        }
    }

    @Override
    public ProxyObject getProxyObject(VersionedObject versionedObject) {
        log.info("Trying to get ProxyObject, versionedObject='{}'", versionedObject);
        ProxyObject proxyObject = versionedObject.getObject().getProxy();
        log.info("ProxyObject has been found, versionedObject='{}'", proxyObject, versionedObject);
        return proxyObject;
    }

    @Override
    public TerminalObject getTerminalObject(VersionedObject versionedObject) {
        log.info("Trying to get TerminalObject, versionedObject='{}'", versionedObject);
        TerminalObject terminalObject = versionedObject.getObject().getTerminal();
        log.info("TerminalObject has been found, versionedObject='{}'", terminalObject, versionedObject);
        return terminalObject;
    }

    @Override
    public ProviderObject getProviderObject(VersionedObject versionedObject) {
        log.info("Trying to get ProviderObject, versionedObject='{}'", versionedObject);
        ProviderObject providerObject = versionedObject.getObject().getProvider();
        log.info("ProviderObject has been found, versionedObject='{}'", providerObject, versionedObject);
        return providerObject;
    }

    @Override
    public SystemAccountSet getSystemAccountSet(SystemAccountSetRef systemAccountSetRef) {
        log.info("Trying to get systemAccountSet, systemAccountSetRef='{}'", systemAccountSetRef);
        VersionedObject versionedObject = getVersionedObjectFromSystemAccount(systemAccountSetRef);
        SystemAccountSet systemAccountSet = versionedObject.getObject().getSystemAccountSet().getData();
        log.info("SystemAccountSet has been found, systemAccountSetRef='{}', systemAccountSet='{}'", systemAccountSetRef, systemAccountSet);
        return systemAccountSet;
    }

    private VersionedObject checkoutObject(Reference revisionReference, com.rbkmoney.damsel.domain.Reference reference) throws TException {
        return retryTemplate.execute(
                context -> dominantClient.checkoutObject(revisionReference, reference)
        );
    }
}
