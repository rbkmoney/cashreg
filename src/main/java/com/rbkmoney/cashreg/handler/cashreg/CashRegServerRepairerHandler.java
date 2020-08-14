package com.rbkmoney.cashreg.handler.cashreg;

import com.rbkmoney.damsel.cashreg.processing.Change;
import com.rbkmoney.damsel.cashreg.repairer.RepairScenario;
import com.rbkmoney.damsel.cashreg.repairer.RepairerSrv;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CashRegServerRepairerHandler implements RepairerSrv.Iface {

    private final AutomatonClient<Value, Change> automatonClient;

    @Override
    public void repair(String cashRegID, RepairScenario repairScenario) {
        throw new RuntimeException("Not supported yet");
    }

}
