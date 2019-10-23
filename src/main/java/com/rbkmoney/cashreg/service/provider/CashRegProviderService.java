package com.rbkmoney.cashreg.service.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregator;
import com.rbkmoney.cashreg.utils.cashreg.creators.CashRegProviderCreators;
import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.CashRegProviderSrv;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.damsel.cashreg_processing.CashReg;
import com.rbkmoney.damsel.domain.ProxyObject;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;

import static com.rbkmoney.cashreg.service.management.impl.ManagementServiceImpl.NETWORK_TIMEOUT_SEC;
import static com.rbkmoney.cashreg.utils.ProtoUtils.prepareCashRegContext;

@Slf4j
@Component
public class CashRegProviderService implements CashRegProvider {

    private final ManagementAggregator managementAggregate;
    private final Cache<String, CashRegProviderSrv.Iface> providerCache;

    @Autowired
    public CashRegProviderService(
            ManagementAggregator managementAggregate,
            @Value("${cache.maxSize}") long cacheMaximumSize
    ) {
        this.managementAggregate = managementAggregate;
        this.providerCache = Caffeine.newBuilder()
                .maximumSize(cacheMaximumSize)
                .build();
    }

    @Override
    public CashRegResult register(CashReg cashReg) {
        String url = extractUrl(cashReg);
        Map<String, String> options = managementAggregate.aggregateOptions(
                CashRegProviderCreators.createCashregProviderRef(cashReg.getCashregProviderId()),
                cashReg.getDomainRevision()
        );
        CashRegContext context = prepareCashRegContext(cashReg, options);
        return call(url, NETWORK_TIMEOUT_SEC, context);
    }

    private CashRegResult call(String url, Integer networkTimeout, CashRegContext context) {
        CashRegProviderSrv.Iface provider = providerCache.get(url, key -> cashRegProviderSrv(url, networkTimeout));
        try {
            return provider.register(context);
        } catch (TException e) {
            // Add more exception
            throw new RuntimeException(e);
        }
    }

    private String extractUrl(CashReg cashReg) {
        ProxyObject proxyObject = managementAggregate.extractProxyObject(cashReg);
        return proxyObject.getData().getUrl();
    }

    private CashRegProviderSrv.Iface cashRegProviderSrv(String url, Integer networkTimeout) {
        try {
            return new THSpawnClientBuilder()
                    .withAddress(new URI(url))
                    .withNetworkTimeout(networkTimeout)
                    .build(CashRegProviderSrv.Iface.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't connect provider");
        }
    }

}
