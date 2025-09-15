package ru.petrelevich;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.petrelevich.config.ConfigProviderYaml;
import ru.petrelevich.exporter.StatementExporterCsv;
import ru.petrelevich.loader.StatementLoaderFile;
import ru.petrelevich.parser.Categorizer;
import ru.petrelevich.parser.PdfToText;
import ru.petrelevich.parser.StatementComposer;
import ru.petrelevich.parser.SumParser;

public class PdfStatementParser {
    private static final Logger log = LoggerFactory.getLogger(PdfStatementParser.class);

    public static void main(String[] args) {
        ObjectMapper mapper = new YAMLMapper();
        var configProvider = new ConfigProviderYaml("./config.yaml", mapper);
        var config = configProvider.get();

        var categorizer = new Categorizer(config.categories());

        var pdfToText = new PdfToText();
        var statementLoader = new StatementLoaderFile(config.pdfFile());
        var sumParser = new SumParser();
        var statementComposer = new StatementComposer(categorizer, sumParser);
        var statementExporterCsv = new StatementExporterCsv(config.csvFile());

        var content = statementLoader.getContent();
        var textParts = pdfToText.getTextParts(content);
        var statement = statementComposer.compose(textParts);
        statementExporterCsv.export(statement);
        log.info("{}", statement);
    }
}
