package ru.petrelevich.parser;

import ru.petrelevich.model.CategoryPattern;

import java.util.List;

public class Categorizer {
    public static final String CATEGORY_EMPTY = "";
    private final List<CategoryPattern> categories;

    public Categorizer(List<CategoryPattern> categories) {
        this.categories = categories;
    }

    public String getCategory(String comment) {
        for (var categoryPattern: categories) {
            for (var commentPart: categoryPattern.commentPart()) {
                if (comment.toLowerCase().contains(commentPart.toLowerCase())) {
                    return categoryPattern.name();
                }
            }
        }
        return CATEGORY_EMPTY;
    }
}
