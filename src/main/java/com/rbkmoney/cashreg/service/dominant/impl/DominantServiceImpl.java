package com.rbkmoney.cashreg.service.dominant.impl;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.exception.NotFoundException;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.Reference;
import com.rbkmoney.damsel.domain_config.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class DominantServiceImpl implements DominantService {

    private final RepositoryClientSrv.Iface dominantClient;

    private final RetryTemplate retryTemplate;

    public DominantServiceImpl(RepositoryClientSrv.Iface dominantClient, RetryTemplate retryTemplate) {
        this.dominantClient = dominantClient;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public PaymentInstitution getPaymentInstitution(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException {
        log.info("Trying to get payment institution, paymentInstitutionRef='{}'", paymentInstitutionRef);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setPaymentInstitution(paymentInstitutionRef);
            VersionedObject versionedObject = checkoutObject(Reference.head(new Head()), reference);

            versionedObject.getObject().getProvider().getData().getProxy();

            PaymentInstitution paymentInstitution = versionedObject.getObject().getPaymentInstitution().getData();
            log.info("Payment institution has been found, PaymentInstitutionRef='{}', paymentInstitution='{}'", paymentInstitutionRef, paymentInstitution);
            return paymentInstitution;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        }
    }

    @Override
    public ProxyObject getProxyObject(PaymentInstitutionRef paymentInstitutionRef) throws NotFoundException {
        log.info("Trying to get ProxyObject, paymentInstitutionRef='{}'", paymentInstitutionRef);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setPaymentInstitution(paymentInstitutionRef);
            VersionedObject versionedObject = checkoutObject(Reference.head(new Head()), reference);

            ProxyObject proxyObject = versionedObject.getObject().getProxy();
            log.info("ProxyObject has been found, paymentInstitutionRef='{}'", proxyObject, paymentInstitutionRef);
            return proxyObject;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, paymentInstitutionRef='%s'", paymentInstitutionRef), ex);
        }
    }

    @Override
    public SystemAccountSet getSystemAccountSet(SystemAccountSetRef systemAccountSetRef) throws NotFoundException {
        log.info("Trying to get systemAccountSet, systemAccountSetRef='{}'", systemAccountSetRef);
        try {
            com.rbkmoney.damsel.domain.Reference reference = new com.rbkmoney.damsel.domain.Reference();
            reference.setSystemAccountSet(systemAccountSetRef);
            VersionedObject versionedObject = checkoutObject(Reference.head(new Head()), reference);
            SystemAccountSet systemAccountSet = versionedObject.getObject().getSystemAccountSet().getData();
            log.info("SystemAccountSet has been found, systemAccountSetRef='{}', systemAccountSet='{}'", systemAccountSetRef, systemAccountSet);
            return systemAccountSet;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, systemAccountSetRef='%s'", systemAccountSetRef), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get systemAccountSet, systemAccountSetRef='%s'", systemAccountSetRef), ex);
        }
    }

    private VersionedObject checkoutObject(Reference revisionReference, com.rbkmoney.damsel.domain.Reference reference) throws TException {
        return retryTemplate.execute(
                context -> dominantClient.checkoutObject(revisionReference, reference)
        );
    }
}
