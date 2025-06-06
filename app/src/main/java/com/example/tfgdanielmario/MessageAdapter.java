package com.example.tfgdanielmario;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;

    public MessageAdapter(List<Message> messages) {
        this.messageList = messages;
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textUser, textMessage;

        public MessageViewHolder(View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.textViewUser);
            textMessage = itemView.findViewById(R.id.textViewMessage);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message msg = messageList.get(position);
        holder.textUser.setText(msg.getUser());
        holder.textMessage.setText(msg.getText());
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

