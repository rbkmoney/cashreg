package com.rbkmoney.cashreg.service.provider;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregator;
import com.rbkmoney.cashreg.utils.cashreg.creators.CashRegProviderCreators;
import com.rbkmoney.cashreg.utils.cashreg.extractors.ReceiptExtractor;
import com.rbkmoney.damsel.cashreg.adapter.CashregAdapterSrv;
import com.rbkmoney.damsel.cashreg.adapter.CashregContext;
import com.rbkmoney.damsel.cashreg.adapter.CashregResult;
import com.rbkmoney.damsel.cashreg.processing.Receipt;
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
    private final Cache<String, CashregAdapterSrv.Iface> providerCache;

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
    public CashregResult register(Receipt receipt, com.rbkmoney.damsel.msgpack.Value value) {
        String receiptId = ReceiptExtractor.extractReceiptId(receipt);
        String url = extractUrl(receipt);
        log.info("Start register. receiptId {}, url {}", receiptId, url);
        Map<String, String> options = managementAggregate.aggregateOptions(
                CashRegProviderCreators.createCashregProviderRef(receipt.getCashregProvider().getProviderId()),
                receipt.getDomainRevision()
        );
        options.putAll(receipt.getCashregProvider().getProviderParams());
        CashregContext context = prepareCashRegContext(receipt, options, value);
        log.info("Finish register. receiptId {}, url {}", receiptId, url);
        return call(url, NETWORK_TIMEOUT_SEC, context);
    }

    private CashregResult call(String url, Integer networkTimeout, CashregContext context) {
        log.debug("Start call. url {}", url);
        CashregAdapterSrv.Iface provider = providerCache.get(url, key -> cashRegProviderSrv(url, networkTimeout));
        try {
            CashregResult cashregResult = provider.register(context);
            log.debug("Finish call. url {}, result {}", url, cashregResult);
            return cashregResult;
        } catch (TException ex) {
            // Add more exception
            log.error("Call exception", ex);
            throw new RuntimeException(ex);
        }
    }

    private String extractUrl(Receipt receipt) {
        String receiptId = ReceiptExtractor.extractReceiptId(receipt);
        log.debug("Start extractUrl. receiptId {}", receiptId);
        ProxyObject proxyObject = managementAggregate.extractProxyObject(receipt);
        log.debug("Finish extractUrl. receiptId {}, proxyObject {}", receiptId, proxyObject);
        return proxyObject.getData().getUrl();
    }

    private CashregAdapterSrv.Iface cashRegProviderSrv(String url, Integer networkTimeout) {
        try {
            return new THSpawnClientBuilder()
                    .withAddress(new URI(url))
                    .withNetworkTimeout(networkTimeout)
                    .build(CashregAdapterSrv.Iface.class);
        } catch (URISyntaxException e) {
            throw new RuntimeException("Can't connect provider");
        }
    }

}
