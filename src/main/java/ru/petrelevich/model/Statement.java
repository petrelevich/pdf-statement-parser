package ru.petrelevich.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Value;
import lombok.experimental.Accessors;

@Value
@Builder
@Accessors(fluent = true)
public class Statement {
    String fio;
    String contract;
    String account;
    LocalDate from;
    LocalDate to;
    BigDecimal balanceInitial;
    BigDecimal balanceFinal;
    BigDecimal totalIncome;
    BigDecimal totalOutcome;
    List<AccountEntry> entries;
}
