package com.rbkmoney.cashreg.entity;

import lombok.*;

import javax.persistence.*;
import java.util.Arrays;

@Getter
@ToString
@AllArgsConstructor
public enum PaymentType {

    BANK_CARD("bank_card"),
    PAYMENT_TERMINAL("payment_terminal"),
    DIGITAL_WALLET("digital_wallet"),
    CRYPTO_WALLET("crypto_wallet");

    @Column(name = "type")
    private String type;

    public static PaymentType findByType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.getType().equals(type))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
