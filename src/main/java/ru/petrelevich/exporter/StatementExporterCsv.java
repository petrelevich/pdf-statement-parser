package ru.petrelevich.exporter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.petrelevich.model.Statement;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static java.nio.file.StandardOpenOption.APPEND;

public class StatementExporterCsv implements StatementExporter {
    private static final Logger log = LoggerFactory.getLogger(StatementExporterCsv.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    private final String fileName;

    public StatementExporterCsv(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public void export(Statement statement) {
        var header = "Дата и время операции" +
                ";" +
                "Дата обработки банком" +
                ";" +
                "Сумма операции в валюте операции" +
                ";" +
                "Сумма в валюте счета, приход" +
                ";" +
                "Сумма в валюте счета, расход" +
                ";" +
                "Комиссия" +
                ";" +
                "Категория" +
                ";" +
                "Описание операции";

        var entryLines = statement.entries().stream().map(entry ->
                DATE_TIME_FORMATTER.format(entry.dateOperation()) +
                        ";" +
                        DATE_FORMATTER.format(entry.dateProcessing()) +
                        ";" +
                        entry.sumOperationCurrency() +
                        ";" +
                        entry.sumIncome() +
                        ";" +
                        entry.sumOutcome() +
                        ";" +
                        entry.sumFee() +
                        ";" +
                        entry.category() +
                        ";" +
                        entry.comment()).toList();

        try {
            Files.write(Paths.get(fileName), List.of(header));
            Files.write(Paths.get(fileName), entryLines, APPEND);
        } catch (Exception ex) {
            throw new ExporterException("can't export to the file:" + fileName, ex);
        }

        log.info("saved to file:{}", fileName);
    }
}
