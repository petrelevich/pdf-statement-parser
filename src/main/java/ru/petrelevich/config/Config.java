package ru.petrelevich.config;

import ru.petrelevich.model.CategoryPattern;

import java.util.List;

public record Config(String pdfFile, String csvFile, List<CategoryPattern> categories) {
}
