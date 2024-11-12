package ru.petrelevich.parser;

import org.junit.jupiter.api.Test;
import ru.petrelevich.model.CategoryPattern;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static ru.petrelevich.parser.Categorizer.CATEGORY_EMPTY;


class CategorizerTest {

    @Test
    void getCategory() {
        //given
        var categories = new ArrayList<CategoryPattern>();

        var catComputer = new CategoryPattern("компьютер", List.of("DNS", "citylink"));
        categories.add(catComputer);
        var catFood = new CategoryPattern("еда", List.of("Пятерочка", "Верный"));
        categories.add(catFood);

        var categorizer = new Categorizer(categories);
        
        //when
        var cat1 = categorizer.getCategory("неизвестно что");
        //then
        assertThat(cat1).isEqualTo(CATEGORY_EMPTY);

        //when
        var cat2 = categorizer.getCategory("покупка в dns");
        //then
        assertThat(cat2).isEqualTo(catComputer.name());

        //when
        var cat3 = categorizer.getCategory("куплены ПятеРочка продукты");
        //then
        assertThat(cat3).isEqualTo(catFood.name());
    }
}