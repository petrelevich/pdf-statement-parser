package ru.petrelevich.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class AccountEntry {
    LocalDateTime dateOperation;
    LocalDate dateProcessing;
    BigDecimal sumOperationCurrency;
    BigDecimal sumIncome;
    BigDecimal sumOutcome;
    BigDecimal sumFee;
    String category;
    String comment;
}
