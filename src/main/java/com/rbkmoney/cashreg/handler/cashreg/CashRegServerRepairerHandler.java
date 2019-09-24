package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.cashreg.utils.ProtoUtils;
import com.rbkmoney.damsel.cashreg.CashRegNotFound;
import com.rbkmoney.damsel.cashreg.MachineAlreadyWorking;
import com.rbkmoney.damsel.cashreg_processing.Change;
import com.rbkmoney.damsel.cashreg_processing.RepairScenario;
import com.rbkmoney.damsel.cashreg_processing.RepairerSrv;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.thrift.TException;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashRegServerRepairerHandler implements RepairerSrv.Iface {

    private final AutomatonClient<Value, Change> automatonClient;

    @Override
    public void repair(String cashRegID, RepairScenario repairScenario) throws CashRegNotFound, MachineAlreadyWorking, TException {
        automatonClient.call(cashRegID, ProtoUtils.toValue(repairScenario.getAddEvents().getEvents()));
    }

}
