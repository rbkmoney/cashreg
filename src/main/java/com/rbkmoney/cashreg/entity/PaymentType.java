package com.rbkmoney.cashreg.entity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.persistence.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@Entity(name = "payment_type")
@Table(name = "payment_type")
public class PaymentType {

    public static final String BANK_CARD = "bank_card";
    public static final String PAYMENT_TERMINAL = "payment_terminal";
    public static final String DIGITAL_WALLET = "digital_wallet";
    public static final String CRYPTO_WALLET = "crypto_wallet";


    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_payment_type_id_gen"
    )
    @SequenceGenerator(
            name = "seq_payment_type_id_gen",
            sequenceName = "seq_payment_type_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Type
     * e.g. terminal, card, wallet and etc
     */
    @Column(name = "type")
    private String type;


    public PaymentType(String type) {
        this.type = type;
    }

    public PaymentType(Long id, String type) {
        this(type);
        this.id = id;
    }

}
