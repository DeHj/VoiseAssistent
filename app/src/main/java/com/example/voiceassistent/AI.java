package com.example.voiceassistent;

import android.os.AsyncTask;
import android.os.Build;
import android.util.Pair;

import androidx.annotation.RequiresApi;

import com.example.voiceassistent.Forecast.ForecastToString;

import java.io.IOException;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Date;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.Observable;
import io.reactivex.rxjava3.schedulers.Schedulers;


public class AI {
    private List<Pair<Predicate<String>, AnswerObject>> phrases = new ArrayList<>();
    private void phrasesInit() {
        phrases.add(new Pair<>(
                s -> (s.contains("привет") || s.contains("прювет")),
                (s, callback) -> callback.accept("Прювет")));
        phrases.add(new Pair<>(
                s -> (s.contains("hi") || s.contains("hello")),
                (s, callback) -> callback.accept("о вы из англии! Hi!")));
        phrases.add(new Pair<>(
                s -> ((s.contains("чем") && s.contains("занимаешься")) || s.contains("what are you doing")),
                (s, callback) -> callback.accept("получаю зачёт :)")));
        phrases.add(new Pair<>(
                s -> ((s.contains("как") && s.contains("дела")) || s.contains("how are you")),
                (s, callback) -> callback.accept("нормас")));
        phrases.add(new Pair<>(
                s -> ((s.contains("сколько") && (s.contains("время") || s.contains("времени"))) || s.contains("time?")),
                (s, callback) -> {
                    DateFormat df = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
                    Date curDate = new Date();
                    callback.accept(df.format(curDate));
                }));
        phrases.add(new Pair<>(
                s -> ((s.contains("какой") && s.contains("сегодня") && s.contains("день")) || s.contains("day?")),
                (s, callback) -> {
                    DateFormat df = new SimpleDateFormat("yyyy/MM/dd", Locale.getDefault());
                    Date curDate = new Date();
                    callback.accept(df.format(curDate));
                }));
        phrases.add(new Pair<>(
                s -> ((s.contains("какой") && s.contains("сегодня") && s.contains("день") && s.contains("недели")) || s.contains("day of week?")),
                (s, callback) -> {
                    Date curDate = new Date();
                    Calendar cal = Calendar.getInstance();
                    cal.setFirstDayOfWeek(Calendar.MONDAY);
                    cal.setTime(curDate);
                    int dayOfWeek = cal.get(Calendar.DAY_OF_WEEK);
                    switch (dayOfWeek) {
                        case 1: callback.accept("Воскресенье"); return;
                        case 2: callback.accept("Понедельник"); return;
                        case 3: callback.accept("Вторник"); return;
                        case 4: callback.accept("Среда"); return;
                        case 5: callback.accept("Четверг"); return;
                        case 6: callback.accept("Пятница"); return;
                        case 7: callback.accept("Суббота"); return;

                    }
                    callback.accept("Я не знаю, какой сегодня день");
                }));
        phrases.add(new Pair<>(
                s -> ((s.contains("сколько") && s.contains("до зачёта")) || s.contains("to test?")),
                (s, callback) -> {
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.DAY_OF_MONTH, 6);
                    cal.set(Calendar.MONTH, 5);
                    cal.set(Calendar.YEAR, 2020);
                    Calendar today = Calendar.getInstance();
                    long delta = cal.getTimeInMillis() - today.getTimeInMillis();
                    callback.accept((delta / (24 * 60 * 60 * 1000)) + " дней до зачета");
                }));
        phrases.add(new Pair<>(
                s -> {
                    Pattern cityPattern = Pattern.compile("weather in (\\p{L}+)", Pattern.CASE_INSENSITIVE);
                    return (cityPattern.matcher(s).find());
                },
                (s, callback) -> {
                    Pattern cityPattern = Pattern.compile("weather in (\\p{L}+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = cityPattern.matcher(s);

                    if (matcher.find()) {
                        String cityName = matcher.group(1);
                        ForecastToString.getForecast(cityName, new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                callback.accept(s);
                            }
                        });
                    }
                    else {
                        callback.accept("странно...");
                    }
                }));
        phrases.add(new Pair<>(
                s -> {
                    Pattern cityPattern = Pattern.compile("погода в (\\p{L}+)", Pattern.CASE_INSENSITIVE);
                    return (cityPattern.matcher(s).find());
                },
                (s, callback) -> {
                    Pattern cityPattern = Pattern.compile("погода в (\\p{L}+)", Pattern.CASE_INSENSITIVE);
                    Matcher matcher = cityPattern.matcher(s);
                    if (matcher.find()) {
                        String cityName = matcher.group(1);
                        ForecastToString.getForecast(cityName, new Consumer<String>() {
                            @Override
                            public void accept(String s) {
                                callback.accept(s);
                            }
                        });
                    }
                }));
        phrases.add(new Pair<>(s -> {return (s.contains("праздник") || s.contains("holyday"));},
                (s, callback) -> {
                    List<String> dates = getDates(s);

                    if  (dates.isEmpty()) {
                        callback.accept("Какой день вас интересует? Уточните.");
                    }
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
                }));
    }

    public AI() {
        phrasesInit();
    }

    // Получение ответа от ИИ
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void getAnswer(String question, final Consumer<String> callback) throws ParseException {
        question = question.toLowerCase();

        for (int i = 0; i < phrases.size(); i++) {
            if (phrases.get(i).first.test(question)) {
                phrases.get(i).second.answer(question, callback);
                return;
            }
        }

        callback.accept("Не понял вопрос");
    }




    private String modify(String date) {
        String[] mas = date.split(" ");
        Matcher matcher = Pattern.compile("0\\d").matcher(mas[0]);
        if (matcher.find()) {
            return date.substring(1);
        } else {
            return date;
        }
    }

    private List<String> getDates(String question) throws ParseException {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM YYYY", dateFormatSymbols);
        String[] blocks = question.split(",");

        Pattern pattern1 = Pattern.compile("\\d{1,2}\\.\\d{1,2}\\.\\d{4}");

        List<String> result = new ArrayList<String>();
        Date tmp;
        for (String block : blocks) {
            Matcher matcher1 = pattern1.matcher(block);

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
            }
        }

        return result;
    }

    private DateFormatSymbols dateFormatSymbols = new DateFormatSymbols() {
        @Override
        public String[] getMonths() {
            return new String[]{"января", "февраля", "марта", "апреля", "мая", "июня",
                    "июля", "августа", "сентября", "октября", "ноября", "декабря"};
        }

    };
}


