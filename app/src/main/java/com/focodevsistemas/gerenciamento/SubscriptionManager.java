package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.content.SharedPreferences;

public class SubscriptionManager {

    private static final String PREFS_NAME = "SubscriptionManagerPrefs";
    private static final String KEY_PLAN_TYPE = "plan_type";

    private static volatile SubscriptionManager instance;

    private final Context appContext;
    private volatile PlanType planType;

    private SubscriptionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.planType = readPlanTypeFromPrefs();
    }

    public static SubscriptionManager getInstance(Context context) {
        if (instance == null) {
            synchronized (SubscriptionManager.class) {
                if (instance == null) {
                    instance = new SubscriptionManager(context);
                }
            }
        }
        return instance;
    }

    public boolean isPremiumUser() {
        return getPlanType() == PlanType.PREMIUM;
    }

    public PlanType getPlanType() {
        return planType;
    }

    public void setPlanType(PlanType planType) {
        PlanType normalized = planType == null ? PlanType.FREE : planType;
        this.planType = normalized;
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_PLAN_TYPE, normalized.name()).apply();
    }

    private PlanType readPlanTypeFromPrefs() {
        SharedPreferences prefs = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String saved = prefs.getString(KEY_PLAN_TYPE, PlanType.FREE.name());
        try {
            return PlanType.valueOf(saved);
        } catch (Throwable t) {
            return PlanType.FREE;
        }
    }
}

