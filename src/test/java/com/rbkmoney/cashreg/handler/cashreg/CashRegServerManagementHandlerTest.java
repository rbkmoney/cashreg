package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.utils.CreateUtils;
import com.rbkmoney.cashreg.utils.MockUtils;
import com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators;
import com.rbkmoney.cashreg.utils.cashreg.creators.TypeCreators;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.damsel.domain_config.RepositoryClientSrv;
import com.rbkmoney.damsel.payment_processing.PartyManagementSrv;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.URISyntaxException;

import static com.rbkmoney.cashreg.utils.CreateUtils.createCashRegParams;
import static com.rbkmoney.cashreg.utils.CreateUtils.createPaymentInfo;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
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

    @Autowired
    private ManagementSrv.Iface management;

    @Before
    public void setup() throws URISyntaxException, TException {
        management = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/cashreg/management"))
                .build(ManagementSrv.Iface.class);
        doNothing().when(automatonClient).start(any(), any());
        doAnswer((Answer<Change>) invocation -> {
            CashReg cashReg = new CashReg()
                    .setId(cashregId)
                    .setPaymentInfo(CreateUtils.createPaymentInfo())
                    .setType(TypeCreators.createDebit())
                    .setShopId(shopId)
                    .setPartyId(partyId)
                    .setStatus(StatusCreators.createPendingStatus());

            return Change.created(new CreatedChange().setCashreg(cashReg));
        }).when(automatonClient).call(any(), any());

        // TODO: do party and dominant
        MockUtils.mockPartyManagement(partyManagementClient);
        MockUtils.mockDominant(dominantClient);
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
