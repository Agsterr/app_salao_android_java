package com.example.appdetestes;

import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AgendaEntry {
    private String title;
    private long timestamp; // millis since epoch
    private String status; // "Em andamento" ou "Concluído"
    private String endereco; // endereço textual
    private int reminderPrevDayHour = 20;
    private int reminderPrevDayMinute = 0;

    public AgendaEntry(String title, long timestamp) {
        this.title = title;
        this.timestamp = timestamp;
        this.status = "Em andamento";
        this.endereco = "";
        this.reminderPrevDayHour = 20;
        this.reminderPrevDayMinute = 0;
    }

    public AgendaEntry(String title, long timestamp, String status, String endereco) {
        this.title = title;
        this.timestamp = timestamp;
        this.status = status == null || status.isEmpty() ? "Em andamento" : status;
        this.endereco = endereco == null ? "" : endereco;
        this.reminderPrevDayHour = 20;
        this.reminderPrevDayMinute = 0;
    }

    // Novo construtor com hora/minuto do lembrete do dia anterior
    public AgendaEntry(String title, long timestamp, String status, String endereco, int reminderPrevDayHour, int reminderPrevDayMinute) {
        this.title = title;
        this.timestamp = timestamp;
        this.status = status == null || status.isEmpty() ? "Em andamento" : status;
        this.endereco = endereco == null ? "" : endereco;
        this.reminderPrevDayHour = reminderPrevDayHour;
        this.reminderPrevDayMinute = reminderPrevDayMinute;
    }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }
    public int getReminderPrevDayHour() { return reminderPrevDayHour; }
    public void setReminderPrevDayHour(int reminderPrevDayHour) { this.reminderPrevDayHour = reminderPrevDayHour; }
    public int getReminderPrevDayMinute() { return reminderPrevDayMinute; }
    public void setReminderPrevDayMinute(int reminderPrevDayMinute) { this.reminderPrevDayMinute = reminderPrevDayMinute; }

    public JSONObject toJson() throws JSONException {
        JSONObject obj = new JSONObject();
        obj.put("title", title);
        obj.put("timestamp", timestamp);
        obj.put("status", status);
        obj.put("endereco", endereco);
        obj.put("reminderPrevDayHour", reminderPrevDayHour);
        obj.put("reminderPrevDayMinute", reminderPrevDayMinute);
        return obj;
    }

    public static AgendaEntry fromJson(JSONObject obj) throws JSONException {
        String t = obj.optString("title", "");
        long ts = obj.optLong("timestamp", System.currentTimeMillis());
        String st = obj.optString("status", "Em andamento");
        String end = obj.optString("endereco", "");
        int rph = obj.optInt("reminderPrevDayHour", 20);
        int rpm = obj.optInt("reminderPrevDayMinute", 0);
        return new AgendaEntry(t, ts, st, end, rph, rpm);
    }

    @Override
    public String toString() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        String base = sdf.format(new Date(timestamp)) + " - " + title;
        if (status != null && !status.isEmpty()) {
            base += " (" + status + ")";
        }
        if (endereco != null && !endereco.isEmpty()) {
            base += " • " + endereco;
        }
        return base;
    }
}