package ru.petrelevich.parser;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigDecimal;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;


class SumParserTest {

    private static Stream<Arguments> parseParameters() {
        return Stream.of(
                Arguments.of("4,994.00 RUB", new BigDecimal("4994.00")),
                Arguments.of("231.00", new BigDecimal("231.00")),
                Arguments.of("387.74", new BigDecimal("387.74")),
                Arguments.of("1,003.70", new BigDecimal("1003.70")
                )
        );
    }

    @ParameterizedTest
    @MethodSource("parseParameters")
    void parse(String sumStr, BigDecimal expectedSum) {
        //given
        var parser = new SumParser();

        //when
        var sum = parser.parse(sumStr);

        //then
        assertThat(sum).isEqualTo(expectedSum);
    }

}