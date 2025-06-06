package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private Button buttonSend;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;

    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.navigation_progress) {
                        Intent progressIntent = new Intent(this, ProgressActivity.class);
                        startActivity(progressIntent);
                        finish();
                        return true;
                    } else if (itemId == R.id.navigation_workout) {
                        Intent workoutIntent = new Intent(this, MyWorkoutPlanActivity.class);
                        startActivity(workoutIntent);
                        finish();
                        return true;
                    } else if (item.getItemId() == R.id.navigation_profile) {
                        Intent intent = new Intent(this, Profile.class);
                        startActivity(intent);
                        return true;
                    } else if (itemId == R.id.navigation_chat) {
                        return true;
                    }
                    return false;
        });
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewMessages);

        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        buttonSend.setOnClickListener(v -> sendMessage());

        loadMessages();
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", text);
        messageMap.put("user", "Anónimo");  // Aquí puedes usar el nombre real si tienes auth
        messageMap.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());

        db.collection("globalChat").add(messageMap);
        editTextMessage.setText("");
    }

    private void loadMessages() {
        db.collection("globalChat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;

                    for (DocumentChange dc : snapshots.getDocumentChanges()) {
                        switch (dc.getType()) {
                            case ADDED:
                                String user = dc.getDocument().getString("user");
                                String text = dc.getDocument().getString("text");

                                // Evitar duplicados si ya está en la lista
                                boolean alreadyExists = false;
                                for (Message m : messageList) {
                                    if (m.getText().equals(text) && m.getUser().equals(user)) {
                                        alreadyExists = true;
                                        break;
                                    }
                                }

                                if (!alreadyExists) {
                                    messageList.add(new Message(user, text));
                                    adapter.notifyItemInserted(messageList.size() - 1);
                                    recyclerView.scrollToPosition(messageList.size() - 1);
                                }
                                break;
                        }
                    }
                });
    }

}



