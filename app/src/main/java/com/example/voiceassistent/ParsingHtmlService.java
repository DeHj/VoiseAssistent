package com.example.voiceassistent;


import android.provider.DocumentsContract;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ListIterator;

public class ParsingHtmlService {
    private static final String URL = "http://mirkosmosa.ru/holiday/2020";

    public static String getHolyday(String date) throws IOException {
        if (date.startsWith("0")) {
            date = date.replaceFirst("0", "");
        }

        Document document = Jsoup.connect(URL).get();
        Elements divs = document.body().select("div.next_phase");

        ListIterator<Element> iter = divs.listIterator();
        String holidaysStr = "";
        while(iter.hasNext()) {
            String test = ((Element)iter.next().childNode(0)).text();
            if (test.contains("мая")) {
                date += "";
            }
            if (test.contains(date)) {
                Element el = (Element)iter.previous().childNode(1);
                Elements holidays = el.select("a");
                if (!holidays.isEmpty()) {
                    for (Element holiday : holidays) {
                        holidaysStr += holiday.text() + "; ";
                    }
                    return holidaysStr;
                }
                return "Праздников нет"; //holidaysStr == "" ? "Праздников нет" : holidaysStr;
            }
        }
        return "Праздников нет";
    }
}
