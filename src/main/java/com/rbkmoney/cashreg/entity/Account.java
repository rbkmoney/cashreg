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
@Entity(name = "account")
@Table(name = "account")
public class Account {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_account_id_gen"
    )
    @SequenceGenerator(
            name = "seq_account_id_gen",
            sequenceName = "seq_account_id",
            allocationSize = 1
    )
    private Long id;

    /**
     * Merchant ID
     * e.g. r72a669a5-a63a-4f1b-9899-58bab2896dcb
     */
    @Column(name = "merchant_id")
    private String merchantId;

    /**
     * Shop ID
     * e.g. TEST
     */
    @Column(name = "shop_Id")
    private String shopId;

    @ManyToOne
    @JoinColumn(name = "cashbox_id")
    private CashBox cashbox;

    /**
     * Is active
     */
    @Column(name = "is_active")
    private Boolean isActive = Boolean.TRUE;

}
