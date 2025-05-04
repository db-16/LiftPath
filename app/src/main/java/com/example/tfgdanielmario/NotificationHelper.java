package com.example.tfgdanielmario;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import java.util.concurrent.TimeUnit;

public class NotificationHelper {

    private static final String CHANNEL_ID = "fitness_app_channel";
    private static final int WEEKLY_ROUTINE_NOTIFICATION_ID = 1001;
    private static final int MONTHLY_WEIGHT_NOTIFICATION_ID = 1002;

    private Context context;

    public NotificationHelper(Context context) {
        this.context = context;
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Fitness App Notifications";
            String description = "Notifications for weekly routines and monthly weight input";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void scheduleNotifications() {
        // Schedule weekly routine notification every 7 days
        PeriodicWorkRequest weeklyRoutineWork =
                new PeriodicWorkRequest.Builder(WeeklyRoutineWorker.class, 7, TimeUnit.DAYS)
                        .build();

        // Schedule monthly weight input notification every 30 days
        PeriodicWorkRequest monthlyWeightWork =
                new PeriodicWorkRequest.Builder(MonthlyWeightWorker.class, 30, TimeUnit.DAYS)
                        .build();

        WorkManager.getInstance(context).enqueue(weeklyRoutineWork);
        WorkManager.getInstance(context).enqueue(monthlyWeightWork);
    }

    public static class WeeklyRoutineWorker extends Worker {

        public WeeklyRoutineWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            sendNotification("New Weekly Routine", "Your new weekly training routine has started. Check it out!");
            return Result.success();
        }

        private void sendNotification(String title, String message) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(WEEKLY_ROUTINE_NOTIFICATION_ID, builder.build());
        }
    }

    public static class MonthlyWeightWorker extends Worker {

        public MonthlyWeightWorker(Context context, WorkerParameters params) {
            super(context, params);
        }

        @Override
        public Result doWork() {
            sendNotification("Monthly Weight Input", "Please enter your weight for this month to track your progress.");
            return Result.success();
        }

        private void sendNotification(String title, String message) {
            Context context = getApplicationContext();
            Intent intent = new Intent(context, Profile.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_dialog_info)
                    .setContentTitle(title)
                    .setContentText(message)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            if (ActivityCompat.checkSelfPermission(this.getApplicationContext(), android.Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            notificationManager.notify(MONTHLY_WEIGHT_NOTIFICATION_ID, builder.build());
        }
    }
}
