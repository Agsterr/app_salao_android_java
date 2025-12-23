package com.focodevsistemas.gerenciamento;

import android.content.Context;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.util.Calendar;
import java.text.NumberFormat;
import java.util.Locale;

public class AgendaTotaisActivity extends AppCompatActivity {

    private Button buttonVoltar;
    private TextView textTotalDia;
    private TextView textTotalSemana;
    private TextView textTotalMes;
    private TextView textTotalAno;
    private AgendamentoDAO agendamentoDAO;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Verificar acesso antes de abrir a Activity
        FeatureGate featureGate = new FeatureGate(this);
        if (!featureGate.checkAccessAndBlock(this, "Relatórios", featureGate.canAccessReports())) {
            // Acesso bloqueado - dialog já foi exibido, fechar Activity
            finish();
            return;
        }

        setContentView(R.layout.activity_agenda_totais);

        setupActionBar();
        setupDao();
        bindViews();
        setupListeners();

        atualizarTotais();
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Totais e Lucros");
        }
    }

    private void setupDao() {
        agendamentoDAO = new AgendamentoDAO(this);
        agendamentoDAO.open();
    }

    private void bindViews() {
        buttonVoltar = findViewById(R.id.buttonVoltar);
        textTotalDia = findViewById(R.id.textTotalDia);
        textTotalSemana = findViewById(R.id.textTotalSemana);
        textTotalMes = findViewById(R.id.textTotalMes);
        textTotalAno = findViewById(R.id.textTotalAno);
    }

    private void setupListeners() {
        if (buttonVoltar != null) {
            buttonVoltar.setOnClickListener(v -> finish());
        }
    }

    private void atualizarTotais() {
        long dataAtual = System.currentTimeMillis();
        
        // Dia
        long inicioDia = getTimestampInicioDoDia(dataAtual);
        long fimDia = inicioDia + (24L * 60 * 60 * 1000) - 1;
        double totalDia = agendamentoDAO.getTotalValorPeriodo(inicioDia, fimDia);
        if (Double.isNaN(totalDia)) totalDia = 0.0;

        // Semana (segunda a domingo)
        Calendar calSemana = Calendar.getInstance();
        calSemana.setTimeInMillis(dataAtual);
        int diaDaSemana = calSemana.get(Calendar.DAY_OF_WEEK);
        int diasParaSegunda = (diaDaSemana == Calendar.SUNDAY) ? -6 : Calendar.MONDAY - diaDaSemana;
        calSemana.add(Calendar.DAY_OF_MONTH, diasParaSegunda);
        long inicioSemana = getTimestampInicioDoDia(calSemana.getTimeInMillis());
        long fimSemana = inicioSemana + (7L * 24 * 60 * 60 * 1000) - 1;
        double totalSemana = agendamentoDAO.getTotalValorPeriodo(inicioSemana, fimSemana);
        if (Double.isNaN(totalSemana)) totalSemana = 0.0;

        // Mês
        Calendar calMes = Calendar.getInstance();
        calMes.setTimeInMillis(dataAtual);
        calMes.set(Calendar.DAY_OF_MONTH, 1);
        long inicioMes = getTimestampInicioDoDia(calMes.getTimeInMillis());
        calMes.add(Calendar.MONTH, 1);
        long inicioProximoMes = getTimestampInicioDoDia(calMes.getTimeInMillis());
        long fimMes = inicioProximoMes - 1;
        double totalMes = agendamentoDAO.getTotalValorPeriodo(inicioMes, fimMes);
        if (Double.isNaN(totalMes)) totalMes = 0.0;

        // Ano
        Calendar calAno = Calendar.getInstance();
        calAno.setTimeInMillis(dataAtual);
        calAno.set(Calendar.MONTH, Calendar.JANUARY);
        calAno.set(Calendar.DAY_OF_MONTH, 1);
        long inicioAno = getTimestampInicioDoDia(calAno.getTimeInMillis());
        calAno.add(Calendar.YEAR, 1);
        long inicioProximoAno = getTimestampInicioDoDia(calAno.getTimeInMillis());
        long fimAno = inicioProximoAno - 1;
        double totalAno = agendamentoDAO.getTotalValorPeriodo(inicioAno, fimAno);
        if (Double.isNaN(totalAno)) totalAno = 0.0;

        setTotalText(textTotalDia, "Total do Dia:", totalDia);
        setTotalText(textTotalSemana, "Total da Semana:", totalSemana);
        setTotalText(textTotalMes, "Total do Mês:", totalMes);
        setTotalText(textTotalAno, "Total do Ano:", totalAno);
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

    private void setTotalText(TextView tv, String label, double value) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt","BR"));
        String formatted = nf.format(value);
        String full = label + " " + formatted;
        SpannableString ss = new SpannableString(full);
        int start = full.lastIndexOf(formatted);
        if (start >= 0) {
            int valueColor = ContextCompat.getColor(tv.getContext(), R.color.primary);
            ss.setSpan(new ForegroundColorSpan(valueColor), start, start + formatted.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        tv.setText(ss);
    }

    @Override
    protected void onResume() {
        super.onResume();
        atualizarTotais();
    }

    @Override
    protected void onDestroy() {
        if (agendamentoDAO != null) {
            agendamentoDAO.close();
        }
        super.onDestroy();
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

