package com.example.voiceassistent;

import android.os.AsyncTask;
import android.os.Build;

import androidx.annotation.RequiresApi;

import com.example.voiceassistent.Forecast.ForecastToString;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


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
        put("weather?", new AnswerObject() {
            @Override
            public String answer() {
                return "Не знхааю!";
            }
        });
    }};

    // Получение ответа от ИИ
    @RequiresApi(api = Build.VERSION_CODES.N)
    public static void getAnswer(String question, final Consumer<String> callback) throws ParseException {
        question = question.toLowerCase();

        Pattern cityPattern = Pattern.compile("weather in (\\p{L}+)", Pattern.CASE_INSENSITIVE);
        //Pattern cityPattern = Pattern.compile("погода в городе (\\p{L}+)", Pattern.CASE_INSENSITIVE);
        Matcher matcher = cityPattern.matcher(question);

        if (phrases.get(question) != null)
            callback.accept(Objects.requireNonNull(phrases.get(question)).answer());
        else if (matcher.find()) {
            String cityName = matcher.group(1);
            ForecastToString.getForecast(cityName, new Consumer<String>() {
                @Override
                public void accept(String s) {
                    callback.accept(s);
                }
            });
        }
        else if (question.contains("праздник")) {
            /*
            new AsyncTask<String, Integer, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    return null;
                }
            }
            */

            List<String> dates = getDates(question);

            Observable.fromCallable(() -> {
                String result = "";
                for (String str : dates) {
                    try {
                        result += " " + str + ": " + ParsingHtmlService.getHolyday(str) + "\n";
                    } catch (IOException e) {
                        result += " " + str + ": Не могу ответь :(";
                    }
                }
                return result;
            })
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(callback::accept);

            /*
            for (String date: dates) {
                callback.accept(date);
            }
            callback.accept("Это всё");
            */
        }
        else {
            callback.accept("Не понял вопрос");
        }
    }

    private static String modify(String date) {
        String[] mas = date.split(" ");
        Matcher matcher = Pattern.compile("0\\d").matcher(mas[0]);
        if (matcher.find()) {
            return date.substring(1);
        } else {
            return date;
        }
    }

    public static List<String> getDates(String question) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM YYYY", dateFormatSymbols);
        String[] blocks = question.split(",");

        Pattern pattern1 = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{4}");
        //Pattern pattern2 = Pattern.compile("\\d{1,2} (\\p{L}+) \\d{4}");

        List<String> result = new ArrayList<String>();
        Date tmp;
        for (String block : blocks) {
            Matcher matcher1 = pattern1.matcher(block);
            //Matcher matcher2 = pattern2.matcher(block);

            if (block.contains("вчера")) {
                calendar.add(Calendar.DAY_OF_YEAR, -1);
                tmp = calendar.getTime();
                result.add(sdf.format(tmp));

                calendar.add(Calendar.DAY_OF_YEAR, +1);
            } else if (block.contains("сегодня")) {
                tmp = calendar.getTime();
                result.add(sdf.format(tmp));
            } else if (block.contains("завтра")) {
                calendar.add(Calendar.DAY_OF_YEAR, +1);
                tmp = calendar.getTime();
                result.add(sdf.format(tmp));
                calendar.add(Calendar.DAY_OF_YEAR, -1);
            } else if (matcher1.find()) {
                result.add(sdf.format(Objects.requireNonNull(new SimpleDateFormat("dd.MM.yyyy").
                        parse(block.substring(matcher1.start(), matcher1.end())))));
            } /*else if (matcher2.find()) {

                if ()
                String month = matcher2.group(1);
                result.add("");
            }*/
        }

        return result;
    }

    private static DateFormatSymbols dateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }

    };

}


