package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.VersionedObject;

public interface DominantService {

    ResponseDominantWrapper<ProxyObject> getProxyObject(ProxyRef proxyRef, Long revisionVersion);

    ResponseDominantWrapper<TerminalObject> getTerminalObject(TerminalRef terminalRef, Long revisionVersion);

    ResponseDominantWrapper<CashRegProviderObject> getCashRegProviderObject(CashRegProviderRef providerRef, Long revisionVersion);

    ResponseDominantWrapper<VersionedObject> getVersionedObjectFromReference(com.rbkmoney.damsel.domain.Reference reference, Long revisionVersion);

}
