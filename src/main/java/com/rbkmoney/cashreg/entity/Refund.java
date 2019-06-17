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
@Entity(name = "refund")
@Table(name = "refund")
public class Refund {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_refund_id_gen"
    )
    @SequenceGenerator(
            name = "seq_refund_id_gen",
            sequenceName = "seq_refund_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Refund ID
     * e.g. sQoyFthoX2
     */
    @Column(name = "refund_id")
    private String refundId;

    /**
     * Amount
     * e.g. 10000 (minor units)
     */
    @Column(name = "amount")
    private Long amount;

    /**
     * Status
     * e.g. pending
     */
    @Column(name = "status")
    private String status;

    /**
     * Cart
     * e.g. {"test":"test"}
     */
    @Column(name = "cart")
    private String cart;

    @Column(name = "previous_cart")
    private String previousCart;

}
