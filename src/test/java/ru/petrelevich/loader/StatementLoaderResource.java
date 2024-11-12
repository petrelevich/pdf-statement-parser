package ru.petrelevich.loader;

import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;

public class StatementLoaderResource implements StatementLoader{
    @Override
    public byte[] getContent() {
        var filePath = "/test-data/testStatement.pdf";
        URL fileUrl = StatementLoaderResource.class.getResource(filePath);
        if (fileUrl == null) {
            throw new StatementLoaderException("can't found file:" + filePath);
        }
        try {
            var file = Paths.get(fileUrl.getPath());
            return Files.readAllBytes(file);
        } catch (Exception ex) {
            throw new StatementLoaderException("can't load file:" + filePath, ex);
        }
    }
}
