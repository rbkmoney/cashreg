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
@Entity(name = "cashbox")
@Table(name = "cashbox")
public class CashBox {

    @Id
    @GeneratedValue(
            strategy = GenerationType.SEQUENCE,
            generator = "seq_cashbox_id_gen"
    )
    @SequenceGenerator(
            name = "seq_cashbox_id_gen",
            sequenceName = "seq_cashbox_id",
            allocationSize = 1
    )
    private Long id;

    @Column(name = "name")
    private String name;

    @Column(name = "url")
    private String url;

    @ToString.Exclude
    @Column(name = "settings")
    private String settings;

}
