package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Activity para criar e gerenciar orçamentos de serviços e produtos.
 */
public class OrcamentosActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private OrcamentoDAO orcamentoDAO;
    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    private ProdutoDAO produtoDAO;
    
    private RadioGroup radioGroupTipoOrcamento;
    private RadioButton radioServico;
    private RadioButton radioProduto;
    private Spinner spinnerCliente;
    private TextInputEditText editTextObservacoes;
    private ListView listViewItens;
    private TextView textValorTotal;
    
    private List<Cliente> listaClientes;
    private ArrayAdapter<Cliente> clienteAdapter;
    private ArrayAdapter<String> itensAdapter;
    
    private Orcamento orcamentoAtual;
    private String tipoOrcamento = "SERVICO"; // "SERVICO" ou "PRODUTO"

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_orcamentos);
        
        setupActionBar();
        setupDAOs();
        bindViews();
        setupSpinners();
        setupListeners();
        setupPremiumUI();
        
        inicializarOrcamento();
    }
    
    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Orçamentos");
        }
    }
    
    private void setupDAOs() {
        orcamentoDAO = new OrcamentoDAO(this);
        orcamentoDAO.open();
        clienteDAO = new ClienteDAO(this);
        clienteDAO.open();
        servicoDAO = new ServicoDAO(this);
        servicoDAO.open();
        produtoDAO = new ProdutoDAO(this);
        produtoDAO.open();
    }
    
    private void bindViews() {
        radioGroupTipoOrcamento = findViewById(R.id.radioGroupTipoOrcamento);
        radioServico = findViewById(R.id.radioServico);
        radioProduto = findViewById(R.id.radioProduto);
        spinnerCliente = findViewById(R.id.spinnerCliente);
        editTextObservacoes = findViewById(R.id.editTextObservacoes);
        listViewItens = findViewById(R.id.listViewItens);
        textValorTotal = findViewById(R.id.textValorTotal);
    }
    
    private void setupSpinners() {
        // Spinner de cliente
        listaClientes = new ArrayList<>();
        listaClientes.add(new Cliente()); // Opção "Selecione um cliente"
        listaClientes.get(0).setNome("Selecione um cliente");
        listaClientes.addAll(clienteDAO.getAllClientes());
        
        clienteAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, listaClientes) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaClientes.get(position).getNome());
                return view;
            }
            
            @Override
            public View getDropDownView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setText(listaClientes.get(position).getNome());
                return view;
            }
        };
        clienteAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCliente.setAdapter(clienteAdapter);
        
        // ListView de itens
        itensAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_list_item_1, new ArrayList<>());
        listViewItens.setAdapter(itensAdapter);
        
        // Permitir remover itens com toque longo
        listViewItens.setOnItemLongClickListener((parent, view, position, id) -> {
            removerItem(position);
            return true;
        });
    }
    
    private void setupListeners() {
        radioGroupTipoOrcamento.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioServico) {
                tipoOrcamento = "SERVICO";
            } else if (checkedId == R.id.radioProduto) {
                tipoOrcamento = "PRODUTO";
            }
            // Limpar itens ao mudar tipo
            orcamentoAtual.getItensServicos().clear();
            orcamentoAtual.getItensProdutos().clear();
            atualizarListaItens();
        });
        
        findViewById(R.id.buttonAdicionarItem).setOnClickListener(v -> mostrarDialogAdicionarItem());
        findViewById(R.id.buttonGerarPDF).setOnClickListener(v -> gerarPDFOrcamento());
    }
    
    private void setupPremiumUI() {
        featureGate = new FeatureGate(this);
        PlanManager planManager = PlanManager.getInstance(this);
        boolean isPremium = planManager.isPremium();
        
        com.google.android.material.card.MaterialCardView cardAviso = findViewById(R.id.cardAvisoPremium);
        if (cardAviso != null) {
            cardAviso.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        }
        
        if (!isPremium) {
            android.widget.Toast.makeText(this, 
                "Você está visualizando em modo FREE. Faça upgrade para usar todas as funcionalidades.", 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }
    
    private void inicializarOrcamento() {
        orcamentoAtual = new Orcamento();
        orcamentoAtual.setTipo("SERVICO");
        orcamentoAtual.setDataCriacao(System.currentTimeMillis());
        orcamentoAtual.setStatus(0); // Pendente
        atualizarListaItens();
    }
    
    private void mostrarDialogAdicionarItem() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Adicionar Item");
            return;
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Adicionar Item");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item_orcamento, null);
        builder.setView(dialogView);
        
        Spinner spinnerItem = dialogView.findViewById(R.id.spinnerItem);
        TextInputEditText editTextQuantidade = dialogView.findViewById(R.id.editTextQuantidade);
        TextInputEditText editTextValorUnitario = dialogView.findViewById(R.id.editTextValorUnitario);
        
        // Configurar spinner baseado no tipo
        if ("SERVICO".equals(tipoOrcamento)) {
            List<Servico> servicos = servicoDAO.getAllServicos();
            ArrayAdapter<Servico> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, servicos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerItem.setAdapter(adapter);
        } else {
            List<Produto> produtos = produtoDAO.getAllProdutos();
            ArrayAdapter<Produto> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, produtos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerItem.setAdapter(adapter);
            
            // Carregar preço automaticamente ao selecionar produto
            spinnerItem.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (position >= 0 && position < produtos.size()) {
                        Produto produto = produtos.get(position);
                        double precoVenda = produto.getPrecoVenda() > 0 ? produto.getPrecoVenda() : produto.getValorPadrao();
                        if (precoVenda > 0) {
                            editTextValorUnitario.setText(String.format(Locale.getDefault(), "%.2f", precoVenda));
                        }
                    }
                }
                
                @Override
                public void onNothingSelected(android.widget.AdapterView<?> parent) {}
            });
        }
        
        builder.setPositiveButton("Adicionar", (dialog, which) -> {
            int posicao = spinnerItem.getSelectedItemPosition();
            if (posicao < 0) {
                Toast.makeText(this, "Selecione um item", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String qtdStr = editTextQuantidade.getText().toString().trim();
            String valorStr = editTextValorUnitario.getText().toString().trim();
            
            if (qtdStr.isEmpty()) {
                editTextQuantidade.setError("Informe a quantidade");
                return;
            }
            
            if (valorStr.isEmpty()) {
                editTextValorUnitario.setError("Informe o valor unitário");
                return;
            }
            
            int quantidade;
            double valorUnitario;
            try {
                quantidade = Integer.parseInt(qtdStr);
                valorUnitario = Double.parseDouble(valorStr.replace(",", "."));
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Valores inválidos", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if ("SERVICO".equals(tipoOrcamento)) {
                Servico servico = (Servico) spinnerItem.getSelectedItem();
                Orcamento.OrcamentoItemServico item = new Orcamento.OrcamentoItemServico();
                item.setServicoId(servico.getId());
                item.setNomeServico(servico.getNome());
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                item.setValorTotal(quantidade * valorUnitario);
                orcamentoAtual.getItensServicos().add(item);
            } else {
                Produto produto = (Produto) spinnerItem.getSelectedItem();
                Orcamento.OrcamentoItemProduto item = new Orcamento.OrcamentoItemProduto();
                item.setProdutoId(produto.getId());
                item.setNomeProduto(produto.getNome());
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                item.setValorTotal(quantidade * valorUnitario);
                orcamentoAtual.getItensProdutos().add(item);
            }
            
            atualizarListaItens();
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    private void atualizarListaItens() {
        itensAdapter.clear();
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        double total = 0.0;
        
        if ("SERVICO".equals(tipoOrcamento)) {
            for (Orcamento.OrcamentoItemServico item : orcamentoAtual.getItensServicos()) {
                String linha = String.format("%s x%d - %s cada = %s",
                    item.getNomeServico(),
                    item.getQuantidade(),
                    nf.format(item.getValorUnitario()),
                    nf.format(item.getValorTotal()));
                itensAdapter.add(linha);
                total += item.getValorTotal();
            }
        } else {
            for (Orcamento.OrcamentoItemProduto item : orcamentoAtual.getItensProdutos()) {
                String linha = String.format("%s x%d - %s cada = %s",
                    item.getNomeProduto(),
                    item.getQuantidade(),
                    nf.format(item.getValorUnitario()),
                    nf.format(item.getValorTotal()));
                itensAdapter.add(linha);
                total += item.getValorTotal();
            }
        }
        
        orcamentoAtual.setValorTotal(total);
        textValorTotal.setText("Total: " + nf.format(total));
        itensAdapter.notifyDataSetChanged();
    }
    
    private void removerItem(int position) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remover Item");
        builder.setMessage("Deseja remover este item do orçamento?");
        builder.setPositiveButton("Remover", (dialog, which) -> {
            if ("SERVICO".equals(tipoOrcamento)) {
                if (position >= 0 && position < orcamentoAtual.getItensServicos().size()) {
                    orcamentoAtual.getItensServicos().remove(position);
                }
            } else {
                if (position >= 0 && position < orcamentoAtual.getItensProdutos().size()) {
                    orcamentoAtual.getItensProdutos().remove(position);
                }
            }
            atualizarListaItens();
            Toast.makeText(this, "Item removido", Toast.LENGTH_SHORT).show();
        });
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    private void gerarPDFOrcamento() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Gerar PDF");
            return;
        }
        
        // Validar
        if (spinnerCliente.getSelectedItemPosition() == 0) {
            Toast.makeText(this, "Selecione um cliente", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (("SERVICO".equals(tipoOrcamento) && orcamentoAtual.getItensServicos().isEmpty()) ||
            ("PRODUTO".equals(tipoOrcamento) && orcamentoAtual.getItensProdutos().isEmpty())) {
            Toast.makeText(this, "Adicione pelo menos um item", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Salvar orçamento
        Cliente cliente = listaClientes.get(spinnerCliente.getSelectedItemPosition());
        orcamentoAtual.setClienteId(cliente.getId());
        orcamentoAtual.setObservacoes(editTextObservacoes.getText().toString().trim());
        
        long orcamentoId = orcamentoDAO.inserirOrcamento(orcamentoAtual);
        
        if (orcamentoId > 0) {
            // Gerar PDF
            new Thread(() -> {
                try {
                    java.io.File pdfFile = PDFGeneratorHelper.gerarPDFOrcamento(this, orcamentoAtual, cliente);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                            Toast.LENGTH_LONG).show();
                        PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                        
                        // Limpar formulário
                        inicializarOrcamento();
                        spinnerCliente.setSelection(0);
                        editTextObservacoes.setText("");
                    });
                } catch (Exception e) {
                    android.util.Log.e("OrcamentosActivity", "Erro ao gerar PDF", e);
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), 
                            Toast.LENGTH_LONG).show();
                    });
                }
            }).start();
        } else {
            Toast.makeText(this, "Erro ao salvar orçamento", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        if (orcamentoDAO != null) {
            orcamentoDAO.close();
        }
        if (clienteDAO != null) {
            clienteDAO.close();
        }
        if (servicoDAO != null) {
            servicoDAO.close();
        }
        if (produtoDAO != null) {
            produtoDAO.close();
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

