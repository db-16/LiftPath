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
import java.util.List;
import java.util.Locale;

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
                        tvInitialWeight.setText(String.format(Locale.getDefault(), "%.1f kg", initialWeight));
                        tvGoalWeight.setText(String.format(Locale.getDefault(), "Objetivo: %.1f kg", goalWeight));
                        updateCurrentWeight(user.getWeight());
                    }
                });
    }

    private void loadWeightHistory() {
        // Obtener los últimos 7 días
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        Date startDate = calendar.getTime();

        db.collection("weightHistory")
                .document(userId)
                .collection("weights")
                .whereGreaterThanOrEqualTo("date", startDate)
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> dates = new ArrayList<>();
                    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM", Locale.getDefault());

                    float lastWeight = (float) initialWeight;
                    int i = 0;
                    for (com.google.firebase.firestore.DocumentSnapshot document : queryDocumentSnapshots) {
                        WeightRecord record = document.toObject(WeightRecord.class);
                        if (record != null) {
                            entries.add(new Entry(i, (float) record.getWeight()));
                            dates.add(sdf.format(record.getDate()));
                            lastWeight = (float) record.getWeight();
                            i++;
                        }
                    }

                    // Actualizar gráfico
                    LineDataSet dataSet = new LineDataSet(entries, "Peso (kg)");
                    dataSet.setColor(Color.WHITE);
                    dataSet.setCircleColor(Color.WHITE);
                    dataSet.setValueTextColor(Color.WHITE);

                    LineData lineData = new LineData(dataSet);
                    weightChart.setData(lineData);
                    weightChart.getXAxis().setValueFormatter(new IndexAxisValueFormatter(dates));
                    weightChart.invalidate();

                    // Calcular y mostrar porcentaje de progreso
                    if (initialWeight > 0) {
                        double progressPercentage = ((lastWeight - initialWeight) / initialWeight) * 100;
                        tvProgressPercentage.setText(String.format(Locale.getDefault(), 
                            "Progreso: %.1f%%", progressPercentage));
                    }
                });
    }

    private void showUpdateWeightDialog() {
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
    }

    private void updateWeight(double newWeight) {
        // Crear registro de peso
        WeightRecord weightRecord = new WeightRecord(newWeight, new Date());

        // Guardar en historial
        db.collection("weightHistory")
                .document(userId)
                .collection("weights")
                .add(weightRecord)
                .addOnSuccessListener(documentReference -> {
                    // Actualizar peso actual del usuario
                    db.collection("users")
                            .document(userId)
                            .update("weight", newWeight)
                            .addOnSuccessListener(aVoid -> {
                                updateCurrentWeight(newWeight);
                                loadWeightHistory();
                                Toast.makeText(this, "Peso actualizado correctamente", Toast.LENGTH_SHORT).show();
                            });
                });
    }

    private void updateCurrentWeight(double weight) {
        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
    }
} 