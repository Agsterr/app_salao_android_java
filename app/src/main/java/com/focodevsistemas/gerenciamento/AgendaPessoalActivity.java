package com.focodevsistemas.gerenciamento;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Arrays;

public class AgendaPessoalActivity extends AppCompatActivity {

    private CalendarView calendarAgendaView;
    private Button buttonToggleCalendar;
    private Button buttonAdicionarAgendaInline;
    private Button buttonVoltar;
    private Spinner spinnerAgendaFilter;
    private ListView listViewAgenda;
    
    private ArrayAdapter<AgendaEntry> agendaAdapter;
    private List<AgendaEntry> agendaItems = new ArrayList<>();
    private List<AgendaEntry> agendaFilteredItems = new ArrayList<>();
    
    private static final String PREFS_AGENDA = "AgendaPrefs";
    private static final String PREF_AGENDA_ITEMS = "agenda_items";
    private long agendaSelectedDateStart;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_pessoal);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        bindViews();
        setupListeners();
        loadAgendaItems();
        
        agendaSelectedDateStart = getTimestampInicioDoDia(System.currentTimeMillis());
        registerForContextMenu(listViewAgenda);
    }

    private void bindViews() {
        calendarAgendaView = findViewById(R.id.calendarAgendaView);
        buttonToggleCalendar = findViewById(R.id.buttonToggleCalendar);
        buttonAdicionarAgendaInline = findViewById(R.id.buttonAdicionarAgendaInline);
        buttonVoltar = findViewById(R.id.buttonVoltar);
        spinnerAgendaFilter = findViewById(R.id.spinnerAgendaFilter);
        listViewAgenda = findViewById(R.id.listViewAgenda);
        
        agendaAdapter = new AgendaEntryAdapter(this, agendaFilteredItems);
        listViewAgenda.setAdapter(agendaAdapter);
    }

    private void setupListeners() {
        if (calendarAgendaView != null) {
            calendarAgendaView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
                Calendar c = Calendar.getInstance();
                c.set(year, month, dayOfMonth, 0, 0, 0);
                c.set(Calendar.MILLISECOND, 0);
                agendaSelectedDateStart = c.getTimeInMillis();
            });
        }

        if (buttonToggleCalendar != null && calendarAgendaView != null) {
            buttonToggleCalendar.setText("Fechar calendário");
            buttonToggleCalendar.setOnClickListener(v -> {
                if (calendarAgendaView.getVisibility() == View.VISIBLE) {
                    calendarAgendaView.setVisibility(View.GONE);
                    buttonToggleCalendar.setText("Mostrar calendário");
                } else {
                    calendarAgendaView.setVisibility(View.VISIBLE);
                    buttonToggleCalendar.setText("Fechar calendário");
                }
            });
        }

        buttonAdicionarAgendaInline.setOnClickListener(v -> mostrarDialogAdicionarAgendaInline());
        
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        if (spinnerAgendaFilter != null) {
            ArrayAdapter<String> filterAdapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item,
                Arrays.asList("Todos", "Em andamento", "Finalizado", "Cancelado"));
            filterAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerAgendaFilter.setAdapter(filterAdapter);
            spinnerAgendaFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    applyAgendaFilter();
                }
                @Override
                public void onNothingSelected(AdapterView<?> parent) {}
            });
        }
    }

    private long getTimestampInicioDoDia(long timestamp) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(timestamp);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTimeInMillis();
    }

    private void mostrarDialogAdicionarAgendaInline() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Novo compromisso");
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_agenda_inline, null);
        
        android.widget.EditText inputTitulo = dialogView.findViewById(R.id.editTextAgendaTitulo);
        Spinner spinnerStatus = dialogView.findViewById(R.id.spinnerAgendaStatus);
        android.widget.CheckBox checkBoxTemLocal = dialogView.findViewById(R.id.checkBoxTemLocal);
        android.widget.LinearLayout layoutEndereco = dialogView.findViewById(R.id.layoutEndereco);
        android.widget.EditText editTextEndereco = dialogView.findViewById(R.id.editTextAgendaEndereco);
        Button buttonAbrirMaps = dialogView.findViewById(R.id.buttonAbrirMaps);
        android.widget.TimePicker timePickerPrevDayReminder = dialogView.findViewById(R.id.timePickerPrevDayReminder);
        
        timePickerPrevDayReminder.setIs24HourView(true);
        if (android.os.Build.VERSION.SDK_INT >= 23) {
            timePickerPrevDayReminder.setHour(20);
            timePickerPrevDayReminder.setMinute(0);
        } else {
            timePickerPrevDayReminder.setCurrentHour(20);
            timePickerPrevDayReminder.setCurrentMinute(0);
        }

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, 
            Arrays.asList("Em andamento", "Finalizado", "Cancelado"));
        statusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerStatus.setAdapter(statusAdapter);

        checkBoxTemLocal.setOnCheckedChangeListener((buttonView, isChecked) -> {
            layoutEndereco.setVisibility(isChecked ? View.VISIBLE : View.GONE);
        });

        buttonAbrirMaps.setOnClickListener(v -> {
            String query = editTextEndereco.getText().toString().trim();
            if (query.isEmpty()) {
                Toast.makeText(this, "Digite um endereço para buscar", Toast.LENGTH_SHORT).show();
                return;
            }
            android.net.Uri gmmIntentUri = android.net.Uri.parse("https://www.google.com/maps/search/?api=1&query=" + android.net.Uri.encode(query));
            android.content.Intent mapIntent = new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri);
            try {
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
            } catch (Exception e) {
                startActivity(new android.content.Intent(android.content.Intent.ACTION_VIEW, gmmIntentUri));
            }
        });

        builder.setView(dialogView);
        builder.setPositiveButton("Escolher horário", null);
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.dismiss());
        
        AlertDialog alertDialog = builder.create();
        alertDialog.setOnShowListener(dlg -> {
            android.widget.Button positiveButton = alertDialog.getButton(AlertDialog.BUTTON_POSITIVE);
            positiveButton.setOnClickListener(v -> {
                String titulo = inputTitulo.getText().toString().trim();
                String status = spinnerStatus.getSelectedItem() != null ? spinnerStatus.getSelectedItem().toString().trim() : "";
                boolean temLocal = checkBoxTemLocal.isChecked();
                String endereco = temLocal ? editTextEndereco.getText().toString().trim() : "";
                
                if (titulo.isEmpty()) {
                    Toast.makeText(this, "Informe o título", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (status.isEmpty()) {
                    Toast.makeText(this, "Selecione o status", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (temLocal && endereco.isEmpty()) {
                    Toast.makeText(this, "Informe o endereço", Toast.LENGTH_SHORT).show();
                    return;
                }
                
                int reminderHour, reminderMinute;
                if (android.os.Build.VERSION.SDK_INT >= 23) {
                    reminderHour = timePickerPrevDayReminder.getHour();
                    reminderMinute = timePickerPrevDayReminder.getMinute();
                } else {
                    reminderHour = timePickerPrevDayReminder.getCurrentHour();
                    reminderMinute = timePickerPrevDayReminder.getCurrentMinute();
                }
                
                alertDialog.dismiss();
                selecionarHoraParaAgendaNovo(titulo, status, endereco, reminderHour, reminderMinute);
            });
        });
        alertDialog.show();
    }

    private void selecionarHoraParaAgendaNovo(String titulo, String status, String endereco, int reminderHour, int reminderMinute) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(agendaSelectedDateStart);
        TimePickerDialog tpd = new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {
            cal.set(Calendar.HOUR_OF_DAY, hourOfDay);
            cal.set(Calendar.MINUTE, minute);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            long ts = cal.getTimeInMillis();
            AgendaEntry entry = new AgendaEntry(titulo, ts, status, endereco, reminderHour, reminderMinute);
            agendaItems.add(entry);
            saveAgendaItems();
            applyAgendaFilter();
            Toast.makeText(this, "Compromisso adicionado", Toast.LENGTH_SHORT).show();
        }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true);
        tpd.show();
    }

    private void loadAgendaItems() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AGENDA, Context.MODE_PRIVATE);
        String json = prefs.getString(PREF_AGENDA_ITEMS, "[]");
        try {
            JSONArray arr = new JSONArray(json);
            agendaItems.clear();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                agendaItems.add(AgendaEntry.fromJson(obj));
            }
            if (agendaAdapter != null) applyAgendaFilter();
        } catch (JSONException e) {
            // ignora erro e mantém lista vazia
        }
    }

    private void applyAgendaFilter() {
        String filtro = "Todos";
        if (spinnerAgendaFilter != null && spinnerAgendaFilter.getSelectedItem() != null) {
            filtro = spinnerAgendaFilter.getSelectedItem().toString();
        }
        agendaFilteredItems.clear();
        for (AgendaEntry e : agendaItems) {
            String status = e.getStatus() == null ? "" : e.getStatus();
            if ("Todos".equals(filtro)) {
                agendaFilteredItems.add(e);
            } else if ("Cancelado".equals(filtro)) {
                if ("Cancelado".equals(status)) agendaFilteredItems.add(e);
            } else {
                if (!"Cancelado".equals(status) && filtro.equals(status)) agendaFilteredItems.add(e);
            }
        }
        agendaAdapter.notifyDataSetChanged();
    }

    private void saveAgendaItems() {
        SharedPreferences prefs = getSharedPreferences(PREFS_AGENDA, Context.MODE_PRIVATE);
        JSONArray arr = new JSONArray();
        for (AgendaEntry e : agendaItems) {
            try {
                arr.put(e.toJson());
            } catch (JSONException ignore) {}
        }
        prefs.edit().putString(PREF_AGENDA_ITEMS, arr.toString()).apply();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listViewAgenda) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            getMenuInflater().inflate(R.menu.menu_contexto_agenda_pessoal, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        if (info == null) return super.onContextItemSelected(item);
        
        AgendaEntry entry = agendaFilteredItems.get(info.position);
        
        if (item.getItemId() == R.id.menu_apagar_agenda_pessoal) {
            new AlertDialog.Builder(this)
                .setTitle("Confirmar Exclusão")
                .setMessage("Tem certeza que deseja apagar este compromisso?")
                .setPositiveButton("Sim", (dialog, which) -> {
                    agendaItems.remove(entry);
                    saveAgendaItems();
                    applyAgendaFilter();
                    Toast.makeText(this, "Compromisso apagado", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Não", null)
                .show();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadAgendaItems();
    }
}

