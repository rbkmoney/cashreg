package com.rbkmoney.cashreg.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;
import java.util.Arrays;

@Getter
@ToString
@AllArgsConstructor
public enum ContactType {

    EMAIL("email"),
    PHONE_NUMBER("phone_number");

    @Column(name = "type")
    private String type;

    public static ContactType findByType(String type) {
        return Arrays.stream(values())
                .filter(value -> value.getType().equals(type))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }

}
