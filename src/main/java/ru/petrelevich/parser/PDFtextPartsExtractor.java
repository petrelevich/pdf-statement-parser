package ru.petrelevich.parser;

import lombok.Getter;
import lombok.NonNull;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

@Getter
public class PDFtextPartsExtractor extends PDFTextStripper {
    private final List<String> textParts = new ArrayList<>();

    public PDFtextPartsExtractor() {
        super();
        output = new NullWriter();
    }

    @Override
    protected void writeString(String string, List<TextPosition> textPositions) throws IOException {
        textParts.add(string);
    }


    private static class NullWriter extends Writer {
        @Override
        public void write(@Nonnull char[] cbuf, int off, int len) {// is not used
        }

        @Override
        public void flush() {// is not used
        }

        @Override
        public void close() {// is not used
        }
    }

}