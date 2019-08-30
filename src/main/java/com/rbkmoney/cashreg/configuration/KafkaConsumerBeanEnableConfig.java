package com.rbkmoney.cashreg.configuration;

import com.rbkmoney.cashreg.listener.InvoicingKafkaListener;
import com.rbkmoney.cashreg.service.HandlerManager;
import com.rbkmoney.damsel.payment_processing.EventPayload;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;

@Configuration
@EnableKafka
public class KafkaConsumerBeanEnableConfig {

    @Bean
    @ConditionalOnProperty(value = "kafka.topics.invoice.enabled", havingValue = "true")
    public InvoicingKafkaListener invoicingEventsKafkaListener(
            HandlerManager handlerManager,
            MachineEventParser<EventPayload> parser
    ) {
        return new InvoicingKafkaListener(handlerManager, parser);
    }
}