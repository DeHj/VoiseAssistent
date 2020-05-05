package com.example.voiceassistent;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.function.Consumer;

public class MainActivity extends AppCompatActivity {

    protected Button sendButton;
    protected EditText questionText;
    protected RecyclerView chatMessageList;
    protected MessageListAdapter messageListAdapter;
    protected TextToSpeech textToSpeech;

    DBHelper dBHelper;
    SQLiteDatabase database;

    protected SharedPreferences sPref;
    public static final String APP_PREFERENCES = "mysettings";

    private boolean isLight = true;
    private String THEME = "THEME";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        sPref = getSharedPreferences(APP_PREFERENCES, MODE_PRIVATE);
        isLight = sPref.getBoolean(THEME, true);
        getDelegate().setLocalNightMode(isLight ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        sendButton = findViewById(R.id.sendButton);
        questionText = findViewById(R.id.questionField);
        chatMessageList = findViewById(R.id.chatMessageList);
        chatMessageList.setLayoutManager(new LinearLayoutManager(this));

        messageListAdapter = new MessageListAdapter();
        chatMessageList.setAdapter(messageListAdapter);

        dBHelper = new DBHelper(this);
        database = dBHelper.getWritableDatabase();

        Cursor cursor = database.query(dBHelper.TABLE_MESSAGES, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int messageIndex = cursor.getColumnIndex(DBHelper.FIELD_MESSAGE);
            int dateIndex    = cursor.getColumnIndex(DBHelper.FIELD_DATE);
            int sendIndex    = cursor.getColumnIndex(DBHelper.FIELD_SEND);
            do {
                MessageEntity messageEntity = new MessageEntity(
                        cursor.getString(messageIndex),
                        cursor.getString(dateIndex),
                        cursor.getInt(sendIndex));
                Message message = null;
                try {
                    message = new Message(messageEntity);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                messageListAdapter.messageList.add(message);
            } while (cursor.moveToNext());
        }

        textToSpeech = new TextToSpeech(getApplicationContext(),
                new TextToSpeech.OnInitListener() {
                    @Override
                    public void onInit(int i) {
                        if (i != TextToSpeech.ERROR) {
                            textToSpeech.setLanguage(new Locale("ru"));
                        }
                    }
                });

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    onSend();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        messageListAdapter.messageList.add(new Message("Привет!", false));
    }

    protected void onSend() throws ParseException {
        final String question = questionText.getText().toString();

        messageListAdapter.messageList.add(new Message(question, true));

        AI.getAnswer(question, new Consumer<String>() {
            @Override
            public void accept(String answer) {
                //messageListAdapter.messageList.add(new Message(question, true));
                messageListAdapter.messageList.add(new Message(answer, false));
                messageListAdapter.notifyDataSetChanged();
                textToSpeech.speak(answer, TextToSpeech.QUEUE_FLUSH, null, null);
                chatMessageList.scrollToPosition(messageListAdapter.messageList.size() - 1);
                questionText.setText("");
            }
        });
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.day_settings:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                isLight = true;
                break;
            case R.id.night_settings:
                getDelegate().setLocalNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                isLight = false;
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("LOG", "onStop");

        SharedPreferences.Editor editor = sPref.edit();
        editor.putBoolean(THEME, isLight);
        editor.apply();

        database.delete(dBHelper.TABLE_MESSAGES, null, null);
        for (Message message : messageListAdapter.messageList) {
            MessageEntity entity = new MessageEntity(message);
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FIELD_MESSAGE, entity.text);
            contentValues.put(DBHelper.FIELD_SEND, entity.isSend);
            contentValues.put(DBHelper.FIELD_DATE, entity.date);

            database.insert(dBHelper.TABLE_MESSAGES, null, contentValues);
        }
    }

    @Override
    protected void onDestroy() {
        database.close();
        super.onDestroy();
        Log.i("LOG", "onDestroy");
    }
}