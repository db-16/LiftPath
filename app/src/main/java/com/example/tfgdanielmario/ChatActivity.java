package com.example.tfgdanielmario;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatActivity extends AppCompatActivity {

    private EditText editTextMessage;
    private MaterialButton buttonSend;
    private RecyclerView recyclerView;
    private MessageAdapter adapter;
    private List<Message> messageList;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String currentUserName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // Verificar autenticación
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();

        // Configurar la barra de navegación
        setupBottomNavigation();

        // Inicializar vistas
        editTextMessage = findViewById(R.id.editTextMessage);
        buttonSend = findViewById(R.id.buttonSend);
        recyclerView = findViewById(R.id.recyclerViewMessages);

        // Configurar RecyclerView
        messageList = new ArrayList<>();
        adapter = new MessageAdapter(messageList);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);

        // Obtener nombre del usuario actual
        loadCurrentUserName();

        // Configurar botón de envío
        buttonSend.setOnClickListener(v -> sendMessage());

        // Cargar mensajes
        loadMessages();
    }

    private void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_chat);

        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_progress) {
                startActivity(new Intent(this, ProgressActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_workout) {
                startActivity(new Intent(this, MyWorkoutPlanActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_chat) {
                return true;
            }
            return false;
        });
    }

    private void loadCurrentUserName() {
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser != null) {
            db.collection("users").document(currentUser.getUid())
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            currentUserName = documentSnapshot.getString("name");
                            if (currentUserName == null || currentUserName.isEmpty()) {
                                currentUserName = "Usuario";
                            }
                        } else {
                            currentUserName = "Usuario";
                        }
                    })
                    .addOnFailureListener(e -> currentUserName = "Usuario");
        }
    }

    private void sendMessage() {
        String text = editTextMessage.getText().toString().trim();
        if (TextUtils.isEmpty(text)) return;

        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "Debes iniciar sesión para enviar mensajes", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        Map<String, Object> messageMap = new HashMap<>();
        messageMap.put("text", text);
        messageMap.put("user", currentUserName != null ? currentUserName : "Usuario");
        messageMap.put("userId", currentUser.getUid());
        messageMap.put("timestamp", com.google.firebase.Timestamp.now());

        db.collection("globalChat")
                .add(messageMap)
                .addOnSuccessListener(documentReference -> {
                    editTextMessage.setText("");
                    recyclerView.smoothScrollToPosition(messageList.size());
                })
                .addOnFailureListener(e -> 
                    Toast.makeText(ChatActivity.this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show());
    }

    private void loadMessages() {
        try {
            db.collection("globalChat")
                    .orderBy("timestamp", Query.Direction.ASCENDING)
                    .addSnapshotListener((snapshots, e) -> {
                        if (e != null) {
                            Toast.makeText(ChatActivity.this, "Error al cargar los mensajes", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        if (snapshots == null) return;

                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            try {
                                if (dc.getType() == DocumentChange.Type.ADDED) {
                                    Message message = dc.getDocument().toObject(Message.class);
                                    if (message != null) {
                                        messageList.add(message);
                                        adapter.notifyItemInserted(messageList.size() - 1);
                                        recyclerView.smoothScrollToPosition(messageList.size() - 1);
                                    }
                                }
                            } catch (Exception documentError) {
                                // Ignorar mensajes malformados
                            }
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Error al inicializar el chat", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (auth.getCurrentUser() == null) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }
    }
}



