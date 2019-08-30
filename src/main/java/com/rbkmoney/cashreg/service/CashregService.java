package com.rbkmoney.cashreg.service;

import com.rbkmoney.damsel.context.Change;
import com.rbkmoney.machinarium.client.AutomatonClient;
import com.rbkmoney.machinegun.msgpack.Value;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CashregService {

    private final AutomatonClient<Value, Change> automatonClient;

}
