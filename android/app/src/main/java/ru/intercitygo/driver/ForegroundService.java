package ru.intercitygo.driver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.util.Log;
import androidx.core.app.NotificationCompat;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * ForegroundService — фоновый сервис, который:
 * 1. Держит приложение живым, когда телефон спит
 * 2. Показывает уведомление "ПОДАЧА — Водитель онлайн"
 * 3. Каждые 10 секунд получает координаты GPS и отправляет на сервер
 * 4. Работает, даже когда экран выключен
 */
public class ForegroundService extends Service {

    private static final String TAG = "ForegroundService";
    private static final String CHANNEL_ID = "driver_foreground";
    private static final int NOTIFICATION_ID = 1;

    private LocationManager locationManager;
    private PowerManager.WakeLock wakeLock;
    private Handler handler;
    private Runnable locationTask;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();

        // Показываем уведомление (обязательно для foreground service)
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(
                this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE
        );

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("ПОДАЧА")
                .setContentText("Водитель онлайн")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(NOTIFICATION_ID, notification);

        // WakeLock — не даёт процессору заснуть полностью
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "DriverApp::WakeLock");
        wakeLock.acquire(3600000); // держим процессор частично активным на час

        // GPS-провайдер
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        handler = new Handler(Looper.getMainLooper());
        locationTask = new Runnable() {
            @Override
            public void run() {
                requestLocationAndSend();
                handler.postDelayed(this, 10000); // раз в 10 секунд
            }
        };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Запускаем цикл
        handler.post(locationTask);
        return START_STICKY; // если сервис убьёт — он перезапустится сам
    }

    private void requestLocationAndSend() {
        try {
            // Пробуем получить последнюю известную позицию и через GPS
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        sendLocationToServer(location.getLatitude(), location.getLongitude());
                    }
                    @Override public void onStatusChanged(String p, int s, Bundle b) {}
                    @Override public void onProviderEnabled(String p) {}
                    @Override public void onProviderDisabled(String p) {}
                }, Looper.getMainLooper());
            } else {
                // Если GPS выключен — пробуем сетевую геолокацию
                Location last = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                if (last != null) {
                    sendLocationToServer(last.getLatitude(), last.getLongitude());
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Нет разрешений на геолокацию: " + e.getMessage());
        }
    }

    /**
     * Отправляет координаты на сервер через HTTP POST.
     * Использует тот же API, что и водительское PWA: /api/driver/location.php
     */
    private void sendLocationToServer(double lat, double lng) {
        new Thread(() -> {
            try {
                URL url = new URL("https://intercitygo.ru/api/driver/heartbeat.php?lat=" + lat + "&lng=" + lng);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestProperty("Cookie", MainActivity.getSessionCookie());
                int code = conn.getResponseCode();
                if (code == 200) {
                    Log.d(TAG, "Координаты отправлены: " + lat + ", " + lng);
                } else {
                    Log.w(TAG, "Сервер вернул: " + code);
                }
                conn.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Ошибка отправки координат: " + e.getMessage());
            }
        }).start();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Водитель онлайн",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Уведомление о работе в фоне");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(locationTask);
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}