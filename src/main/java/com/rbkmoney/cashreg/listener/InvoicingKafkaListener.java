package com.rbkmoney.cashreg.listener;

import com.rbkmoney.cashreg.service.HandlerManager;
import com.rbkmoney.cashreg.service.exception.UnsuccessfulSaveException;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.machinegun.eventsink.SinkEvent;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;

@Slf4j
@RequiredArgsConstructor
public class InvoicingKafkaListener {

    @Value("${kafka.exception.sleep:1000}")
    private Integer exceptionSleep;

    private final HandlerManager handlerManager;
    private final MachineEventParser<EventPayload> parser;

    @KafkaListener(topics = "${kafka.topics.invoice.id}", containerFactory = "kafkaListenerContainerFactory")
    public void handle(SinkEvent sinkEvent, Acknowledgment ack) {
        log.debug("Reading sinkEvent, sourceId:{}, eventId:{}", sinkEvent.getEvent().getSourceId(), sinkEvent.getEvent().getEventId());
        EventPayload payload = parser.parse(sinkEvent.getEvent());
        if (payload.isSetInvoiceChanges()) {
            payload.getInvoiceChanges()
                    .forEach(invoiceChange -> handle(sinkEvent, invoiceChange));
        }
        ack.acknowledge();
    }

    private void handle(SinkEvent sinkEvent, InvoiceChange invoiceChange) {
        try {
            handlerManager.getHandler(invoiceChange)
                    .ifPresentOrElse(handler -> handler.handle(invoiceChange, sinkEvent.getEvent().getSourceId()),
                            () -> log.debug("Handler for invoiceChange {} wasn't found (machineEvent {})", invoiceChange, sinkEvent.getEvent()));
        } catch (UnsuccessfulSaveException ex) {
            //DAO save exception
            log.warn("Entity wasn't saved for '{}'", invoiceChange, ex);
            sleepAndLog(invoiceChange);
            throw ex;
        } catch (Exception ex) {
            log.error("Failed to handle invoice change, invoiceChange='{}'", invoiceChange, ex);
            sleepAndLog(invoiceChange);
            throw ex;
        }
    }

    private void sleepAndLog(InvoiceChange invoiceChange) {
        try {
            Thread.sleep(exceptionSleep);
        } catch (InterruptedException e) {
            log.error("Interrupted while sleeping when handle invoice change, invoiceChange='{}'", invoiceChange);
            Thread.currentThread().interrupt();
        }
    }
}