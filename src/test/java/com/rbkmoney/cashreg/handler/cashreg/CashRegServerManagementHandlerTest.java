package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.service.management.aggregate.ManagementAggregate;
import com.rbkmoney.cashreg.service.mg.aggregate.handler.MgChangeHandler;
import com.rbkmoney.cashreg.utils.cashreg.creators.TypeCreators;
import com.rbkmoney.damsel.cashreg_processing.CashRegParams;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.ManagementSrv;
import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.URISyntaxException;

import static com.rbkmoney.cashreg.utils.CreateUtils.createCashRegParams;
import static com.rbkmoney.cashreg.utils.CreateUtils.createPaymentInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;

public class CashRegServerManagementHandlerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private AutomatonClient<Value, Change> automatonClient;

    @MockBean
    private PartyManagementSrv.Iface partyManagementClient;

    @MockBean
    private RepositoryClientSrv.Iface dominantClient;

    @MockBean
    private ManagementAggregate managementAggregate;

    @MockBean
    private MgChangeHandler mgChangeHandler;

    @Autowired
    private ManagementSrv.Iface management;



    @Before
    public void setup() throws URISyntaxException, TException {
        management = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/cashreg/management"))
                .build(ManagementSrv.Iface.class);
        doNothing().when(automatonClient).start(any(), any());

    }

    @Test
    public void create() throws TException {
        CashRegParams params = createCashRegParams(
                cashregId, partyId, shopId,
                TypeCreators.createDebit(), createPaymentInfo()
        );
        management.create(params);
    }

}
