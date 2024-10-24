package ru.petrelevich.model;


import java.util.List;


public record CategoryPattern(
        String name,
        List<String> commentPart) {
}
