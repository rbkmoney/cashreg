package com.rbkmoney.cashreg.utils.cashreg.creators;

import com.rbkmoney.damsel.cashreg.type.*;

public class TypeCreators {

    public static Type createDebit() {
        return Type.debit(new Debit());
    }

    public static Type createCredit() {
        return Type.credit(new Credit());
    }

    public static Type createRefundDebit() {
        return Type.refund_debit(new RefundDebit());
    }

    public static Type createRefundCredit() {
        return Type.refund_credit(new RefundCredit());
    }

}
