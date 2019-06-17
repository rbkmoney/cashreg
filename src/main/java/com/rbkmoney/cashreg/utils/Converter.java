package com.rbkmoney.cashreg.utils;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.cashreg.model.InvoiceLine;
import com.rbkmoney.cashreg.model.Settings;
import com.rbkmoney.file.storage.base.Cash;
import com.rbkmoney.file.storage.base.Currency;
import com.rbkmoney.kkt.provider.Cart;
import com.rbkmoney.kkt.provider.ItemsLine;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Converter {

    private static ObjectMapper objectMapper;

    static {
        objectMapper = new ObjectMapper();
        objectMapper.configure(JsonParser.Feature.ALLOW_UNQUOTED_CONTROL_CHARS, true);
    }

    public static Settings extractSettings(String settings) {
        try {
            return objectMapper.readValue(settings, Settings.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Cart getInvoiceLines(String cart) {
        try
        {
            List<InvoiceLine> lineList = (cart == null)
                ? Collections.emptyList()
                : objectMapper.readValue(cart, objectMapper.getTypeFactory().constructCollectionType(List.class, InvoiceLine.class));

            Cart prepareCart = new Cart();
            List<ItemsLine> itemsLines = new ArrayList<>();

            lineList.forEach(invoiceLine -> {

                ItemsLine itemLine = new ItemsLine();
                Cash cash = new Cash();
                cash.setAmount(invoiceLine.getPrice().longValue());

                Currency currency = new Currency();
                currency.setExponent((short) 2);
                currency.setName("Rubles");
                currency.setNumericCode((short) 643);
                currency.setSymbolicCode("RUB");

                cash.setCurrency(currency);

                itemLine.setPrice(cash);
                itemLine.setProduct(invoiceLine.getProduct());
                itemLine.setQuantity(invoiceLine.getQuantity());
                itemLine.setTax(invoiceLine.getMetadata().get("TaxMode"));
                itemsLines.add(itemLine);
            });

            prepareCart.setLines(itemsLines);
            return prepareCart;
        }catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }


}
