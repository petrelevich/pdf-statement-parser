package ru.petrelevich.parser;

import java.math.BigDecimal;

public class SumParser {
    public BigDecimal parse(String sumStr) {
        try {
            return new BigDecimal(sumStr
                    .replace("RUB", "")
                    .replace(" ", "")
                    .replace(",", "")
            );
        } catch (Exception ex) {
            throw new NumberFormatException("can't parse string:" + sumStr);
        }
    }
}
