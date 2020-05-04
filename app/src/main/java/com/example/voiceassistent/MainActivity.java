package com.example.voiceassistent;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.os.Parcelable;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    protected Button sendButton;
    protected EditText questionText;
    protected RecyclerView chatMessageList;
    protected MessageListAdapter messageListAdapter;

    protected TextToSpeech textToSpeech;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        questionText = findViewById(R.id.questionField);
        chatMessageList = findViewById(R.id.chatMessageList);
        chatMessageList.setLayoutManager(new LinearLayoutManager(this));

        messageListAdapter = new MessageListAdapter();
        chatMessageList.setAdapter(messageListAdapter);

        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int i) {
                if (i!= TextToSpeech.ERROR) {
                    textToSpeech.setLanguage(new Locale("ru"));
                }
            }
        });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onSend();
            }
        });


        // temp:
        String question = "Привет";
        messageListAdapter.messageList.add(new Message(question, true));
    }

    protected void onSend() {
        String question = questionText.getText().toString();
        messageListAdapter.messageList.add(new Message(question, true));

        String answer = AI.getAnswer(question);
        messageListAdapter.messageList.add(new Message(answer, false));
        messageListAdapter.notifyDataSetChanged();

        textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
        chatMessageList.scrollToPosition(messageListAdapter.messageList.size() - 1);
        questionText.setText("");
    }

    // Сохранение состояния при повороте
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        //savedInstanceState.putParcelable("dialog", (<Parcelable>) chatWindow.getText().toString());
        savedInstanceState.putParcelableArrayList("dialog", (ArrayList<? extends Parcelable>) messageListAdapter.messageList);
        super.onSaveInstanceState(savedInstanceState);
    }

    //Восстановление сохраненного состояния
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {

        super.onRestoreInstanceState(savedInstanceState);

        messageListAdapter.messageList = savedInstanceState.getParcelableArrayList("dialog");
        messageListAdapter.notifyDataSetChanged();
        chatMessageList.scrollToPosition(messageListAdapter.messageList.size() - 1);
    }
}