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
            }
            else if (itemId == R.id.navigation_chat) {
                startActivity(new Intent(this, ChatActivity.class));
                finish();
                return true;
            }
            else if (itemId == R.id.navigation_progress) {
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

    @Override
    protected void onResume() {
        super.onResume();
        // Reinicializar el gráfico antes de recargar los datos
        setupChart();
        // Recargar datos
        loadUserData();
    }

    private void setupChart() {
        weightChart.getDescription().setEnabled(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setDrawGridBackground(false);
        weightChart.setBackgroundColor(Color.TRANSPARENT);
        
        // Ajustar márgenes del gráfico
        weightChart.setExtraBottomOffset(15f);
        weightChart.setExtraTopOffset(15f);
        weightChart.setExtraLeftOffset(15f);
        weightChart.setExtraRightOffset(15f);

        // Configurar eje X
        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setTextSize(11f);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(7, true);
        xAxis.setLabelRotationAngle(-45f);
        xAxis.setYOffset(10f);
        xAxis.setDrawAxisLine(true);
        xAxis.setAxisLineColor(Color.WHITE);
        xAxis.setAxisLineWidth(1f);

        // Configurar eje Y con intervalos de 1 kg
        weightChart.getAxisLeft().setTextColor(Color.WHITE);
        weightChart.getAxisLeft().setTextSize(11f);
        weightChart.getAxisLeft().setDrawGridLines(true);
        weightChart.getAxisLeft().setGridColor(Color.parseColor("#20FFFFFF"));
        weightChart.getAxisLeft().setGridLineWidth(0.5f);
        weightChart.getAxisLeft().setGranularity(1f);
        weightChart.getAxisLeft().setDrawAxisLine(true);
        weightChart.getAxisLeft().setAxisLineColor(Color.WHITE);
        weightChart.getAxisLeft().setAxisLineWidth(1f);
        weightChart.getAxisLeft().setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return String.format(Locale.getDefault(), "%.0f", value);
            }
        });
        weightChart.getAxisRight().setEnabled(false);

        // Configurar leyenda
        weightChart.getLegend().setEnabled(false);
        
        // Habilitar marcadores
        weightChart.setDrawMarkers(true);
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
                        // Cargar el historial después de tener los datos del usuario
                        loadWeightHistory();
                    }
                });
    }

    private void loadWeightHistory() {
        if (weightChart == null) return;
        
        db.collection("weightHistory")
                .document(userId)
                .collection("records")
                .orderBy("date", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Entry> entries = new ArrayList<>();
                    List<String> dates = new ArrayList<>();
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM", Locale.getDefault());

                    // Obtener la fecha actual y crear un calendario para los últimos 10 días
                    Calendar endDate = Calendar.getInstance();
                    Calendar startDate = Calendar.getInstance();
                    startDate.add(Calendar.DAY_OF_MONTH, -9);

                    Map<String, Double> weightsByDate = new HashMap<>();
                    double lastKnownWeight = initialWeight;
                    double minWeight = Double.MAX_VALUE;
                    double maxWeight = Double.MIN_VALUE;

                    // Primero, encontrar el peso más antiguo en el rango de fechas como peso inicial
                    boolean foundInitialWeight = false;

                    for (int i = 0; i < queryDocumentSnapshots.size(); i++) {
                        Date date = queryDocumentSnapshots.getDocuments().get(i).getDate("date");
                        Double weight = queryDocumentSnapshots.getDocuments().get(i).getDouble("weight");
                        if (date != null && weight != null) {
                            Calendar recordDate = Calendar.getInstance();
                            recordDate.setTime(date);
                            
                            if (!recordDate.after(startDate)) {
                                lastKnownWeight = weight;
                                foundInitialWeight = true;
                            }

                            if (!recordDate.before(startDate) && !recordDate.after(endDate)) {
                                String dateStr = dateFormat.format(date);
                                weightsByDate.put(dateStr, weight);
                                minWeight = Math.min(minWeight, weight);
                                maxWeight = Math.max(maxWeight, weight);
                            }
                        }
                    }

                    if (!foundInitialWeight) {
                        lastKnownWeight = initialWeight;
                    }

                    if (minWeight == Double.MAX_VALUE) {
                        minWeight = lastKnownWeight;
                        maxWeight = lastKnownWeight;
                    }

                    minWeight = Math.min(minWeight, Math.min(initialWeight, goalWeight));
                    maxWeight = Math.max(maxWeight, Math.max(initialWeight, goalWeight));

                    double range = maxWeight - minWeight;
                    if (range < 1) range = 1;
                    minWeight = Math.floor(minWeight - (range * 0.1));
                    maxWeight = Math.ceil(maxWeight + (range * 0.1));

                    int index = 0;
                    Calendar currentDate = (Calendar) startDate.clone();
                    double currentWeight = lastKnownWeight;

                    while (!currentDate.after(endDate)) {
                        String dateStr = dateFormat.format(currentDate.getTime());
                        
                        if (weightsByDate.containsKey(dateStr)) {
                            currentWeight = weightsByDate.get(dateStr);
                            lastKnownWeight = currentWeight;
                        } else {
                            currentWeight = lastKnownWeight;
                        }

                        entries.add(new Entry(index, (float) currentWeight));
                        dates.add(dateStr);
                        index++;

                        currentDate.add(Calendar.DAY_OF_MONTH, 1);
                    }

                    // Actualizar el porcentaje de progreso con el último peso conocido
                    updateProgressPercentage(lastKnownWeight);

                    // Crear y configurar el dataset
                    LineDataSet dataSet = new LineDataSet(entries, "");
                    dataSet.setColor(Color.parseColor("#FF9800"));
                    dataSet.setCircleColor(Color.parseColor("#FF9800"));
                    dataSet.setCircleRadius(5f);
                    dataSet.setDrawCircleHole(true);
                    dataSet.setCircleHoleRadius(2.5f);
                    dataSet.setCircleHoleColor(Color.parseColor("#293038"));
                    dataSet.setValueTextColor(Color.WHITE);
                    dataSet.setValueTextSize(11f);
                    dataSet.setLineWidth(2.5f);
                    dataSet.setMode(LineDataSet.Mode.HORIZONTAL_BEZIER);
                    dataSet.setDrawFilled(true);
                    dataSet.setFillColor(Color.parseColor("#FF9800"));
                    dataSet.setFillAlpha(50);
                    dataSet.setDrawValues(true);
                    dataSet.setHighlightEnabled(true);
                    dataSet.setHighLightColor(Color.WHITE);
                    dataSet.setDrawHorizontalHighlightIndicator(false);
                    dataSet.setValueFormatter(new com.github.mikephil.charting.formatter.ValueFormatter() {
                        @Override
                        public String getFormattedValue(float value) {
                            return String.format(Locale.getDefault(), "%.1f", value);
                        }
                    });

                    LineData lineData = new LineData(dataSet);
                    weightChart.setData(lineData);

                    weightChart.getAxisLeft().setAxisMinimum((float) minWeight);
                    weightChart.getAxisLeft().setAxisMaximum((float) maxWeight);
                    weightChart.getAxisLeft().setLabelCount(5, true);

                    XAxis xAxis = weightChart.getXAxis();
                    xAxis.setValueFormatter(new IndexAxisValueFormatter(dates));
                    xAxis.setGranularity(1f);
                    xAxis.setLabelCount(dates.size(), true);
                    xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                    xAxis.setTextColor(Color.WHITE);
                    xAxis.setTextSize(10f);
                    xAxis.setLabelRotationAngle(-45f);
                    xAxis.setYOffset(5f);

                    weightChart.invalidate();
                })
                .addOnFailureListener(e -> Toast.makeText(this,
                    getString(R.string.error_loading_exercises), Toast.LENGTH_SHORT).show());
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
                            Toast.makeText(this, getString(R.string.weight_already_registered), Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(this, getString(R.string.enter_valid_weight), Toast.LENGTH_SHORT).show();
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
                                    if ((goalWeight > initialWeight && newWeight >= goalWeight) ||
                                        (goalWeight < initialWeight && newWeight <= goalWeight)) {
                                        // Objetivo alcanzado
                                        Toast.makeText(this, getString(R.string.congratulations_goal_reached), 
                                            Toast.LENGTH_LONG).show();
                                    }
                                }
                            })
                            .addOnFailureListener(e -> Toast.makeText(this, 
                                getString(R.string.error_updating_profile), Toast.LENGTH_SHORT).show());
                })
                .addOnFailureListener(e -> Toast.makeText(this, 
                    getString(R.string.error_saving_history), Toast.LENGTH_SHORT).show());
    }

    private void updateCurrentWeight(double weight) {
        currentWeight = weight;
        tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", weight));
    }

    private void updateProgressPercentage(double lastKnownWeight) {
        if (initialWeight != 0 && goalWeight != initialWeight) {
            double totalChange = goalWeight - initialWeight;
            double currentChange = lastKnownWeight - initialWeight;
            double progressPercentage = (currentChange / totalChange) * 100;

            // Limitar el progreso entre 0% y 100%
            progressPercentage = Math.min(100, Math.max(0, progressPercentage));
            int roundedPercentage = (int) Math.round(progressPercentage);

            // Siempre mostrar en verde
            tvProgressPercentage.setTextColor(Color.parseColor("#4CAF50"));
            
            // Formatear el texto del progreso
            tvProgressPercentage.setText(getString(R.string.progress_percentage, roundedPercentage));
        } else {
            tvProgressPercentage.setText(getString(R.string.progress_percentage, 0));
            tvProgressPercentage.setTextColor(Color.parseColor("#4CAF50"));
        }
    }
} 
