package ru.petrelevich.loader;

import java.nio.file.Files;
import java.nio.file.Paths;

public class StatementLoaderFile implements StatementLoader {
    private final String fileName;

    public StatementLoaderFile(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public byte[] getContent() {
        try {
            var file = Paths.get(fileName);
            return Files.readAllBytes(file);
        } catch (Exception ex) {
            throw new StatementLoaderException("can't load file:" + fileName, ex);
        }
    }
}
