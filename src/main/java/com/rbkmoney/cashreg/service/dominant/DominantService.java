package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.VersionedObject;

public interface DominantService {

    ProxyObject getProxyObject(ProxyRef proxyRef);

    TerminalObject getTerminalObject(TerminalRef terminalRef);

    CashRegProviderObject getCashRegProviderObject(CashRegProviderRef providerRef);

    VersionedObject getVersionedObjectFromReference(com.rbkmoney.damsel.domain.Reference reference);

}
