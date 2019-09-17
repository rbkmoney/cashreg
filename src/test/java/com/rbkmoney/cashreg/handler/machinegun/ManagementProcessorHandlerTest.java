package com.rbkmoney.cashreg.handler.machinegun;

import com.rbkmoney.cashreg.AbstractIntegrationTest;
import com.rbkmoney.cashreg.utils.ProtoUtils;
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

import static com.rbkmoney.cashreg.utils.cashreg.creators.StatusCreators.createPendingStatus;


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
        Change change = Change.status_changed(new StatusChange().setStatus(createPendingStatus()));

        SignalArgs signalArgs = new SignalArgs();
        signalArgs.setSignal(Signal.init(new InitSignal(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))))));
        signalArgs.setMachine(new Machine()
                .setId(cashregId)
                .setNs(namespace)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));

        client.processSignal(signalArgs);
    }

    @Test
    public void processSignalTimeout() throws TException {
        SignalArgs signalArgs = new SignalArgs();
        TimeoutSignal timeoutSignal = new TimeoutSignal();
        signalArgs.setSignal(Signal.timeout(timeoutSignal));
        signalArgs.setMachine(new Machine()
                .setId(cashregId)
                .setNs(namespace)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));
        client.processSignal(signalArgs);
    }

    @Test
    public void processCall() throws TException {
        Change change = Change.status_changed(new StatusChange().setStatus(createPendingStatus()));
        CallArgs callArgs = new CallArgs();
        callArgs.setArg(Value.bin(Geck.toMsgPack(ProtoUtils.toValue(Collections.singletonList(change)))));
        callArgs.setMachine(new Machine()
                .setId(cashregId)
                .setNs(namespace)
                .setHistory(new ArrayList<>())
                .setHistoryRange(new HistoryRange()));
        client.processCall(callArgs);
    }

}