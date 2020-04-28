package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.utils.MockUtils;
import com.rbkmoney.cashreg.utils.TestData;
import com.rbkmoney.damsel.cashreg.base.EventRange;
import com.rbkmoney.damsel.cashreg.processing.*;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import static com.rbkmoney.cashreg.utils.CreateUtils.createDefaultReceiptParams;
import static junit.framework.TestCase.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class CashRegServerManagementHandlerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private AutomatonClient<Value, Change> automatonClient;

    @MockBean
    private PartyManagementService partyManagementService;

    @MockBean
    private DominantService dominantService;

    private ManagementSrv.Iface managementClient;

    @Before
    public void setup() throws URISyntaxException {
        managementClient = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/cashreg/management"))
                .build(ManagementSrv.Iface.class);
        MockUtils.mockDominant(dominantService);
        MockUtils.mockPartyManagement(partyManagementService);
        MockUtils.mockAutomatonClient(automatonClient);
    }

    @Test
    public void create() throws TException {
        ReceiptParams receiptParams = createDefaultReceiptParams();
        managementClient.create(receiptParams);
        Receipt receipt = managementClient.get(receiptParams.getReceiptId());
        assertTrue(receipt.getStatus().isSetPending());
    }

    @Test
    public void getEvents() throws TException {
        EventRange eventRange = new EventRange().setLimit(3).setAfter(1L);
        List<Event> eventList = managementClient.getEvents(TestData.RECEIPT_ID, eventRange);
        assertTrue(eventList.size() > 0);
        assertTrue(eventList.get(1).getChange().getStatusChanged().getStatus().isSetPending());
    }

}