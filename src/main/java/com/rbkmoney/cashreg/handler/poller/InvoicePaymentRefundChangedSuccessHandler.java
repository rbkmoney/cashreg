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
import com.rbkmoney.damsel.domain.Invoice;
import com.rbkmoney.damsel.payment_processing.InvoiceChange;
import com.rbkmoney.damsel.payment_processing.InvoicePaymentRefundChange;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import static com.rbkmoney.cashreg.utils.cart.CartUtils.prepareCashRegDelivery;

@Slf4j
@Component
@RequiredArgsConstructor
public class InvoicePaymentRefundChangedSuccessHandler implements PollingEventHandler {

    private final String handlerEvent = this.getClass().getSimpleName();

    private final InvoicePayerService invoicePayerService;
    private final PaymentService paymentService;
    private final RefundService refundService;
    private final CashRegDeliveryService cashRegDeliveryService;

    public void handle(InvoiceChange ic, MachineEvent event, String sourceId) {
        Invoice invoice = ic.getInvoiceCreated().getInvoice();
        String invoiceId = invoice.getId();

        InvoicePaymentRefundChange invoicePaymentRefundChange = ic.getInvoicePaymentChange().getPayload().getInvoicePaymentRefundChange();
        String paymentId = ic.getInvoicePaymentChange().getId();
        String refundId = invoicePaymentRefundChange.getId();
        log.info("Start {} with  payment {}.{}, refundId {}", handlerEvent, invoiceId, paymentId, refundId);

        InvoicePayer invoicePayer = invoicePayerService.findByInvoiceIdAndPaymentIdAndRefundId(invoiceId, paymentId, refundId);
        if (invoicePayer == null) {
            log.debug("InvoicePayer is missing, invoiceId {}", invoiceId);
            return;
        }

        // TODO: check count refunds and amount
        /**
         * отправляем чек возврата-прихода c корзиной инвойса/предпоследнего успешного рефанда
         *
         * отправляем чек прихода с корзиной из события refund created
         */


        Refund refund = invoicePayer.getPayment().getRefund();
        refund.setStatus(RefundStatus.SUCCEEDED);
        refund.setPreviousCart(invoicePayer.getExchangeCart());

        Refund refundDB = refundService.save(refund);

        if (refundDB == null) {
            log.debug("Couldn't save Refund. payment {}.{}", invoiceId, paymentId);
            return;
        } else {
            log.debug("Saved Refund. payment {}.{}", invoiceId, paymentId);
        }

        Payment payment = invoicePayer.getPayment();
        payment.setRefund(refundDB);

        Payment paymentDB = paymentService.save(payment);
        if (paymentDB == null) {
            log.debug("Couldn't save Payment. payment {}.{}", invoiceId, paymentId);
            return;
        } else {
            log.debug("Saved Payment. payment {}.{}", invoiceId, paymentId);
        }

        // Полный возврат корзины
        CashRegDelivery cashRegDelivery = prepareCashRegDelivery(
                invoicePayer, paymentDB, refundDB,
                CashRegTypeOperation.REFUND_DEBIT,
                CashRegStatus.READY,
                CartState.FULL
        );

        CashRegDelivery cashRegDeliveryDB = cashRegDeliveryService.save(cashRegDelivery);
        if (cashRegDeliveryDB == null) {
            log.debug("{}: couldn't save CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
            return;
        } else {
            log.debug("{}: saved CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
        }

        // Если корзины нет, то нет необходимости отправлять второй чек
        if (refundDB.getCart() != null && !refundDB.getCart().isEmpty()) {
            cashRegDelivery = prepareCashRegDelivery(
                    invoicePayer, paymentDB, refundDB,
                    CashRegTypeOperation.DEBIT,
                    CashRegStatus.READY,
                    CartState.PARTIAL
            );

            cashRegDeliveryDB = cashRegDeliveryService.save(cashRegDelivery);
            if (cashRegDeliveryDB == null) {
                log.debug("{}: couldn't save CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
                return;
            } else {
                log.debug("{}: saved CashRegDelivery. payment {}.{}", handlerEvent, invoiceId, paymentId);
            }
        }

        log.info("End {} with payment {}.{}, refundId {}", handlerEvent, invoiceId, paymentId, refundId);
    }

    @Override
    public ChangeType getChangeType() {
        return ChangeType.INVOICE_PAYMENT_REFUND_CHANGED_SUCCEEDED;
    }

}
