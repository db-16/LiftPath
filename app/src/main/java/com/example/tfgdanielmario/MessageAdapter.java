package com.example.tfgdanielmario;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {

    private List<Message> messageList;
    private String currentUserId;
    private SimpleDateFormat timeFormat;

    public MessageAdapter(List<Message> messages) {
        this.messageList = messages;
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        this.currentUserId = currentUser != null ? currentUser.getUid() : "";
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    public static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView textUser, textMessage, textTime;
        LinearLayout messageContainer;
        MaterialCardView cardView;

        public MessageViewHolder(View itemView) {
            super(itemView);
            textUser = itemView.findViewById(R.id.textViewUser);
            textMessage = itemView.findViewById(R.id.textViewMessage);
            textTime = itemView.findViewById(R.id.textViewTime);
            messageContainer = itemView.findViewById(R.id.messageContainer);
            cardView = itemView.findViewById(R.id.messageCard);
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
        boolean isCurrentUser = !currentUserId.isEmpty() && currentUserId.equals(msg.getUserId());

        // Configurar el contenedor del mensaje
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) holder.messageContainer.getLayoutParams();
        if (isCurrentUser) {
            params.addRule(RelativeLayout.ALIGN_PARENT_END);
            params.removeRule(RelativeLayout.ALIGN_PARENT_START);
            holder.messageContainer.setLayoutParams(params);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.nav_orange));
            holder.textMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.black));
            holder.textUser.setVisibility(View.GONE);
        } else {
            params.addRule(RelativeLayout.ALIGN_PARENT_START);
            params.removeRule(RelativeLayout.ALIGN_PARENT_END);
            holder.messageContainer.setLayoutParams(params);
            holder.cardView.setCardBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.card_background));
            holder.textMessage.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.white));
            holder.textUser.setVisibility(View.VISIBLE);
            holder.textUser.setText(msg.getUser() != null ? msg.getUser() : "Usuario");
        }

        holder.textMessage.setText(msg.getText() != null ? msg.getText() : "");
        
        if (msg.getTimestamp() != null) {
            try {
                holder.textTime.setText(timeFormat.format(msg.getTimestamp().toDate()));
            } catch (Exception e) {
                holder.textTime.setText("");
            }
        } else {
            holder.textTime.setText("");
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }
}

