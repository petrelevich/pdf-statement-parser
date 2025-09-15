package ru.petrelevich.parser;

import java.math.BigDecimal;

public class SumParser {
    public BigDecimal parse(String sumStr) {
        try {
            var sumStrNormalized = sumStr.replace("RUB", "").replace(" ", "");
            if (sumStrNormalized.contains(".") && sumStrNormalized.contains(",")) {
                sumStrNormalized = sumStrNormalized.replace(",", "");
            } else if (sumStrNormalized.contains(",")) {
                sumStrNormalized = sumStrNormalized.replace(",", ".");
            }
            return new BigDecimal(sumStrNormalized);
        } catch (Exception ex) {
            throw new NumberFormatException("can't parse string:" + sumStr);
        }
    }
}
