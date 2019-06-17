package com.rbkmoney.cashreg.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

@ToString
@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Settings implements Serializable {

    @JsonProperty(value = "url")
    private String url;

    @JsonProperty(value = "login")
    private String login;

    @JsonProperty(value = "pass")
    private String pass;

    @JsonProperty(value = "private_key")
    private String privateKey;

    @JsonProperty(value = "key")
    private String key;

    @JsonProperty(value = "group")
    private String group;

    @JsonProperty(value = "client_id")
    private String clientId;

    @JsonProperty(value = "tax_id")
    private String taxId;

    @JsonProperty(value = "tax_mode")
    private String taxMode;

    @JsonProperty(value = "inn")
    private String inn;

    @JsonProperty(value = "company_name")
    private String companyName;

    @JsonProperty(value = "company_address")
    private String companyAddress;

    @JsonProperty(value = "company_email")
    private String companyEmail;

    @JsonProperty(value = "payment_type")
    private String paymentType;

    @JsonProperty(value = "payment_method")
    private String paymentMethod;

    @JsonProperty(value = "payment_object")
    private String paymentObject;

    public Map<String, String> prepareMap() {
        Map<String, String> map = new HashMap<>();
        for (Field field : Settings.class.getDeclaredFields()) {
            // Skip this if you intend to access to public fields only
            if (!field.canAccess(field)) {
                field.setAccessible(true);
            }
            map.put(field.getName(), field.toString());
        }

        return map;
    }

}
