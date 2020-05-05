package com.example.voiceassistent;

import android.media.VolumeShaper;

import java.text.ParseException;
import java.util.function.Consumer;

@FunctionalInterface
public interface AnswerObject {
    void answer(String s, final Consumer<String> callback) throws ParseException;
}
