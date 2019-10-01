package com.rbkmoney.cashreg.service.provider;

import com.rbkmoney.damsel.cashreg.provider.CashRegContext;
import com.rbkmoney.damsel.cashreg.provider.CashRegProviderSrv;
import com.rbkmoney.damsel.cashreg.provider.CashRegResult;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;


@Component
public class CashRegProviderService implements CashRegProvider {

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

    @Override
    public CashRegResult register(String url, Integer networkTimeout, CashRegContext context) {
        CashRegProviderSrv.Iface provider = cashRegProviderSrv(url, networkTimeout);
        try {
            return provider.register(context);
        } catch (TException e) {
            // Add more exception
            throw new RuntimeException(e);
        }
    }

}
