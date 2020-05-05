package com.example.voiceassistent;

import org.junit.Test;

import java.io.IOException;
import java.text.ParseException;

import static com.example.voiceassistent.ParsingHtmlService.getHolyday;
import static org.junit.Assert.*;

public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDate1() throws IOException {
        String date = ParsingHtmlService.getHolyday("4 февраля 2020");
        assertEquals(date, "Всемирный день борьбы с раковыми заболеваниями; ");
    }
}