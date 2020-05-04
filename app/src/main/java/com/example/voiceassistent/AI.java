package com.example.voiceassistent;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class AI {
//    public static Map<String, String> phrases = new HashMap<String, String>() {{
//        put("привет", "Привет");
//        put("как дела?", "Норм");
//        put("чем занимаешься?", "отвечаю на твои глупые вопросы");
//        put("hi", "Hi!");
//    }};

    // Набор вопросов и объектов-поведений на них
    public static Map<String, AnswerObject> phrases = new HashMap<String, AnswerObject>() {{
        put("привет", new AnswerObject() {
            @Override
            public String answer() {
                return "Прювет";
            }
        });
        put("как дела?", new AnswerObject() {
            @Override
            public String answer() {
                return "Норм";
            }
        });
        put("чем занимаешься?", new AnswerObject() {
            @Override
            public String answer() {
                return "Отвечаю на твои глупые вопросы";
            }
        });
        put("сколько время?", new AnswerObject() {
            @Override
            public String answer() {
                DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                Date curDate = new Date();
                return df.format((curDate));
            }
        });
        //put("какое сегодня число?", new AnswerObject() {
        put("day?", new AnswerObject() {
            @Override
            public String answer() {
                DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                Date curDate = new Date();
                return (df.format(curDate));
            }
        });
        //put("какой сегодня день недели?", new AnswerObject() {
        put("day of week?", new AnswerObject() {
            @Override
            public String answer() {
                Date curDate = new Date();
                Calendar cal = Calendar.getInstance();
                cal.setFirstDayOfWeek(Calendar.MONDAY);
                cal.setTime(curDate);
                int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                switch (dayOfWeek) {
                    case 1: return "Воскресенье";
                    case 2: return "Понедельник";
                    case 3: return "Вторник";
                    case 4: return "Среда";
                    case 5: return "Четверг";
                    case 6: return "Пятница";
                    case 7: return "Суббота";
                }
                return "Я не знаю, какой сегодня день";
            }
        });
        //put("сколько осталось до зачёта?", new AnswerObject() {
        put("to test?", new AnswerObject() {
            @Override
            public String answer() {
                Calendar cal = Calendar.getInstance();
                cal.set(Calendar.DAY_OF_MONTH, 6);
                cal.set(Calendar.MONTH, 5);
                cal.set(Calendar.YEAR, 2020);
                Calendar today = Calendar.getInstance();
                long delta = cal.getTimeInMillis() - today.getTimeInMillis();
                return (delta / (24 * 60 * 60 * 1000)) + " дней до зачета";
            }
        });
    }};

    // Получение ответа от ИИ
    public static String getAnswer(String question) {
        question = question.toLowerCase();
        if (phrases.get(question) != null)
            return phrases.get(question).answer();
        else
            return "Вопрос понял. Думаю...";
    }
}


