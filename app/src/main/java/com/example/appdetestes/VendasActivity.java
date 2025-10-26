package com.example.appdetestes;

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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", new Locale("pt", "BR"));
        String filtro = filtroAtual != null ? filtroAtual.trim().toLowerCase() : "";
        // Agrupa vendas por clienteId
        java.util.Map<Long, List<Venda>> porCliente = new java.util.HashMap<>();
        for (Venda v : vendas) {
            long clienteId = v.getClienteId();
            List<Venda> lista = porCliente.get(clienteId);
            if (lista == null) { lista = new ArrayList<>(); porCliente.put(clienteId, lista); }
            lista.add(v);
        }
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
            boolean clienteMatch = filtro.length() > 0 && clienteNome.toLowerCase().contains(filtro);
            List<Venda> vendasFiltradas = new ArrayList<>();
            if (filtro.length() == 0) {
                vendasFiltradas = vendasCliente;
            } else {
                if (clienteMatch) {
                    vendasFiltradas = vendasCliente;
                } else {
                    for (Venda v : vendasCliente) {
                        // Considerar itens: se produtoId==0, tenta buscar itens
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
                vendasAdapter.add("Cliente: " + clienteNome);
                vendaIdPosicoes.add(null);
                java.util.Collections.sort(vendasFiltradas, (v1, v2) -> Long.compare(v2.getDataVenda(), v1.getDataVenda()));
                for (Venda v : vendasFiltradas) {
                    String tipo = v.getTipoPagamento() == VendaDAO.TIPO_AVISTA ? "À vista" : "A prazo";
                    String data = sdf.format(new java.util.Date(v.getDataVenda()));
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
                        // fallback para produto único
                        Produto p = produtoDAO.getProdutoById(v.getProdutoId());
                        String produtoNome = p != null ? p.getNome() : "(Produto)";
                        itensResumo = produtoNome;
                    }
                    String linha = "- " + itensResumo +
                            " • Valor: " + nf.format(v.getValorTotal()) +
                            " • " + tipo + " • " + data;
                    vendasAdapter.add(linha);
                    vendaIdPosicoes.add(v.getId());
                }
                vendasAdapter.add(" ");
                vendaIdPosicoes.add(null);
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
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Custom adapter to color the client's name in red within each list item
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
                    int idx = s.indexOf("Cliente:");
                    if (idx >= 0) {
                        int start = idx + "Cliente:".length();
                        while (start < s.length() && Character.isWhitespace(s.charAt(start))) {
                            start++;
                        }
                        int end = s.length();
                        int endNewline = s.indexOf('\n', start);
                        int endDash = s.indexOf(" - ", start);
                        int endProduto = s.indexOf("Produto:", start);
                        if (endNewline >= 0) end = Math.min(end, endNewline);
                        if (endDash >= 0) end = Math.min(end, endDash);
                        if (endProduto >= 0) end = Math.min(end, endProduto);
                        if (end > start) {
                            android.text.SpannableStringBuilder ssb = new android.text.SpannableStringBuilder(current);
                            ssb.setSpan(new android.text.style.ForegroundColorSpan(android.graphics.Color.RED), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tv.setText(ssb);
                        }
                    }
                }
            }
            return view;
        }
    }
}