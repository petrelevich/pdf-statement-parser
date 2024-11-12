package ru.petrelevich.parser;


import org.apache.pdfbox.io.RandomAccessReadBuffer;
import org.apache.pdfbox.pdfparser.PDFParser;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;


public class PdfToText {
    private static final Logger log = LoggerFactory.getLogger(PdfToText.class);

    public List<String> getTextParts(byte[] fileContent) {
        try {
            try (var readerBuffer = new RandomAccessReadBuffer(fileContent)) {
                var parser = new PDFParser(readerBuffer);
                try (var pdDocument = parser.parse()) {
                    var pdDoc = new PDDocument(pdDocument.getDocument());
                    var tableExtractor = new PDFtextPartsExtractor();
                    tableExtractor.setSortByPosition(true);
                    for (var page : pdDoc.getPages()) {
                        log.info("Processing page:{}", (pdDoc.getPages().indexOf(page) + 1));
                        tableExtractor.processPage(page);
                    }
                    return tableExtractor.getTextParts();
                }
            }
        } catch (Exception ex) {
            throw new ParserException("can't parse content", ex);
        }
    }
}
