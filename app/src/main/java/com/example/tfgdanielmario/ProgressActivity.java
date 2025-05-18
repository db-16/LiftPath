package com.example.tfgdanielmario;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProgressActivity extends AppCompatActivity {

    private TextView tvInitialWeight;
    private TextView tvCurrentWeight;
    private TextView tvGoalWeight;
    private Button btnUpdateWeight;
    private LineChart weightChart;
    private TextView tvProgressPercentage;
    private FirebaseFirestore db;
    private String userId;
    private double initialWeight = 0.0;
    private double goalWeight = 0.0;
    private double currentWeight = 0.0;
    private Date lastWeightDate = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_progress);

        // Configurar la barra de navegación
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.navigation_progress);
        
        bottomNavigationView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.navigation_workout) {
                startActivity(new Intent(this, MyWorkoutPlanActivity.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_profile) {
                startActivity(new Intent(this, Profile.class));
                finish();
                return true;
            } else if (itemId == R.id.navigation_progress) {
                return true;
            }
            return false;
        });

        // Inicializar Firebase
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Inicializar vistas
        tvInitialWeight = findViewById(R.id.tvInitialWeight);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoalWeight = findViewById(R.id.tvGoalWeight);
        btnUpdateWeight = findViewById(R.id.btnUpdateWeight);
        weightChart = findViewById(R.id.weightChart);
        tvProgressPercentage = findViewById(R.id.tvProgressPercentage);

        // Configurar botón de actualizar peso
        btnUpdateWeight.setOnClickListener(v -> showUpdateWeightDialog());

        // Configurar gráfico
        setupChart();

        // Cargar datos
        loadUserData();
        loadWeightHistory();
    }

    private void setupChart() {
        weightChart.getDescription().setEnabled(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setDrawGridBackground(false);
        weightChart.setBackgroundColor(Color.TRANSPARENT);

        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setDrawGridLines(false);

        weightChart.getAxisLeft().setTextColor(Color.WHITE);
        weightChart.getAxisRight().setEnabled(false);
        weightChart.getLegend().setTextColor(Color.WHITE);
    }

    private void loadUserData() {
        db.collection("users")
                .document(userId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    User user = documentSnapshot.toObject(User.class);
                    if (user != null) {
                        initialWeight = user.getWeight();
                        goalWeight = user.getGoalWeight();
                        currentWeight = user.getCurrentWeight();
                        tvInitialWeight.setText(String.format(Locale.getDefault(), "%.1f kg", initialWeight));
                        tvGoalWeight.setText(String.format(Locale.getDefault(), "%.1f kg", goalWeight));
                        updateCurrentWeight(currentWeight);
                    }
                });
    }

    private void loadWeightHistory() {
        db.collection("weightHistory")
                .document(userId)
                .collection("records")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> dates = new ArrayList<>();
                    float lastWeight = (float) initialWeight;
                    int index = 0;

                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MONTH, -1); // Mostrar último mes

                    for (int i = 0; i < 30; i++) {
                        calendar.add(Calendar.DAY_OF_MONTH, 1);
                        dates.add(dateFormat.format(calendar.getTime()));
                        entries.add(new Entry(index++, lastWeight));
                    }

                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (int i = 0; i < entries.size(); i++) {
                            boolean foundWeight = false;
                            Date currentDate = calendar.getTime();
                            
                            for (int j = queryDocumentSnapshots.size() - 1; j >= 0; j--) {
                                Date weightDate = queryDocumentSnapshots.getDocuments().get(j).getDate("date");
                                if (weightDate != null && isSameDay(weightDate, currentDate)) {
                                    lastWeight = queryDocumentSnapshots.getDocuments().get(j).getDouble("weight").floatValue();
                                    foundWeight = true;
                                    break;
                                }
                            }
                            
                            if (!foundWeight) {
                                entries.set(i, new Entry(i, lastWeight));
                            } else {
                                entries.set(i, new Entry(i, lastWeight));
                            }
                            
                            calendar.add(Calendar.DAY_OF_MONTH, -1);
                        }

                        // Actualizar el peso actual con el último peso registrado
                        updateCurrentWeight(lastWeight);
                    }

                    LineDataSet dataSet = new LineDataSet(entries, "Peso (kg)");
                    dataSet.setColor(Color.WHITE);
                    dataSet.setCircleColor(Color.WHITE);
                    dataSet.setValueTextColor(Color.WHITE);

                    LineData lineData = new LineData(dataSet);
                    weightChart.setData(lineData);

                    XAxis xAxis = weightChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
                    xAxis.setLabelRotationAngle(45f);
                    xAxis.setLabelCount(7);

                    weightChart.invalidate();

                    // Calcular y mostrar porcentaje de progreso
                    if (initialWeight > 0) {
                        double progressPercentage;
                        if (goalWeight > initialWeight) {
                            // Objetivo de ganar peso
                            progressPercentage = ((lastWeight - initialWeight) / (goalWeight - initialWeight)) * 100;
                        } else {
                            // Objetivo de perder peso
                            progressPercentage = ((initialWeight - lastWeight) / (initialWeight - goalWeight)) * 100;
                        }
                        tvProgressPercentage.setText(String.format(Locale.getDefault(), 
                            "Progreso: %.1f%%", progressPercentage));
                    }
                });
    }

    private boolean isSameDay(Date date1, Date date2) {
        Calendar cal1 = Calendar.getInstance();
        Calendar cal2 = Calendar.getInstance();
        cal1.setTime(date1);
        cal2.setTime(date2);
        return cal1.get(Calendar.YEAR) == cal2.get(Calendar.YEAR) &&
                cal1.get(Calendar.MONTH) == cal2.get(Calendar.MONTH) &&
                cal1.get(Calendar.DAY_OF_MONTH) == cal2.get(Calendar.DAY_OF_MONTH);
    }

    private void showUpdateWeightDialog() {
        // Verificar si ya se registró peso hoy
        db.collection("weightHistory")
                .document(userId)
                .collection("records")
                .orderBy("date", Query.Direction.DESCENDING)
                .limit(1)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        Date lastDate = queryDocumentSnapshots.getDocuments().get(0).getDate("date");
                        if (lastDate != null && isSameDay(lastDate, new Date())) {
                            Toast.makeText(this, "Ya has registrado tu peso hoy", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    
                    // Mostrar diálogo para actualizar peso
                    Dialog dialog = new Dialog(this);
                    dialog.setContentView(R.layout.dialog_update_weight);
                    dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);

                    EditText etWeight = dialog.findViewById(R.id.etWeight);
                    Button btnSave = dialog.findViewById(R.id.btnSave);
                    Button btnCancel = dialog.findViewById(R.id.btnCancel);

                    btnSave.setOnClickListener(v -> {
                        String weightStr = etWeight.getText().toString();
                        if (!weightStr.isEmpty()) {
                            try {
                                double newWeight = Double.parseDouble(weightStr);
                                updateWeight(newWeight);
                                dialog.dismiss();
                            } catch (NumberFormatException e) {
                                Toast.makeText(this, "Por favor, ingresa un peso válido", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });

                    btnCancel.setOnClickListener(v -> dialog.dismiss());
                    dialog.show();
                });
    }

    private void updateWeight(double newWeight) {
        // Actualizar peso en el historial
        Map<String, Object> weightRecord = new HashMap<>();
        weightRecord.put("weight", newWeight);
        weightRecord.put("date", new Date());

        db.collection("weightHistory")
                .document(userId)
                .collection("records")
                .add(weightRecord)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar peso actual en el perfil del usuario
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("currentWeight", newWeight);

                    db.collection("users")
                            .document(userId)
                            .update(updates)
                            .addOnSuccessListener(aVoid -> {
                                updateCurrentWeight(newWeight);
                                loadWeightHistory(); // Recargar gráfico
                                
                                // Comparar con el peso objetivo y actualizar si es necesario
                                if (goalWeight > 0) {
                                    if (goalWeight > initialWeight && newWeight >= goalWeight) {
                                        // Objetivo alcanzado (ganar peso)
                                        Toast.makeText(this, "¡Felicidades! Has alcanzado tu objetivo de peso", Toast.LENGTH_LONG).show();
                                    } else if (goalWeight < initialWeight && newWeight <= goalWeight) {
                                        // Objetivo alcanzado (perder peso)
                                        Toast.makeText(this, "¡Felicidades! Has alcanzado tu objetivo de peso", Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, 
                                "Error al actualizar el peso en el perfil", Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, 
                    "Error al guardar el registro de peso", Toast.LENGTH_SHORT).show());
    }

    private void updateCurrentWeight(double weight) {
        currentWeight = weight;
        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
    }
} 