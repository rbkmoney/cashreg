package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.service.dominant.DominantService;
import com.rbkmoney.cashreg.service.pm.PartyManagementService;
import com.rbkmoney.cashreg.service.provider.CashRegProviderService;
import com.rbkmoney.cashreg.utils.CreateUtils;
import com.rbkmoney.cashreg.utils.MockUtils;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.cashreg.utils.TestData;
import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.processing.StatusChange;
import com.rbkmoney.damsel.cashreg.receipt.status.Pending;
import com.rbkmoney.damsel.cashreg.receipt.status.Status;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import com.rbkmoney.woody.api.flow.error.WRuntimeException;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.jetbrains.annotations.NotNull;
import org.junit.Before;
import org.junit.Test;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.annotation.DirtiesContext;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.junit.Assert.assertTrue;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
public class ManagementProcessorHandlerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @MockBean
    private AutomatonClient<Value, Change> automatonClient;

    @MockBean
    private PartyManagementService partyManagementService;

    @MockBean
    public DominantService dominantService;

    @MockBean
    public CashRegProviderService cashRegProvider;

    private ProcessorSrv.Iface client;

    private static Long count = 1L;

    @Before
    public void setup() throws URISyntaxException, TException {
        MockitoAnnotations.initMocks(this);
        client = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/v1/processor"))
                .build(ProcessorSrv.Iface.class);
        MockUtils.mockDominant(dominantService);
        MockUtils.mockPartyManagement(partyManagementService);
        MockUtils.mockAutomatonClient(automatonClient);
        MockUtils.mockCashRegProvider(cashRegProvider);
    }

    @Test
    public void processSignalInit() throws TException {
        Change change = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));

        SignalArgs signalArgs = new SignalArgs();
        signalArgs.setSignal(Signal.init(new InitSignal(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))))));
        prepareMachineEvents(signalArgs, new ArrayList<>());

        SignalResult result = client.processSignal(signalArgs);
        assertTrue(result.getAction().getTimer().isSetSetTimer());
    }

    @Test(expected = WRuntimeException.class)
    public void processCall() throws TException {
        Change change = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));
        CallArgs callArgs = new CallArgs();
        callArgs.setArg(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))));
        callArgs.setMachine(new Machine()
                .setId(TestData.RECEIPT_ID)
                .setNs(TestData.CASHREG_NAMESPACE)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));
        client.processCall(callArgs);
    }

    @Test
    public void testFlow() throws TException {
        // Created
        List<Change> changeList = new ArrayList<>();
        Change createdChange = CreateUtils.createCreatedChange(CreateUtils.createDefaultReceiptParams());
        changeList.add(createdChange);

        SignalArgs signalArgs = new SignalArgs();
        signalArgs.setSignal(Signal.init(new InitSignal(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(changeList))))));
        prepareMachineEvents(signalArgs, new ArrayList<>());

        SignalResult result = client.processSignal(signalArgs);
        assertTrue(result.getAction().getTimer().isSetSetTimer());
        // Pending
        List<Change> changes = convert(result);
        assertTrue(changes.size() == 2);

        signalArgs.setSignal(Signal.timeout(new TimeoutSignal()));
        List<Event> events = prepareEvents(changes);
        prepareMachineEvents(signalArgs, events);

        result = client.processSignal(signalArgs);
        changes = convert(result);
        assertTrue(changes.size() == 3);

        events = prepareEvents(changes);
        prepareMachineEvents(signalArgs, events);

        result = client.processSignal(signalArgs);
        changes = convert(result);
        assertTrue(changes.size() == 4);

        events = prepareEvents(changes);
        prepareMachineEvents(signalArgs, events);

        result = client.processSignal(signalArgs);
        // Delivered
        changes = convert(result);
        assertTrue(changes.size() == 5);
        assertTrue(changes.get(changes.size() - 1).getStatusChanged().getStatus().isSetDelivered());
    }

    private void prepareMachineEvents(SignalArgs signalArgs, List<Event> events) {
        signalArgs.setMachine(new Machine()
                .setId(TestData.RECEIPT_ID)
                .setNs(TestData.CASHREG_NAMESPACE)
                .setHistory(events)
                .setHistoryRange(new HistoryRange()));
    }

    @NotNull
    private List<Event> prepareEvents(List<Change> changes) {
        return changes.stream()
                .map(event -> new Event(
                        count++,
                        "2019-10-22T22:12:27Z",
                        Value.bin(Geck.toMsgPack(event)))).collect(Collectors.toList());
    }

    private List<Change> convert(SignalResult result) {
        return result.getChange().getEventsLegacy()
                .stream()
                .map(event -> Geck.msgPackToTBase(event.getBin(), Change.class))
                .collect(Collectors.toList());
    }

}