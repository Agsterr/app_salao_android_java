package com.example.appdetestes;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.text.TextUtils;

import androidx.core.app.NotificationCompat;

public class AgendaReminderReceiver extends BroadcastReceiver {
    public static final String CHANNEL_ID = "agenda_reminders";

    @Override
    public void onReceive(Context context, Intent intent) {
        String title = intent.getStringExtra("title");
        long timestamp = intent.getLongExtra("timestamp", 0L);
        String endereco = intent.getStringExtra("endereco");
        String type = intent.getStringExtra("type");

        String whenText = "Lembrete";
        if ("previous_day".equals(type)) {
            whenText = "Amanhã";
        } else if ("day_of".equals(type)) {
            whenText = "Em 1 hora e 30 min";
        }

        String contentText = !TextUtils.isEmpty(endereco)
                ? title + " — " + whenText + " • " + endereco
                : title + " — " + whenText;

        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = nm.getNotificationChannel(CHANNEL_ID);
            if (channel == null) {
                channel = new NotificationChannel(CHANNEL_ID, "Lembretes da Agenda", NotificationManager.IMPORTANCE_HIGH);
                channel.setDescription("Notificações de lembretes de compromissos");
                nm.createNotificationChannel(channel);
            }
        }

        Intent openIntent = new Intent(context, MenuActivity.class);
        openIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        android.app.PendingIntent contentPi = android.app.PendingIntent.getActivity(
                context,
                (int) (timestamp % Integer.MAX_VALUE),
                openIntent,
                android.app.PendingIntent.FLAG_UPDATE_CURRENT | android.app.PendingIntent.FLAG_IMMUTABLE
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_dialog_info)
                .setContentTitle("Lembrete de compromisso")
                .setContentText(contentText)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(contentText))
                .setAutoCancel(true)
                .setContentIntent(contentPi)
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        int notifyId = (int) ((timestamp % Integer.MAX_VALUE) + ("previous_day".equals(type) ? 1 : 2));
        nm.notify(notifyId, builder.build());
    }
}