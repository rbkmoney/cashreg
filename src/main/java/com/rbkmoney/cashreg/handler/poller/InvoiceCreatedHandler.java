package com.rbkmoney.cashreg.handler.poller;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.entity.Account;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.AccountService;
import com.rbkmoney.cashreg.service.InvoicePayerService;
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

        InvoicePayer invoicePayer = new InvoicePayer();
        invoicePayer.setAccount(account);
        invoicePayer.setInvoiceId(invoiceId);

        invoicePayer.setCurrency(invoice.getCost().getCurrency().getSymbolicCode());

        String context = new String(invoice.getContext().getData(), Charset.forName("UTF-8"));
        invoicePayer.setMetadata(context);
        invoicePayer.setAmount(invoice.getCost().getAmount());


        if (invoice.getDetails().isSetCart()) {
            List<InvoiceLine> lineList = invoice.getDetails().getCart().getLines();
            if (!lineList.isEmpty()) {
                try {
                    String lines = prepareCartInvoiceLine(objectMapper, lineList);
                    invoicePayer.setCart(lines);
                    invoicePayer.setExchangeCart(lines);
                } catch (JsonProcessingException e) {
                    log.debug("{}: InvoicePayer cart lines is empty", handlerEvent);
                }
            }
        }

        invoicePayerService.save(invoicePayer);

        log.info("End {}: invoice_id {}", handlerEvent, invoice.getId());
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_CREATED;
    }

}
