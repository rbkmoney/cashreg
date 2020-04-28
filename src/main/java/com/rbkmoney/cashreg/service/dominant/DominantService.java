package com.rbkmoney.cashreg.service.dominant;

import com.rbkmoney.cashreg.service.dominant.model.ResponseDominantWrapper;
import com.rbkmoney.damsel.domain.*;

public interface DominantService {

    ResponseDominantWrapper<ProxyObject> getProxyObject(ProxyRef proxyRef, Long revisionVersion);

    ResponseDominantWrapper<CashRegisterProviderObject> getCashRegisterProviderObject(CashRegisterProviderRef providerRef, Long revisionVersion);

}
