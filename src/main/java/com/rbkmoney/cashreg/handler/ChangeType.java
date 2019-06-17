package com.rbkmoney.cashreg.handler;

import com.rbkmoney.geck.filter.Condition;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;

public enum ChangeType {

    INVOICE_CREATED("invoice_created", new IsNullCondition().not()),
    INVOICE_STATUS_CHANGED("invoice_status_changed", new IsNullCondition().not()),
    INVOICE_STATUS_CHANGED_PAID("invoice_status_changed.status.paid", new IsNullCondition().not()),

    INVOICE_PAYMENT_STARTED("invoice_payment_change.payload.invoice_payment_started", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_status_changed", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_PENDING("invoice_payment_change.payload.invoice_payment_status_changed.status.pending", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_PROCESSED("invoice_payment_change.payload.invoice_payment_status_changed.status.processed", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_CAPTURED("invoice_payment_change.payload.invoice_payment_status_changed.status.captured", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_CANCELLED("invoice_payment_change.payload.invoice_payment_status_changed.status.cancelled", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_REFUNDED("invoice_payment_change.payload.invoice_payment_status_changed.status.refunded", new IsNullCondition().not()),
    INVOICE_PAYMENT_STATUS_CHANGED_FAILED("invoice_payment_change.payload.invoice_payment_status_changed.status.failed", new IsNullCondition().not()),

    INVOICE_PAYMENT_REFUND_CREATED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_created", new IsNullCondition().not()),
    INVOICE_PAYMENT_REFUND_STATUS_CHANGED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed", new IsNullCondition().not()),
    INVOICE_PAYMENT_REFUND_CHANGED_SUCCEEDED("invoice_payment_change.payload.invoice_payment_refund_change.payload.invoice_payment_refund_status_changed.status.succeeded", new IsNullCondition().not());

    Filter filter;

    ChangeType(String path, Condition... conditions) {
        this.filter = new PathConditionFilter(new PathConditionRule(path, conditions));
    }

    public Filter getFilter() {
        return filter;
    }

}
