package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Button;
import android.widget.EditText;
import android.view.MenuItem;
import android.text.TextWatcher;
import android.text.Editable;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;

public class VendasActivity extends AppCompatActivity {

    private ListView listViewVendas;
    private ArrayAdapter<String> vendasAdapter;
    private VendaDAO vendaDAO;
    private ProdutoDAO produtoDAO;
    private ClienteDAO clienteDAO;
    private Button buttonVoltarVendas;
    private EditText editTextBuscarVendas;
    private Button buttonNovaVenda;
    private VendaItemDAO vendaItemDAO;
    private List<Long> vendaIdPosicoes = new ArrayList<>();
    private String filtroAtual = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vendas);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Vendas");
        }

        listViewVendas = findViewById(R.id.listViewVendas);
        buttonVoltarVendas = findViewById(R.id.buttonVoltarVendas);
        editTextBuscarVendas = findViewById(R.id.editTextBuscarVendas);
        buttonNovaVenda = findViewById(R.id.buttonNovaVenda);

        vendaDAO = new VendaDAO(this);
        vendaDAO.open();
        produtoDAO = new ProdutoDAO(this);
        produtoDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        vendaItemDAO = new VendaItemDAO(this);
        vendaItemDAO.open();

        vendasAdapter = new ColoringArrayAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewVendas.setAdapter(vendasAdapter);

        buttonVoltarVendas.setOnClickListener(v -> finish());
        buttonNovaVenda.setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(this, RegistrarVendaMultiplaActivity.class);
            startActivity(intent);
        });

        // Pré-carregar filtro por cliente se vier da tela de recebimentos
        String nomeFiltroInicial = null;
        long clienteIdExtra = (getIntent() != null) ? getIntent().getLongExtra("cliente_id", -1) : -1;
        if (clienteIdExtra > 0) {
            Cliente cc = clienteDAO.getClienteById(clienteIdExtra);
            if (cc != null && cc.getNome() != null) {
                nomeFiltroInicial = cc.getNome();
            }
        }
        if (nomeFiltroInicial != null) {
            filtroAtual = nomeFiltroInicial;
            editTextBuscarVendas.setText(nomeFiltroInicial);
        }

        editTextBuscarVendas.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filtroAtual = s != null ? s.toString() : "";
                atualizarListaVendas();
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        atualizarListaVendas();

        listViewVendas.setOnItemClickListener((parent, view, position, id) -> {
            Long vendaId = (vendaIdPosicoes != null && position < vendaIdPosicoes.size()) ? vendaIdPosicoes.get(position) : null;
            if (vendaId != null) {
                android.content.Intent intent = new android.content.Intent(this, RegistrarVendaMultiplaActivity.class);
                intent.putExtra("venda_id", vendaId);
                startActivity(intent);
            }
        });
    }

    private void atualizarListaVendas() {
        List<Venda> vendas = vendaDAO.getAllVendas();
        vendasAdapter.clear();
        vendaIdPosicoes.clear();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        SimpleDateFormat sdfData = new SimpleDateFormat("dd/MM/yyyy", new Locale("pt", "BR"));
        SimpleDateFormat sdfHora = new SimpleDateFormat("HH:mm", new Locale("pt", "BR"));
        String filtro = filtroAtual != null ? filtroAtual.trim().toLowerCase() : "";
        
        // Agrupa por dia primeiro, depois por cliente
        java.util.Map<Long, java.util.Map<Long, List<Venda>>> porDiaECliente = new java.util.HashMap<>();
        
        for (Venda v : vendas) {
            // Obtém o timestamp do início do dia
            java.util.Calendar cal = java.util.Calendar.getInstance();
            cal.setTimeInMillis(v.getDataVenda());
            cal.set(java.util.Calendar.HOUR_OF_DAY, 0);
            cal.set(java.util.Calendar.MINUTE, 0);
            cal.set(java.util.Calendar.SECOND, 0);
            cal.set(java.util.Calendar.MILLISECOND, 0);
            long diaTimestamp = cal.getTimeInMillis();
            
            long clienteId = v.getClienteId();
            
            java.util.Map<Long, List<Venda>> porCliente = porDiaECliente.get(diaTimestamp);
            if (porCliente == null) {
                porCliente = new java.util.HashMap<>();
                porDiaECliente.put(diaTimestamp, porCliente);
            }
            
            List<Venda> lista = porCliente.get(clienteId);
            if (lista == null) {
                lista = new ArrayList<>();
                porCliente.put(clienteId, lista);
            }
            lista.add(v);
        }
        
        // Ordena dias (mais recente primeiro)
        List<Long> diasOrdenados = new ArrayList<>(porDiaECliente.keySet());
        java.util.Collections.sort(diasOrdenados, (d1, d2) -> Long.compare(d2, d1));
        
        for (Long diaTimestamp : diasOrdenados) {
            String dataFormatada = sdfData.format(new java.util.Date(diaTimestamp));
            java.util.Map<Long, List<Venda>> porCliente = porDiaECliente.get(diaTimestamp);
            
            // Ordena clientes por nome
            List<Long> clienteIdsOrdenados = new ArrayList<>(porCliente.keySet());
            java.util.Collections.sort(clienteIdsOrdenados, (id1, id2) -> {
                Cliente c1 = clienteDAO.getClienteById(id1);
                Cliente c2 = clienteDAO.getClienteById(id2);
                String n1 = c1 != null ? c1.getNome() : "";
                String n2 = c2 != null ? c2.getNome() : "";
                return n1.compareToIgnoreCase(n2);
            });
            
            for (Long clienteId : clienteIdsOrdenados) {
                Cliente c = clienteDAO.getClienteById(clienteId);
                String clienteNome = c != null ? c.getNome() : "(Cliente)";
                List<Venda> vendasCliente = porCliente.get(clienteId);
                
                // Aplica filtro
                boolean clienteMatch = filtro.length() > 0 && clienteNome.toLowerCase().contains(filtro);
                List<Venda> vendasFiltradas = new ArrayList<>();
                if (filtro.length() == 0) {
                    vendasFiltradas = vendasCliente;
                } else {
                    if (clienteMatch) {
                        vendasFiltradas = vendasCliente;
                    } else {
                        for (Venda v : vendasCliente) {
                            String textoBusca = "";
                            if (v.getProdutoId() != 0) {
                                Produto p = produtoDAO.getProdutoById(v.getProdutoId());
                                textoBusca = p != null ? p.getNome() : "";
                            } else {
                                List<VendaItem> itens = vendaItemDAO.getItensByVendaId(v.getId());
                                if (!itens.isEmpty()) {
                                    Produto p0 = produtoDAO.getProdutoById(itens.get(0).getProdutoId());
                                    textoBusca = p0 != null ? p0.getNome() : "";
                                }
                            }
                            if (textoBusca.toLowerCase().contains(filtro)) {
                                vendasFiltradas.add(v);
                            }
                        }
                    }
                }
                
                if (!vendasFiltradas.isEmpty()) {
                    // Cabeçalho: Dia • Cliente
                    vendasAdapter.add("📅 " + dataFormatada + " • 👤 " + clienteNome);
                    vendaIdPosicoes.add(null);
                    
                    // Ordena vendas por hora (mais recente primeiro)
                    java.util.Collections.sort(vendasFiltradas, (v1, v2) -> Long.compare(v2.getDataVenda(), v1.getDataVenda()));
                    
                    for (Venda v : vendasFiltradas) {
                        String tipo = v.getTipoPagamento() == VendaDAO.TIPO_AVISTA ? "À vista" : "A prazo";
                        String status = tipo; // Status é o tipo de pagamento
                        String hora = sdfHora.format(new java.util.Date(v.getDataVenda()));
                        
                        String itensResumo;
                        List<VendaItem> itens = vendaItemDAO.getItensByVendaId(v.getId());
                        if (!itens.isEmpty()) {
                            int count = itens.size();
                            Produto p0 = produtoDAO.getProdutoById(itens.get(0).getProdutoId());
                            String nome0 = p0 != null ? p0.getNome() : "Item";
                            if (count == 1) {
                                itensResumo = nome0 + " x" + itens.get(0).getQuantidade();
                            } else {
                                itensResumo = nome0 + " +" + (count - 1) + " itens";
                            }
                        } else {
                            Produto p = produtoDAO.getProdutoById(v.getProdutoId());
                            String produtoNome = p != null ? p.getNome() : "(Produto)";
                            itensResumo = produtoNome;
                        }
                        
                        String linha = "  🕐 " + hora + " • " + itensResumo +
                                " • Valor: " + nf.format(v.getValorTotal()) +
                                " • Status: " + status;
                        vendasAdapter.add(linha);
                        vendaIdPosicoes.add(v.getId());
                    }
                    vendasAdapter.add(" ");
                    vendaIdPosicoes.add(null);
                }
            }
        }
        vendasAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onDestroy() {
        if (vendaDAO != null) vendaDAO.close();
        if (produtoDAO != null) produtoDAO.close();
        if (clienteDAO != null) clienteDAO.close();
        if (vendaItemDAO != null) vendaItemDAO.close();
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

    // Custom adapter to color the headers (dia e cliente)
    private static class ColoringArrayAdapter extends android.widget.ArrayAdapter<String> {
        public ColoringArrayAdapter(android.content.Context context, int resource, java.util.List<String> objects) {
            super(context, resource, objects);
        }

        @Override
        public android.view.View getView(int position, android.view.View convertView, android.view.ViewGroup parent) {
            android.view.View view = super.getView(position, convertView, parent);
            android.widget.TextView tv = view.findViewById(android.R.id.text1);
            if (tv != null) {
                CharSequence current = tv.getText();
                if (current != null) {
                    String s = current.toString();
                    // Destaque para cabeçalhos (dia e cliente)
                    if (s.startsWith("📅")) {
                        tv.setTextSize(16);
                        tv.setTypeface(null, android.graphics.Typeface.BOLD);
                        android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder(current);
                        // Destaca a data em azul
                        int idxData = s.indexOf("📅");
                        int idxCliente = s.indexOf("👤");
                        if (idxData >= 0 && idxCliente > idxData) {
                            ssb.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#1976D2")), 
                                    idxData, idxCliente, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            // Destaca o cliente em vermelho
                            ssb.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#E53935")), 
                                    idxCliente, s.length(), android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                        }
                        tv.setText(ssb);
                    } else if (s.startsWith("  🕐")) {
                        // Itens de venda - texto normal
                        tv.setTextSize(14);
                        tv.setTypeface(null, android.graphics.Typeface.NORMAL);
                        // Destaca o status
                        int idxStatus = s.indexOf("Status:");
                        if (idxStatus >= 0) {
                            android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder(current);
                            String status = s.substring(idxStatus + "Status:".length()).trim();
                            int start = idxStatus + "Status:".length();
                            while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
                                start++;
                            }
                            int end = s.length();
                            int color = status.contains("À vista") ? 
                                    android.graphics.Color.parseColor("#4CAF50") : 
                                    android.graphics.Color.parseColor("#FF9800");
                            ssb.setSpan(new android.text.style.ForegroundColorSpan(color), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv.setText(ssb);
                        }
                    } else {
                        tv.setTextSize(14);
                        tv.setTypeface(null, android.graphics.Typeface.NORMAL);
                    }
                }
            }
            return view;
        }
    }
}
