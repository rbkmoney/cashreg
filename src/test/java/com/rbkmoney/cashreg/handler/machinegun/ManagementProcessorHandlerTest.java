package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.cashreg.utils.TestData;
import com.rbkmoney.damsel.cashreg.status.Pending;
import com.rbkmoney.damsel.cashreg.status.Status;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.StatusChange;
import com.rbkmoney.geck.serializer.Geck;
import com.rbkmoney.machinegun.msgpack.Value;
import com.rbkmoney.machinegun.stateproc.*;
import com.rbkmoney.woody.thrift.impl.http.THSpawnClientBuilder;
import org.apache.thrift.TException;
import org.junit.Before;
import org.junit.Test;
import org.springframework.boot.web.server.LocalServerPort;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.Assert.assertTrue;


public class ManagementProcessorHandlerTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    private ProcessorSrv.Iface client;

    @Before
    public void setup() throws URISyntaxException, TException {
        client = new THSpawnClientBuilder()
                .withAddress(new URI("http://localhost:" + port + "/v1/processor"))
                .build(ProcessorSrv.Iface.class);
    }

    @Test
    public void processSignalInit() throws TException {
        Change change = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));

        SignalArgs signalArgs = new SignalArgs();
        signalArgs.setSignal(Signal.init(new InitSignal(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))))));
        signalArgs.setMachine(new Machine()
                .setId(TestData.CASHREG_ID)
                .setNs(TestData.CASHREG_NAMESPACE)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));

        SignalResult result = client.processSignal(signalArgs);
        assertTrue(result.getAction().getTimer().isSetSetTimer());
    }

    @Test
    public void processCall() throws TException {
        Change change = Change.status_changed(new StatusChange().setStatus(Status.pending(new Pending())));
        CallArgs callArgs = new CallArgs();
        callArgs.setArg(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))));
        callArgs.setMachine(new Machine()
                .setId(TestData.CASHREG_ID)
                .setNs(TestData.CASHREG_NAMESPACE)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));
        CallResult result = client.processCall(callArgs);
        assertTrue(result.isSetAction());
    }

}