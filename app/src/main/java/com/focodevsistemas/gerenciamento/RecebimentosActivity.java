package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.ContextMenu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import android.widget.TextView;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.graphics.Color;

public class RecebimentosActivity extends AppCompatActivity {

    private Button buttonVerAReceber;
    private Button buttonVerPagos;
    private Button buttonVoltarRecebimentos;
    private ListView listViewRecebimentos;

    private RecebimentoDAO recebimentoDAO;
    private ArrayAdapter<CharSequence> recebimentosAdapter;
    private int listaAtualStatus = RecebimentoDAO.STATUS_A_RECEBER;
    private ClienteDAO clienteDAO;
    private VendaDAO vendaDAO;
    private java.util.Map<Long, Boolean> gruposExpandidos = new java.util.HashMap<>();
    private java.util.List<Object> linhasRenderizadas = new java.util.ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recebimentos);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Recebimentos");
        }

        recebimentoDAO = new RecebimentoDAO(this);
        recebimentoDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        vendaDAO = new VendaDAO(this);
        vendaDAO.open();

        buttonVerAReceber = findViewById(R.id.buttonVerAReceber);
        buttonVerPagos = findViewById(R.id.buttonVerPagos);
        buttonVoltarRecebimentos = findViewById(R.id.buttonVoltarRecebimentos);
        listViewRecebimentos = findViewById(R.id.listViewRecebimentos);

        buttonVerAReceber.setOnClickListener(v -> {
            listaAtualStatus = RecebimentoDAO.STATUS_A_RECEBER;
            gruposExpandidos.clear();
            atualizarListaRecebimentos();
        });
        buttonVerPagos.setOnClickListener(v -> {
            listaAtualStatus = RecebimentoDAO.STATUS_PAGO;
            gruposExpandidos.clear();
            atualizarListaRecebimentos();
        });

        buttonVoltarRecebimentos.setOnClickListener(v -> finish());
        
        // Botão Exportar PDF de Recebimentos
        Button buttonExportarPDF = findViewById(R.id.buttonExportarPDFRecebimentos);
        if (buttonExportarPDF != null) {
            buttonExportarPDF.setOnClickListener(v -> exportarPDFRecebimentos());
            
            // Configurar UI baseado no plano (premium ou free)
            PlanManager planManager = PlanManager.getInstance(this);
            boolean isPremium = planManager.isPremium();
            if (!isPremium) {
                buttonExportarPDF.setAlpha(0.5f);
                buttonExportarPDF.setEnabled(true); // Mantém habilitado para mostrar o bloqueio
            }
        }

        registerForContextMenu(listViewRecebimentos);

        listViewRecebimentos.setOnItemClickListener((parent, view, position, id) -> {
            if (linhasRenderizadas == null || position < 0 || position >= linhasRenderizadas.size()) {
                return;
            }
            Object row = linhasRenderizadas.get(position);
            if (row instanceof Long) {
                Long cid = (Long) row;
                boolean expanded = gruposExpandidos.getOrDefault(cid, false);
                gruposExpandidos.put(cid, !expanded);
                atualizarListaRecebimentos();
            }
        });

        atualizarListaRecebimentos();
    }

    private void exportarPDFRecebimentos() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Exportar PDF de Recebimentos");
            return;
        }
        
        new Thread(() -> {
            try {
                List<Recebimento> recebimentos = recebimentoDAO.getPorStatus(listaAtualStatus);
                if (recebimentos.isEmpty()) {
                    runOnUiThread(() -> {
                        String mensagem = (listaAtualStatus == RecebimentoDAO.STATUS_A_RECEBER) ?
                            "Nenhum valor a receber encontrado para exportar." :
                            "Nenhum valor recebido encontrado para exportar.";
                        Toast.makeText(this, mensagem, Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                File pdfFile = PDFGeneratorHelper.gerarPDFRecebimentos(this, recebimentos, listaAtualStatus, vendaDAO, clienteDAO);
                
                runOnUiThread(() -> {
                    Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), Toast.LENGTH_LONG).show();
                    PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                });
            } catch (Exception e) {
                android.util.Log.e("RecebimentosActivity", "Erro ao gerar PDF", e);
                runOnUiThread(() -> {
                    Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }

    @Override
    protected void onDestroy() {
        recebimentoDAO.close();
        if (clienteDAO != null) clienteDAO.close();
        if (vendaDAO != null) vendaDAO.close();
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

    private static class StyledArrayAdapter extends ArrayAdapter<CharSequence> {
        public StyledArrayAdapter(@NonNull android.content.Context context, int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull android.view.ViewGroup parent) {
            View view = super.getView(position, convertView, parent);
            TextView tv = view.findViewById(android.R.id.text1);
            CharSequence item = getItem(position);
            if (tv != null && item != null) {
                tv.setText(item);
            }
            return view;
        }
    }

    private void atualizarListaRecebimentos() {
        List<Recebimento> lista = recebimentoDAO.getPorStatus(listaAtualStatus);
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        if (recebimentosAdapter == null) {
            recebimentosAdapter = new StyledArrayAdapter(this, android.R.layout.simple_list_item_1);
            listViewRecebimentos.setAdapter(recebimentosAdapter);
        }
        recebimentosAdapter.clear();
        linhasRenderizadas.clear();

        // Agrupar por cliente
        java.util.Map<Long, java.util.List<Recebimento>> porCliente = new java.util.HashMap<>();
        for (Recebimento r : lista) {
            Venda venda = vendaDAO.getVendaById(r.getVendaId());
            Long cid = (venda != null) ? venda.getClienteId() : null;
            if (cid == null) {
                cid = -1L; // grupo de desconhecidos
            }
            java.util.List<Recebimento> lr = porCliente.get(cid);
            if (lr == null) {
                lr = new java.util.ArrayList<>();
                porCliente.put(cid, lr);
            }
            lr.add(r);
        }
        // Ordenar clientes por nome
        java.util.List<Long> clienteIdsOrdenados = new java.util.ArrayList<>(porCliente.keySet());
        java.util.Collections.sort(clienteIdsOrdenados, (id1, id2) -> {
            String n1 = "";
            String n2 = "";
            if (id1 != -1L) {
                Cliente c1 = clienteDAO.getClienteById(id1);
                n1 = c1 != null ? c1.getNome() : "";
            } else {
                n1 = "Cliente desconhecido";
            }
            if (id2 != -1L) {
                Cliente c2 = clienteDAO.getClienteById(id2);
                n2 = c2 != null ? c2.getNome() : "";
            } else {
                n2 = "Cliente desconhecido";
            }
            return n1.compareToIgnoreCase(n2);
        });

        for (Long cid : clienteIdsOrdenados) {
            String nomeCliente;
            if (cid != -1L) {
                Cliente c = clienteDAO.getClienteById(cid);
                nomeCliente = (c != null && c.getNome() != null && !c.getNome().isEmpty()) ? c.getNome() : "(Cliente)";
            } else {
                nomeCliente = "Cliente desconhecido";
            }
            // Cabeçalho do cliente
            SpannableStringBuilder header = new SpannableStringBuilder(nomeCliente);
            header.setSpan(new ForegroundColorSpan(Color.RED), 0, nomeCliente.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            recebimentosAdapter.add(header);
            linhasRenderizadas.add(cid);

            boolean expanded = gruposExpandidos.getOrDefault(cid, false);
            if (expanded) {
                java.util.List<Recebimento> lr = porCliente.get(cid);
                // Ordenar por data prevista crescente
                java.util.Collections.sort(lr, (a, b) -> Long.compare(a.getDataPrevista(), b.getDataPrevista()));
                for (Recebimento r : lr) {
                    String base = "Parcela " + r.getNumeroParcela() + " - " + nf.format(r.getValor()) +
                            " - Prevista: " + sdf.format(r.getDataPrevista());
                    String statusStr = (r.getStatus() == RecebimentoDAO.STATUS_PAGO) ? "Pago" : "Pendente";
                    String full = base + " • Status: " + statusStr;
                    SpannableStringBuilder sb = new SpannableStringBuilder(full);
                    int start = full.lastIndexOf(statusStr);
                    int end = start + statusStr.length();
                    int color = (r.getStatus() == RecebimentoDAO.STATUS_PAGO) ? Color.parseColor("#4CAF50") : Color.parseColor("#E53935");
                    sb.setSpan(new ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                    recebimentosAdapter.add(sb);
                    linhasRenderizadas.add(r);
                }
                recebimentosAdapter.add(" ");
                linhasRenderizadas.add(null);
            }
        }

        recebimentosAdapter.notifyDataSetChanged();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        if (v.getId() == R.id.listViewRecebimentos) {
            getMenuInflater().inflate(R.menu.menu_contexto_recebimento, menu);
        }
    }

    @Override
    public boolean onContextItemSelected(@NonNull MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        int position = info.position;
        if (item.getItemId() == R.id.menu_marcar_pago) {
            if (listaAtualStatus == RecebimentoDAO.STATUS_PAGO) {
                Toast.makeText(this, "Já está pago", Toast.LENGTH_SHORT).show();
                return true;
            }
            Object row = (linhasRenderizadas != null && position < linhasRenderizadas.size()) ? linhasRenderizadas.get(position) : null;
            if (!(row instanceof Recebimento)) {
                Toast.makeText(this, "Selecione uma parcela para marcar como paga", Toast.LENGTH_SHORT).show();
                return true;
            }
            Recebimento r = (Recebimento) row;
            int rows = recebimentoDAO.marcarComoPago(r.getId(), System.currentTimeMillis());
            if (rows > 0) {
                Toast.makeText(this, "Marcado como pago", Toast.LENGTH_SHORT).show();
                atualizarListaRecebimentos();
            } else {
                Toast.makeText(this, "Falha ao marcar como pago", Toast.LENGTH_SHORT).show();
            }
            return true;
        }
        return super.onContextItemSelected(item);
    }
}
