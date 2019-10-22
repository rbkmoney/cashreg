package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.MockUtils;
import com.rbkmoney.cashreg.utils.TestData;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg_processing.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Spy;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.rbkmoney.cashreg.utils.CreateUtils.createDefaultCashRegParams;
import static junit.framework.TestCase.assertTrue;

public class CashRegServerManagementHandlerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private AutomatonClient<Value, Change> automatonClient;

    @MockBean
    private PartyManagementService partyManagementService;

    @MockBean
    public DominantService dominantService;

    @Spy
    public com.rbkmoney.cashreg.service.provider.CashRegProvider cashRegProvider;

    private ManagementSrv.Iface managementClient;

    @Before
    public void setup() throws URISyntaxException {
        managementClient = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/cashreg/management"))
                .build(ManagementSrv.Iface.class);
        MockUtils.mockDominant(dominantService);
        MockUtils.mockPartyManagement(partyManagementService);
        MockUtils.mockAutomatonClient(automatonClient);
        MockUtils.mockCashRegProvider(cashRegProvider);
    }

    @Test
    public void create() throws TException {
        CashRegParams cashRegParams = createDefaultCashRegParams();
        managementClient.create(cashRegParams);
        CashReg cashReg = managementClient.get(cashRegParams.getCashregId());
        assertTrue(cashReg.getStatus().isSetPending());
    }

    @Test
    public void get() throws TException {
        CashReg cashReg = managementClient.get(TestData.CASHREG_ID);
        assertTrue(cashReg.getStatus().isSetPending());
    }

    @Test
    public void getEvents() throws TException {
        EventRange eventRange = new EventRange().setLimit(1).setAfter(1L);
        List<Event> eventList = managementClient.getEvents(TestData.CASHREG_ID, eventRange);
        assertTrue(eventList.size() > 0);
        assertTrue(eventList.get(1).getChange().getStatusChanged().getStatus().isSetPending());
    }

}