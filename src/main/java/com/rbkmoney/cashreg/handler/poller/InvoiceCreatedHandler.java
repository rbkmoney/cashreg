package com.rbkmoney.cashreg.handler.poller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.Account;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.cashreg.AccountService;
import com.rbkmoney.cashreg.service.cashreg.InvoicePayerService;
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.domain.InvoiceLine;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.Charset;
import java.util.List;

import static com.rbkmoney.cashreg.utils.cart.CartUtils.prepareCartInvoiceLine;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoiceCreatedHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final AccountService accountService;
    private final InvoicePayerService invoicePayerService;
    private final ObjectMapper objectMapper;


    public void handle(InvoiceChange ic, String sourceId) {
        Invoice invoice = ic.getInvoiceCreated().getInvoice();
        String invoiceId = invoice.getId();
        log.info("Start {}: invoice_id {}", handlerEvent, invoiceId);

        if (invoicePayerService.findByInvoiceId(invoiceId) != null) {
            log.info("Duplicate found, invoice: {}", invoiceId);
            return;
        }

        Account account = accountService.findByMerchantIdAndShopId(invoice.getOwnerId(), invoice.getShopId());
        if (account == null) {
            log.debug("{}: Account is missing", handlerEvent);
            return;
        }

        String context = new String(invoice.getContext().getData(), Charset.forName("UTF-8"));
        InvoicePayer.InvoicePayerBuilder invoicePayer = InvoicePayer.builder()
                .account(account)
                .invoiceId(invoiceId)
                .currency(invoice.getCost().getCurrency().getSymbolicCode())
                .metadata(context)
                .amount(invoice.getCost().getAmount());

        if (invoice.getDetails().isSetCart()) {
            List<InvoiceLine> lineList = invoice.getDetails().getCart().getLines();
            if (!lineList.isEmpty()) {
                try {
                    String lines = prepareCartInvoiceLine(objectMapper, lineList);
                    invoicePayer.cart(lines);
                    invoicePayer.exchangeCart(lines);
                } catch (JsonProcessingException e) {
                    log.debug("{}: InvoicePayer cart lines is empty", handlerEvent);
                }
            }
        }

        invoicePayerService.save(invoicePayer.build());
        log.info("End {}: invoice_id {}", handlerEvent, invoice.getId());
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_CREATED;
    }

}
