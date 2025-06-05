package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {
    private EditText editTextMessage;
    private Button buttonSend;
    private ListView listViewMessages;
    private ArrayList<String> messagesList;
    private ArrayAdapter<String> adapter;
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
            }
            else if (item.getItemId() == R.id.navigation_profile) {
                Intent intent = new Intent(this, Profile.class);
                startActivity(intent);
                return true;
            }
            else if (itemId == R.id.navigation_chat) {
                return true;
            }
            return false;
        });

        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        listViewMessages = findViewById(R.id.listViewMessages);

        messagesList = new ArrayList<>();
        adapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, messagesList);
        listViewMessages.setAdapter(adapter);

        db = FirebaseFirestore.getInstance();

        buttonSend.setOnClickListener(v -> sendMessage());

        db.collection("globalChat")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null || snapshots == null) return;
                    messagesList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        String user = doc.getString("user");
                        String text = doc.getString("text");
                        messagesList.add(user + ": " + text);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (text.isEmpty()) return;

        Map<String, Object> msg = new HashMap<>();
        msg.put("text", text);
        msg.put("user", "Anónimo");
        msg.put("timestamp", FieldValue.serverTimestamp());

        db.collection("globalChat").add(msg);
        editTextMessage.setText("");
    }
}
