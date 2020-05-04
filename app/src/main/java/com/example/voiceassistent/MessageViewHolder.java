package com.example.voiceassistent;

import androidx.recyclerview.widget.RecyclerView;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Locale;

public class MessageViewHolder extends RecyclerView.ViewHolder {
    protected TextView messageText;
    protected TextView messageDate;

    public MessageViewHolder(@NonNull View itemView) {
        super(itemView);
        this.messageText = itemView.findViewById(R.id.messageTextView);
        this.messageDate = itemView.findViewById(R.id.messageDateView);
    }

    public void bind(Message message) {
        messageText.setText(message.text);
        DateFormat fmt = new SimpleDateFormat("", Locale.US);
        messageDate.setText(fmt.format(message.date));
    }
}
