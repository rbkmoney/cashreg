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

import static com.rbkmoney.damsel.domain.Reference.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class DominantServiceImpl implements DominantService {

    private final RepositoryClientSrv.Iface dominantClient;
    private final RetryTemplate retryTemplate;

    @Override
    public VersionedObject getVersionedObjectFromReference(com.rbkmoney.damsel.domain.Reference reference) {
        log.info("Trying to get VersionedObject, reference='{}'", reference);
        try {
            VersionedObject versionedObject = checkoutObject(Reference.head(new Head()), reference);
            log.info("VersionedObject has been found, reference='{}'", versionedObject, reference);
            return versionedObject;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, reference='%s'", reference), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, reference='%s'", reference), ex);
        }
    }

    @Override
    public PaymentInstitutionObject getPaymentInstitutionRef(PaymentInstitutionRef paymentInstitutionRef) {
        log.info("Trying to get PaymentInstitutionObject, paymentInstitutionRef='{}'", paymentInstitutionRef);
        VersionedObject versionedObject = getVersionedObjectFromReference(payment_institution(paymentInstitutionRef));
        PaymentInstitutionObject paymentInstitution = versionedObject.getObject().getPaymentInstitution();
        log.info("PaymentInstitution has been found, versionedObject='{}'", paymentInstitution, versionedObject);
        return paymentInstitution;
    }

    @Override
    public ProxyObject getProxyObject(ProxyRef proxyRef) {
        log.info("Trying to get ProxyObject, proxyRef='{}'", proxyRef);
        VersionedObject versionedObject = getVersionedObjectFromReference(proxy(proxyRef));
        ProxyObject proxyObject = versionedObject.getObject().getProxy();
        log.info("ProxyObject has been found, versionedObject='{}'", proxyObject, versionedObject);
        return proxyObject;
    }

    @Override
    public TerminalObject getTerminalObject(TerminalRef terminalRef) {
        log.info("Trying to get TerminalObject, terminalRef='{}'", terminalRef);
        VersionedObject versionedObject = getVersionedObjectFromReference(terminal(terminalRef));
        TerminalObject terminalObject = versionedObject.getObject().getTerminal();
        log.info("TerminalObject has been found, versionedObject='{}'", terminalObject, versionedObject);
        return terminalObject;
    }


    @Override
    public CashRegProviderObject getCashRegProviderObject(CashRegProviderRef providerRef) {
        log.info("Trying to get CashRegProviderObject, providerRef='{}'", providerRef);
        VersionedObject versionedObject = getVersionedObjectFromReference(cashreg_provider(providerRef));
        CashRegProviderObject providerObject = versionedObject.getObject().getCashregProvider();
        log.info("CashRegProviderObject has been found, versionedObject='{}'", providerObject, versionedObject);
        return providerObject;
    }

    private VersionedObject checkoutObject(Reference revisionReference, com.rbkmoney.damsel.domain.Reference reference) throws TException {
        return retryTemplate.execute(
                context -> dominantClient.checkoutObject(revisionReference, reference)
        );
    }
}
