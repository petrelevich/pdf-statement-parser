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

    private final Categorizer categorizer;

    public StatementComposer(Categorizer categorizer) {
        this.categorizer = categorizer;
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
                if (isNextTime(statementParts, idx)) {
                    comment = String.format("%s %s", statementParts.get(eIdx + 7), statementParts.get(eIdx + 8));
                    idx += 9;
                } else {
                    comment = String.format("%s %s %s", statementParts.get(eIdx + 1), statementParts.get(eIdx + 8), statementParts.get(eIdx + 9));
                    eIdx++;
                    idx += 10;
                }
                var entry = AccountEntry.builder()
                        .dateOperation(LocalDateTime.parse(String.format("%s %s", statementParts.get(currentIdx), statementParts.get(eIdx + 1)), DATE_TIME_FORMATTER))
                        .dateProcessing(LocalDate.parse(statementParts.get(eIdx + 2), DATE_FORMATTER))
                        .sumOperationCurrency(parseSum(statementParts.get(eIdx + 3)))
                        .sumIncome(parseSum(statementParts.get(eIdx + 4)))
                        .sumOutcome(parseSum(statementParts.get(eIdx + 5)))
                        .sumFee(parseSum(statementParts.get(eIdx + 6)))
                        .comment(comment)
                        .category(categorizer.getCategory(comment))
                        .build();
                totalIncome = totalIncome.add(entry.sumIncome());
                totalOutcome = totalOutcome.add(entry.sumOutcome());
                entries.add(entry);
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

                .balanceInitial(parseSum(statementParts.get(9)))
                .balanceFinal(parseSum(statementParts.get(13)))
                .totalIncome(parseSum(statementParts.get(11)))
                .totalOutcome(parseSum(statementParts.get(15)))
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

    private BigDecimal parseSum(String sumStr) {
        return new BigDecimal(sumStr
                .replace("RUB", "")
                .replace(" ", "")
                .replace(",", ".")
        );
    }
}
