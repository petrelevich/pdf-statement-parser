package ru.petrelevich.parser;


import org.junit.jupiter.api.Test;
import ru.petrelevich.loader.StatementLoaderResource;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PdfToTextTest {

    @Test
    void getTextPartsTest() {
        //given
        var pdfToText = new PdfToText();
        var statementLoader = new StatementLoaderResource();

        //when
        var textParts = pdfToText.getTextParts(statementLoader.getContent());

        //then
        assertThat(textParts).isEqualTo(getExpectedParts());
    }

    private List<String> getExpectedParts() {
        var parts = new ArrayList<String>();
        parts.add("Васильев Петр Иванович");
        parts.add("Номер договора");
        parts.add("00000-P-12112126");
        parts.add("Номер счёта");
        parts.add("40817121617111002702");
        parts.add("Период выписки");
        parts.add("31.07.2024 - 31.07.2024");
        parts.add("Информация о счёте");
        parts.add("Баланс на начало периода");
        parts.add("1 548,92 RUB");
        parts.add("Поступления");
        parts.add("0,00 RUB");
        parts.add("Баланс на конец периода");
        parts.add("423,92 RUB");
        parts.add("Расходные операции");
        parts.add("1 125,00 RUB");
        parts.add("Операции по счёту");
        parts.add("Сумма операции в валюте");
        parts.add("счета/карты");
        parts.add("Дата и время");
        parts.add("Дата обработки");
        parts.add("Сумма операции в");
        parts.add("операции");
        parts.add("банком");
        parts.add("валюте операции");
        parts.add("Комиссия");
        parts.add("Описание операции");
        parts.add("Приход");
        parts.add("Расход");
        parts.add("31.07.2024");
        parts.add("Оплата товаров и услуг. DNS 1058. РФ.");
        parts.add("10:19:54");
        parts.add("03.08.2024");
        parts.add("-199,00 RUB");
        parts.add("0,00");
        parts.add("199,00");
        parts.add("0,00 RUB");
        parts.add("MAGNITOGORSK . 861000004126 по карте");
        parts.add("*1021");
        parts.add("31.07.2024");
        parts.add("09:53:46");
        parts.add("03.08.2024");
        parts.add("-185,00 RUB");
        parts.add("0,00");
        parts.add("185,00");
        parts.add("0,00 RUB");
        parts.add("Оплата товаров и услуг. LAKOMKA. РФ.");
        parts.add("Magnitogorsk . POS0013794 по карте *1021");
        parts.add("28.07.2024");
        parts.add("Оплата товаров и услуг. SERGEYS. РФ.");
        parts.add("21:58:17");
        parts.add("31.07.2024");
        parts.add("-95,00 RUB");
        parts.add("0,00");
        parts.add("95,00");
        parts.add("0,00 RUB");
        parts.add("MAGNITOGORSK . 860000018292 по карте");
        parts.add("*1021");
        parts.add("28.07.2024");
        parts.add("Оплата товаров и услуг. SKY CINEMA BAR. РФ.");
        parts.add("21:50:13");
        parts.add("31.07.2024");
        parts.add("-230,00 RUB");
        parts.add("0,00");
        parts.add("230,00");
        parts.add("0,00 RUB");
        parts.add("MAGNITOGORSK . 720000010974 по карте");
        parts.add("*1021");
        parts.add("28.07.2024");
        parts.add("Оплата товаров и услуг. SKY CINEMA");
        parts.add("21:49:14");
        parts.add("31.07.2024");
        parts.add("-800,00 RUB");
        parts.add("0,00");
        parts.add("800,00");
        parts.add("0,00 RUB");
        parts.add("KINOTEATR. РФ. MAGNITOGORSK .");
        parts.add("720000010973 по карте *1021");
        parts.add("Спасибо, что Вы с нами!");
        parts.add("Всегда Ваш, Банк ВТБ (ПАО)");
        parts.add("1");

        return parts;
    }
}