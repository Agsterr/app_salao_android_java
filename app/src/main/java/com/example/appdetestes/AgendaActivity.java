package com.example.appdetestes;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class AgendaActivity extends AppCompatActivity {

    private Button buttonVoltar;
    private Button buttonCalendario;
    private Button buttonTotais;
    private Button buttonAgendamentos;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_agenda_menu);

        setupActionBar();
        bindViews();
        setupListeners();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Agenda");
        }
    }

    private void bindViews() {
        buttonVoltar = findViewById(R.id.buttonVoltar);
        buttonCalendario = findViewById(R.id.buttonCalendario);
        buttonTotais = findViewById(R.id.buttonTotais);
        buttonAgendamentos = findViewById(R.id.buttonAgendamentos);
    }

    private void setupListeners() {
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }

        buttonCalendario.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendaCalendarioActivity.class);
            startActivity(intent);
        });

        buttonTotais.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendaTotaisActivity.class);
            startActivity(intent);
        });

        buttonAgendamentos.setOnClickListener(v -> {
            Intent intent = new Intent(this, AgendaListaActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
