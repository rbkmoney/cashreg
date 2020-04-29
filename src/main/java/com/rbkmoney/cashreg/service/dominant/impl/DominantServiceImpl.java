package com.rbkmoney.cashreg.service.dominant.impl;

import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
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
    public ResponseDominantWrapper<ProxyObject> getProxyObject(ProxyRef proxyRef, Long revisionVersion) {
        log.info("Trying to get ProxyObject, proxyRef='{}'", proxyRef);
        ResponseDominantWrapper<VersionedObject> versionedObjectWrapper = getVersionedObjectFromReference(proxy(proxyRef), revisionVersion);
        ProxyObject proxyObject = versionedObjectWrapper.getResponse().getObject().getProxy();
        log.info("ProxyObject has been found, versionedObjectWrapper='{}'", proxyObject, versionedObjectWrapper);
        ResponseDominantWrapper<ProxyObject> response = new ResponseDominantWrapper<>();
        response.setResponse(proxyObject);
        response.setRevisionVersion(versionedObjectWrapper.getRevisionVersion());
        return response;
    }

    @Override
    public ResponseDominantWrapper<CashRegisterProviderObject> getCashRegisterProviderObject(CashRegisterProviderRef providerRef, Long revisionVersion) {
        log.info("Trying to get CashRegProviderObject, providerRef='{}'", providerRef);
        ResponseDominantWrapper<VersionedObject> versionedObjectWrapper = getVersionedObjectFromReference(cash_register_provider(providerRef), revisionVersion);
        CashRegisterProviderObject providerObject = versionedObjectWrapper.getResponse().getObject().getCashRegisterProvider();
        log.info("CashRegProviderObject {} has been found, versionedObjectWrapper='{}'", providerObject, versionedObjectWrapper);
        ResponseDominantWrapper<CashRegisterProviderObject> response = new ResponseDominantWrapper<>();
        response.setResponse(providerObject);
        response.setRevisionVersion(versionedObjectWrapper.getRevisionVersion());
        return response;
    }

    private ResponseDominantWrapper<VersionedObject> getVersionedObjectFromReference(com.rbkmoney.damsel.domain.Reference reference, Long revisionVersion) {
        log.info("Trying to get VersionedObject, reference='{}'", reference);
        try {
            Reference referenceRevision;
            if (revisionVersion == null) {
                referenceRevision = Reference.head(new Head());
            } else {
                referenceRevision = Reference.version(revisionVersion);
            }
            VersionedObject versionedObject = checkoutObject(referenceRevision, reference);
            log.info("VersionedObject {} has been found, reference='{}'", versionedObject, reference);
            ResponseDominantWrapper<VersionedObject> response = new ResponseDominantWrapper<>();
            response.setResponse(versionedObject);
            response.setRevisionVersion(versionedObject.getVersion());
            return response;
        } catch (VersionNotFound | ObjectNotFound ex) {
            throw new NotFoundException(String.format("Version not found, reference='%s'", reference), ex);
        } catch (TException ex) {
            throw new RuntimeException(String.format("Failed to get payment institution, reference='%s'", reference), ex);
        }
    }

    private VersionedObject checkoutObject(Reference revisionReference, com.rbkmoney.damsel.domain.Reference reference) throws TException {
        return retryTemplate.execute(
                context -> dominantClient.checkoutObject(revisionReference, reference)
        );
    }
}
