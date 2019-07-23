package com.rbkmoney.cashreg.handler.poller;

import com.rbkmoney.cashreg.entity.CashRegDelivery;
import com.rbkmoney.cashreg.entity.InvoicePayer;
import com.rbkmoney.cashreg.entity.Payment;
import com.rbkmoney.cashreg.entity.Refund;
import com.rbkmoney.cashreg.handler.ChangeType;
import com.rbkmoney.cashreg.service.CashRegDeliveryService;
import com.rbkmoney.cashreg.service.InvoicePayerService;
import com.rbkmoney.cashreg.service.PaymentService;
import com.rbkmoney.cashreg.service.RefundService;
import com.rbkmoney.cashreg.utils.constant.CartState;
import com.rbkmoney.cashreg.utils.constant.CashRegStatus;
import com.rbkmoney.cashreg.utils.constant.CashRegTypeOperation;
import com.rbkmoney.cashreg.utils.constant.RefundStatus;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundChangedSuccessHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final CashRegDeliveryService cashRegDeliveryService;

    public void handle(InvoiceChange ic, String sourceId) {
        String paymentId = ic.getInvoicePaymentChange().getId();
        String refundId = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange().getId();
        log.info("Start {} with  refund {}.{}.{}", handlerEvent, sourceId, paymentId, refundId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentIdAndRefundId(sourceId, paymentId, refundId);
        if (invoicePayer == null) {
            log.info("InvoicePayer is missing, refundId {}.{}.{}", sourceId, paymentId, refundId);
            return;
        }

        Refund refund = invoicePayer.getPayment().getRefund();
        if (!RefundStatus.CREATED.equals(refund.getStatus())) {
            log.info("Duplicate found, refund: {}.{}.{}", sourceId, paymentId, refundId);
            return;
        }

        refund.setStatus(RefundStatus.SUCCEEDED);
        refund.setPreviousCart(invoicePayer.getExchangeCart());
        refund.setCart(refund.getCart());
        Refund refundDB = refundService.save(refund);

        invoicePayer.setExchangeCart(refund.getCart());

        Payment payment = invoicePayer.getPayment();
        payment.setRefund(refundDB);
        Payment paymentDB = paymentService.save(payment);

        // Полный возврат корзины
        cashRegDeliveryService.save(CashRegDelivery.builder()
                .invoiceId(invoicePayer)
                .paymentId(paymentDB)
                .refundId(refundDB)
                .typeOperation(CashRegTypeOperation.REFUND_DEBIT)
                .cashregStatus(CashRegStatus.READY)
                .cartState(CartState.FULL).build()
        );


        // Если корзины нет, то нет необходимости отправлять второй чек
        if (refundDB.getCart() != null && !refundDB.getCart().isEmpty()) {
            cashRegDeliveryService.save(CashRegDelivery.builder()
                    .invoiceId(invoicePayer)
                    .paymentId(paymentDB)
                    .refundId(refundDB)
                    .typeOperation(CashRegTypeOperation.DEBIT)
                    .cashregStatus(CashRegStatus.READY)
                    .cartState(CartState.PARTIAL).build()
            );
        }

        log.info("End {} with refund {}.{}.{}", handlerEvent, sourceId, paymentId, refundId);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_CHANGED_SUCCEEDED;
    }

}
