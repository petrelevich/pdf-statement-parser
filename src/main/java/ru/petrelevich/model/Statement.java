package ru.petrelevich.model;

import lombok.Builder;
import lombok.Data;
import lombok.Value;
import lombok.experimental.Accessors;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

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
