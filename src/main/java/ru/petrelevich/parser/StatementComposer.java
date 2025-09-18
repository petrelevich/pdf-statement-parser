package ru.petrelevich.parser;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.petrelevich.model.AccountEntry;
import ru.petrelevich.model.Statement;

public class StatementComposer {
    private static final Logger log = LoggerFactory.getLogger(StatementComposer.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private static final String MSG_PATTERN = "%s %s";
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private final Categorizer categorizer;
    private final SumParser sumParser;

    public StatementComposer(Categorizer categorizer, SumParser sumParser) {
        this.categorizer = categorizer;
        this.sumParser = sumParser;
    }

    public Statement compose(List<String> statementParts) {
        var period = statementParts.get(4).replace(" ", "").split("-");

        var entries = new ArrayList<AccountEntry>();
        var idx = 27;
        BigDecimal totalIncome = BigDecimal.ZERO;
        BigDecimal totalOutcome = BigDecimal.ZERO;
        try {
            while (idx + 8 < statementParts.size()) {
                if (isDate(statementParts, idx)) {
                    LocalDateTime dateOperation =
                            searchDateTimeOperation(statementParts, statementParts.get(idx), idx + 1);
                    var dateProcessingWithIndex =
                            searchDateProcessing(statementParts, dateOperation.getYear(), idx + 1);
                    LocalDate dateProcessing = dateProcessingWithIndex.dateProcessing;
                    BigDecimal operationCurrency = searchOperationCurrency(
                            statementParts, idx + 1, getDateIdx(statementParts, dateProcessingWithIndex.idx()));
                    BigDecimal fee = operationCurrency.compareTo(ZERO) == 0
                            ? searchFee(
                                    statementParts, idx + 1, getDateIdx(statementParts, dateProcessingWithIndex.idx()))
                            : ZERO;
                    String comment = searchComment(
                            statementParts, idx, getDateIdx(statementParts, dateProcessingWithIndex.idx()));
                    BigDecimal income = ZERO;
                    BigDecimal outcome = ZERO;

                    if (operationCurrency.compareTo(ZERO) == 0 && fee.compareTo(ZERO) != 0) {
                        operationCurrency = fee;
                    }

                    if (operationCurrency.compareTo(ZERO) < 0) {
                        outcome = operationCurrency.abs();
                    } else if (operationCurrency.compareTo(ZERO) > 0) {
                        income = operationCurrency.abs();
                    }

                    var entry = AccountEntry.builder()
                            .dateOperation(dateOperation)
                            .dateProcessing(dateProcessing)
                            .sumOperationCurrency(operationCurrency)
                            .sumIncome(income)
                            .sumOutcome(outcome)
                            .sumFee(fee)
                            .comment(comment)
                            .category(categorizer.getCategory(comment))
                            .build();
                    totalIncome = totalIncome.add(entry.sumIncome());
                    totalOutcome = totalOutcome.add(entry.sumOutcome());
                    entries.add(entry);

                    idx = dateProcessingWithIndex.idx;
                }
                idx++;
            }
        } catch (Exception ex) {
            throw new ParserException("parsing error, current idx:" + idx, ex);
        }
        var statement = Statement.builder()
                .fio(statementParts.get(0))
                .contract(statementParts.get(2))
                .account(statementParts.get(4))
                .from(LocalDate.parse(period[0], DATE_FORMATTER))
                .to(LocalDate.parse(period[1], DATE_FORMATTER))
                .balanceInitial(sumParser.parse(statementParts.get(7)))
                .balanceFinal(sumParser.parse(statementParts.get(11)))
                .totalIncome(sumParser.parse(statementParts.get(9)))
                .totalOutcome(sumParser.parse(statementParts.get(13)))
                .entries(entries)
                .build();

        check(statement, totalIncome, totalOutcome);
        return statement;
    }

    private int getDateIdx(List<String> statementParts, int idxCurrent) {
        for (int idx = idxCurrent + 1; idx < statementParts.size(); idx++) {
            if (isDate(statementParts, idx)) {
                return idx;
            }
        }
        return statementParts.size();
    }

    private LocalDateTime searchDateTimeOperation(List<String> statementParts, String dateOperationStr, int beginIdx) {
        for (int idx = beginIdx; idx < beginIdx + 10 && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            try {
                return LocalDateTime.parse(String.format(MSG_PATTERN, dateOperationStr, part), DATE_TIME_FORMATTER);
            } catch (Exception ex) {
                // skip
            }
        }
        throw new IllegalStateException("DateOperation not found, beginIdx=" + beginIdx);
    }

    private BigDecimal searchFee(List<String> statementParts, int beginIdx, int endIdx) {
        List<String> suspected = new ArrayList<>();

        for (int idx = beginIdx; idx < endIdx && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            if (part.length() < 10 && !part.contains("RUB") && !part.contains(":")) {
                suspected.add(part);
            }
        }

        var part1 = "";
        var part2 = "";
        for (var s : suspected) {
            if (s.contains(".")) {
                part2 = s;
            } else if (s.length() > 1) {
                part1 = s;
            }
        }
        return sumParser.parse(part1 + part2);
    }

    @SuppressWarnings("java:S3776")
    private BigDecimal searchOperationCurrency(List<String> statementParts, int beginIdx, int endIdx) {
        var minus = "";

        List<String> suspected = new ArrayList<>();
        Pattern pattern1 = Pattern.compile("-[\\d]+");
        Pattern pattern2 = Pattern.compile("\\.[\\d]+");

        for (int idx = beginIdx; idx < endIdx && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            if ("-".equals(part)) {
                minus = part;
            }
            if (part.contains("RUB")) {
                suspected.add(part);
            } else {
                Matcher matcher1 = pattern1.matcher(part);
                Matcher matcher2 = pattern2.matcher(part);
                if (matcher1.find() || matcher2.find()) {
                    suspected.add(part);
                }
            }
        }
        var sum = "";
        var part1 = "";
        var part2 = "";
        for (var s : suspected) {
            try {
                if (s.contains("RUB")) {
                    part2 = s;
                } else if (s.contains("-") || s.contains(",")) {
                    part1 = s;
                }
                if (s.contains(".") && s.contains("RUB")) {
                    part1 = "";
                    part2 = s;
                }
                sum = part1 + part2;
                if (sum.contains("RUB") && sum.contains(".") && !sum.contains(".0000") && !sum.startsWith(".")) {
                    return sumParser.parse(minus + sum);
                }
            } catch (Exception ex) {
                //
            }
        }
        throw new IllegalStateException("OperationCurrency not found, beginIdx=" + beginIdx);
    }

    @SuppressWarnings("java:S3776")
    private String searchComment(List<String> statementParts, int beginIdx, int endIdx) {
        var comment1 = "";
        var comment2 = "";
        var comment3 = "";
        var comment4 = "";
        for (int idx = beginIdx; idx < endIdx && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            if (part.startsWith("Опл")
                    || part.startsWith("Пер")
                    || part.startsWith("Зачисление")
                    || part.startsWith("Внес")) {
                if (comment1.isEmpty()) {
                    comment1 = part;
                }
            } else if (part.contains("слуг")
                    || part.contains("СБП")
                    || part.contains("уг.")
                    || part.contains("средств")
                    || part.contains("обслужи")
                    || part.contains("счетами")) {
                if (comment2.isEmpty()) {
                    comment2 = part;
                }
            } else if (part.contains("*") || part.contains("РЕ")) {
                if (comment3.isEmpty()) {
                    comment3 = part;
                }
            } else if (part.contains("ИЧ") && comment4.isEmpty()) {
                comment4 = part;
            }
        }
        return comment1 + comment2 + comment3 + comment4;
    }

    @SuppressWarnings("java:S3776")
    private DateProcessingWithIndex searchDateProcessing(List<String> statementParts, int year, int beginIdx) {
        for (int idx = beginIdx; idx < beginIdx + 10 && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            try {
                return new DateProcessingWithIndex(LocalDate.parse(part, DATE_FORMATTER), idx);
            } catch (Exception ex) {
                // skip
            }
        }

        var foundYear = false;
        var yearStr = String.valueOf(year);
        for (int idx = beginIdx; idx < beginIdx + 10 && idx < statementParts.size() && !foundYear; idx++) {
            foundYear = statementParts.get(idx).equals(yearStr);
        }
        if (foundYear) {
            for (int idx = beginIdx; idx < beginIdx + 10 && idx < statementParts.size(); idx++) {
                var part = statementParts.get(idx);
                try {
                    var data = String.format("%s%s", part, yearStr);
                    return new DateProcessingWithIndex(LocalDate.parse(data, DATE_FORMATTER), idx);
                } catch (Exception ex) {
                    // skip
                }
            }
        }

        List<String> suspected = new ArrayList<>();
        int dateProcessingWithIndex = 0;
        for (int idx = beginIdx; idx < beginIdx + 10 && idx < statementParts.size(); idx++) {
            var part = statementParts.get(idx);
            if (!part.contains("RUB")
                    && !part.contains("Оплата")
                    && !part.contains(",")
                    && !part.contains(":")
                    && !part.contains("00")) {
                suspected.add(part);
                dateProcessingWithIndex = idx;
            }
        }

        for (var idxFirst = 0; idxFirst < suspected.size(); idxFirst++) {
            for (String s : suspected) {
                try {
                    var data = String.format("%s%s", s, suspected.get(idxFirst));
                    return new DateProcessingWithIndex(LocalDate.parse(data, DATE_FORMATTER), dateProcessingWithIndex);
                } catch (Exception ex) {
                    // skip
                }
            }
        }

        for (var idxFirst = 0; idxFirst < suspected.size(); idxFirst++) {
            for (String s : suspected) {
                try {
                    var data = String.format("%s%s", suspected.get(idxFirst), s);
                    return new DateProcessingWithIndex(LocalDate.parse(data, DATE_FORMATTER), dateProcessingWithIndex);
                } catch (Exception ex) {
                    // skip
                }
            }
        }

        throw new IllegalStateException("DateProcessing not found, beginIdx=" + beginIdx);
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

    private boolean isDate(List<String> statementParts, int idx) {
        try {
            LocalDate.parse(statementParts.get(idx), DATE_FORMATTER);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    private record DateProcessingWithIndex(LocalDate dateProcessing, int idx) {}
}
