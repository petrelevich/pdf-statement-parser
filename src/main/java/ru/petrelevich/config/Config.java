package ru.petrelevich.config;

import java.util.List;
import ru.petrelevich.model.CategoryPattern;

public record Config(String pdfFile, String csvFile, List<CategoryPattern> categories) {}
