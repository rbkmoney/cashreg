package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.damsel.domain.*;
import com.rbkmoney.damsel.domain_config.VersionedObject;

public interface DominantService {

    PaymentInstitutionObject getPaymentInstitutionRef(PaymentInstitutionRef paymentInstitutionRef);

    ProxyObject getProxyObject(ProxyRef proxyRef);

    TerminalObject getTerminalObject(TerminalRef terminalRef);

    ProviderObject getProviderObject(ProviderRef providerRef);

    VersionedObject getVersionedObjectFromReference(com.rbkmoney.damsel.domain.Reference reference);

}
