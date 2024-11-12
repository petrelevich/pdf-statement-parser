package ru.petrelevich.parser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.petrelevich.model.AccountEntry;
import ru.petrelevich.model.Statement;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class StatementComposer {
    private static final Logger log = LoggerFactory.getLogger(StatementComposer.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final Categorizer categorizer;
    private final SumParser sumParser;

    public StatementComposer(Categorizer categorizer, SumParser sumParser) {
        this.categorizer = categorizer;
        this.sumParser = sumParser;
    }

    public Statement compose(List<String> statementParts) {
        var period = statementParts.get(6).replace(" ", "").split("-");

        var entries = new ArrayList<AccountEntry>();
        var idx = 29;
        int currentIdx = idx;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalOutcome = BigDecimal.ZERO;
        try {
            while (idx + 8 < statementParts.size()) {
                currentIdx = idx;
                var eIdx = idx;
                String comment;
                if (isDateTime(statementParts, idx)) {
                    if (isNextTime(statementParts, idx)) {
                        comment = String.format("%s %s", statementParts.get(eIdx + 7), statementParts.get(eIdx + 8));
                    } else {
                        comment = String.format("%s %s %s", statementParts.get(eIdx + 1), statementParts.get(eIdx + 8), statementParts.get(eIdx + 9));
                        eIdx++;
                    }
                    var dateOperationStr = String.format("%s %s", statementParts.get(currentIdx), statementParts.get(eIdx + 1));
                    var dateProcessingStr = statementParts.get(eIdx + 2);
                    var income = sumParser.parse(statementParts.get(eIdx + 4));
                    var fee = sumParser.parse(statementParts.get(eIdx + 6));
                    if (income.equals(ZERO) && !fee.equals(ZERO) && comment.contains("Зачисление кешбэка по программе лояльности")) {
                        income = fee;
                    }
                    var entry = AccountEntry.builder()
                            .dateOperation(LocalDateTime.parse(dateOperationStr, DATE_TIME_FORMATTER))
                            .dateProcessing(LocalDate.parse(dateProcessingStr, DATE_FORMATTER))
                            .sumOperationCurrency(sumParser.parse(statementParts.get(eIdx + 3)))
                            .sumIncome(income)
                            .sumOutcome(sumParser.parse(statementParts.get(eIdx + 5)))
                            .sumFee(fee)
                            .comment(comment)
                            .category(categorizer.getCategory(comment))
                            .build();
                    totalIncome = totalIncome.add(entry.sumIncome());
                    totalOutcome = totalOutcome.add(entry.sumOutcome());
                    entries.add(entry);
                    idx += 8;
                } else {
                    idx++;
                }
            }
        } catch (Exception ex) {
            throw new ParserException("parsing error, current idx:" + currentIdx, ex);
        }
        var statement = Statement.builder()
                .fio(statementParts.get(0))
                .contract(statementParts.get(2))
                .account(statementParts.get(4))

                .from(LocalDate.parse(period[0], DATE_FORMATTER))
                .to(LocalDate.parse(period[1], DATE_FORMATTER))

                .balanceInitial(sumParser.parse(statementParts.get(9)))
                .balanceFinal(sumParser.parse(statementParts.get(13)))
                .totalIncome(sumParser.parse(statementParts.get(11)))
                .totalOutcome(sumParser.parse(statementParts.get(15)))
                .entries(entries)
                .build();

        check(statement, totalIncome, totalOutcome);
        return statement;
    }

    private void check(Statement stm, BigDecimal totalIncome, BigDecimal totalOutcome) {
        var balanceFinalExpected = stm.balanceInitial().add(stm.totalIncome()).min(stm.totalOutcome());
        if (!stm.balanceFinal().equals(balanceFinalExpected)) {
            log.warn("Check failed. balance. Initial:{}. expected:{}", stm.balanceInitial(), balanceFinalExpected);
        }
        if (!stm.totalIncome().equals(totalIncome)) {
            log.warn("Check failed. totalIncome. header:{}. entries:{}", stm.totalIncome(), totalIncome);
        }
        if (!stm.totalOutcome().equals(totalOutcome)) {
            log.warn("Check failed. totalOutcome. header:{}. entries:{}", stm.totalOutcome(), totalOutcome);
        }
    }

    private boolean isNextTime(List<String> statementParts, int idx) {
        try {
            LocalDateTime.parse(String.format("%s %s", statementParts.get(idx), statementParts.get(idx + 1)), DATE_TIME_FORMATTER);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private boolean isDateTime(List<String> statementParts, int idx) {
        try {
            LocalDate.parse(statementParts.get(idx), DATE_FORMATTER);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

}
