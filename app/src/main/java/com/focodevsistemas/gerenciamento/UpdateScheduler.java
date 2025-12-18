package com.focodevsistemas.gerenciamento;

import android.content.Context;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public final class UpdateScheduler {
    private static final String UNIQUE_WORK_NAME = "daily_update_check";

    private UpdateScheduler() {}

    public static void scheduleDaily(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();

        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(UpdateCheckWorker.class, 1, TimeUnit.DAYS)
                .setConstraints(constraints)
                .build();

        WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.UPDATE, request);
    }
}
