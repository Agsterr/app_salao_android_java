package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.ConcatAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity para criar e gerenciar orçamentos de serviços e produtos.
 * Refatorada para usar ConcatAdapter com RecyclerView único.
 */
public class OrcamentosActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private OrcamentoDAO orcamentoDAO;
    private ClienteDAO clienteDAO;
    private ServicoDAO servicoDAO;
    private ProdutoDAO produtoDAO;
    
    // UI Components (apenas RecyclerView)
    private RecyclerView recyclerViewPrincipal;
    
    // Adapters
    private OrcamentoHeaderAdapter headerAdapter;
    private OrcamentoItensAdapter itensAdapter;
    private OrcamentoFooterAdapter footerAdapter;
    private ConcatAdapter concatAdapter;
    
    // Data
    private List<Cliente> listaClientes;
    private Orcamento orcamentoAtual;
    private String tipoOrcamento = "SERVICO"; // "SERVICO" ou "PRODUTO"
    private int clienteSelecionadoIndex = 0; // Para rastrear seleção do cliente

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Proteção Premium
        if (!PremiumManager.getInstance(this).verificarAcessoEmActivity(this, "Orçamentos")) {
            return;
        }
        
        setContentView(R.layout.activity_orcamentos);
        
        setupActionBar();
        setupDAOs();
        
        // Carregar clientes antes de configurar UI
        listaClientes = new ArrayList<>();
        listaClientes.add(new Cliente()); // Opção "Selecione um cliente"
        listaClientes.get(0).setNome("Selecione um cliente");
        listaClientes.addAll(clienteDAO.getAllClientes());
        
        setupRecyclerView();
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
    
    private void setupRecyclerView() {
        recyclerViewPrincipal = findViewById(R.id.recyclerViewPrincipal);
        recyclerViewPrincipal.setLayoutManager(new LinearLayoutManager(this));
        
        // 1. Header Adapter
        boolean isPremium = PlanManager.getInstance(this).isPremium();
        headerAdapter = new OrcamentoHeaderAdapter(listaClientes, isPremium, new OrcamentoHeaderAdapter.HeaderListener() {
            @Override
            public void onTipoOrcamentoChanged(String tipo) {
                tipoOrcamento = tipo;
            }

            @Override
            public void onClienteSelecionado(int position) {
                clienteSelecionadoIndex = position;
            }

            @Override
            public void onObservacoesChanged(String texto) {
                orcamentoAtual.setObservacoes(texto);
            }

            @Override
            public void onAdicionarItemClicked() {
                mostrarDialogItem(-1);
            }

            @Override
            public void onGerarPDFClicked() {
                gerarPDFOrcamento();
            }
        });
        
        // 2. Itens Adapter
        itensAdapter = new OrcamentoItensAdapter();
        itensAdapter.setOnItemRemoveListener(this::removerItem);
        itensAdapter.setOnItemClickListener(this::mostrarDialogItem);
        
        // 3. Footer Adapter
        footerAdapter = new OrcamentoFooterAdapter(new OrcamentoFooterAdapter.FooterListener() {
            @Override
            public void onDescontoChanged(double valor) {
                orcamentoAtual.setDesconto(valor);
                atualizarCalculosTotais();
            }

            @Override
            public void onAcrescimoChanged(double valor) {
                orcamentoAtual.setAcrescimo(valor);
                atualizarCalculosTotais();
            }
        });
        
        // ConcatAdapter
        concatAdapter = new ConcatAdapter(headerAdapter, itensAdapter, footerAdapter);
        recyclerViewPrincipal.setAdapter(concatAdapter);
    }
    
    private void inicializarOrcamento() {
        orcamentoAtual = new Orcamento();
        orcamentoAtual.setTipo("SERVICO");
        orcamentoAtual.setDataCriacao(System.currentTimeMillis());
        orcamentoAtual.setStatus(0); // Pendente
        
        tipoOrcamento = "SERVICO";
        clienteSelecionadoIndex = 0;
        
        // Resetar adapters se necessário (embora criar novo objeto orcamento já "limpe" a lógica)
        // Precisamos limpar visualmente o Header também se quisermos reset completo
        // Mas como o adapter recria o viewholder ao ser setado novamente ou notificado...
        // Vamos apenas atualizar os dados
        
        atualizarListaItens();
    }
    
    private void mostrarDialogItem(int posicaoParaEditar) {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Gerenciar Item");
            return;
        }
        
        boolean editando = posicaoParaEditar >= 0;
        OrcamentoItensAdapter.ItemDisplay itemEditando = editando ? itensAdapter.getItem(posicaoParaEditar) : null;
        
        // Se estiver editando, ajustar o tipo para corresponder ao item
        // Nota: Isso não atualiza o RadioButton visualmente no Header automaticamente a menos que notifiquemos o HeaderAdapter
        // Mas o HeaderAdapter mantém seu próprio estado visual.
        // Idealmente, deveríamos sincronizar.
        if (editando) {
            if (itemEditando.isServico) {
                tipoOrcamento = "SERVICO";
            } else {
                tipoOrcamento = "PRODUTO";
            }
            // TODO: Se quiséssemos atualizar o RadioButton visualmente, precisaríamos expor um método no HeaderAdapter
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(editando ? "Editar Item" : "Adicionar Item");
        
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_item_orcamento, null);
        builder.setView(dialogView);
        
        Spinner spinnerItem = dialogView.findViewById(R.id.spinnerItem);
        TextInputEditText editTextQuantidade = dialogView.findViewById(R.id.editTextQuantidade);
        TextInputEditText editTextValorUnitario = dialogView.findViewById(R.id.editTextValorUnitario);
        
        if (editando) {
            editTextQuantidade.setText(String.valueOf(itemEditando.quantidade));
            editTextValorUnitario.setText(String.format(Locale.getDefault(), "%.2f", itemEditando.valorUnitario));
        }
        
        // Configurar spinner baseado no tipo
        if ("SERVICO".equals(tipoOrcamento)) {
            List<Servico> servicos = servicoDAO.getAllServicos();
            ArrayAdapter<Servico> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, servicos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerItem.setAdapter(adapter);
            
            if (editando) {
                 Orcamento.OrcamentoItemServico item = (Orcamento.OrcamentoItemServico) itemEditando.originalItem;
                 for(int i=0; i<servicos.size(); i++) {
                     if(servicos.get(i).getId() == item.getServicoId()) {
                         spinnerItem.setSelection(i);
                         break;
                     }
                 }
            }
        } else {
            List<Produto> produtos = produtoDAO.getAllProdutos();
            ArrayAdapter<Produto> adapter = new ArrayAdapter<>(this, 
                android.R.layout.simple_spinner_item, produtos);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerItem.setAdapter(adapter);
            
            if (editando) {
                 Orcamento.OrcamentoItemProduto item = (Orcamento.OrcamentoItemProduto) itemEditando.originalItem;
                 for(int i=0; i<produtos.size(); i++) {
                     if(produtos.get(i).getId() == item.getProdutoId()) {
                         spinnerItem.setSelection(i);
                         break;
                     }
                 }
            }
            
            // Carregar preço automaticamente ao selecionar produto
            spinnerItem.setOnItemSelectedListener(new android.widget.AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(android.widget.AdapterView<?> parent, View view, int position, long id) {
                    if (editando) return; // Não altera valor automaticamente se estiver editando
                    
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
        
        builder.setPositiveButton(editando ? "Salvar" : "Adicionar", (dialog, which) -> {
            int posicao = spinnerItem.getSelectedItemPosition();
            if (posicao < 0) {
                Toast.makeText(this, "Selecione um item", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String qtdStr = editTextQuantidade.getText().toString().trim();
            String valorStr = editTextValorUnitario.getText().toString().trim();
            
            if (qtdStr.isEmpty()) {
                Toast.makeText(this, "Informe a quantidade", Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (valorStr.isEmpty()) {
                Toast.makeText(this, "Informe o valor unitário", Toast.LENGTH_SHORT).show();
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
                Orcamento.OrcamentoItemServico item;
                
                if (editando) {
                    item = (Orcamento.OrcamentoItemServico) itemEditando.originalItem;
                } else {
                    item = new Orcamento.OrcamentoItemServico();
                    orcamentoAtual.getItensServicos().add(item);
                }
                
                item.setServicoId(servico.getId());
                item.setNomeServico(servico.getNome());
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                item.setValorTotal(quantidade * valorUnitario);
            } else {
                Produto produto = (Produto) spinnerItem.getSelectedItem();
                Orcamento.OrcamentoItemProduto item;
                
                if (editando) {
                    item = (Orcamento.OrcamentoItemProduto) itemEditando.originalItem;
                } else {
                    item = new Orcamento.OrcamentoItemProduto();
                    orcamentoAtual.getItensProdutos().add(item);
                }
                
                item.setProdutoId(produto.getId());
                item.setNomeProduto(produto.getNome());
                item.setQuantidade(quantidade);
                item.setValorUnitario(valorUnitario);
                item.setValorTotal(quantidade * valorUnitario);
            }
            
            atualizarListaItens();
        });
        
        builder.setNegativeButton("Cancelar", null);
        builder.show();
    }
    
    private void atualizarListaItens() {
        // Atualizar adapter com todos os itens (serviços e produtos)
        itensAdapter.setAllItens(orcamentoAtual.getItensServicos(), orcamentoAtual.getItensProdutos());
        
        atualizarCalculosTotais();
    }
    
    private void atualizarCalculosTotais() {
        double subtotal = 0.0;
        
        for (Orcamento.OrcamentoItemServico item : orcamentoAtual.getItensServicos()) {
            subtotal += item.getValorTotal();
        }
        for (Orcamento.OrcamentoItemProduto item : orcamentoAtual.getItensProdutos()) {
            subtotal += item.getValorTotal();
        }
        
        double desconto = orcamentoAtual.getDesconto();
        double acrescimo = orcamentoAtual.getAcrescimo();
        
        double totalFinal = subtotal - desconto + acrescimo;
        if (totalFinal < 0) totalFinal = 0;
        
        orcamentoAtual.setValorTotal(totalFinal);
        
        // Atualizar Footer
        if (footerAdapter != null) {
            footerAdapter.updateTotalTextOnly(totalFinal);
        }
    }

    private void removerItem(int position) {
        OrcamentoItensAdapter.ItemDisplay itemDisplay = itensAdapter.getItem(position);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Remover Item");
        builder.setMessage("Deseja remover '" + itemDisplay.nome + "'?");
        builder.setPositiveButton("Remover", (dialog, which) -> {
            if (itemDisplay.isServico) {
                orcamentoAtual.getItensServicos().remove(itemDisplay.originalItem);
            } else {
                orcamentoAtual.getItensProdutos().remove(itemDisplay.originalItem);
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
        if (clienteSelecionadoIndex == 0) {
            Toast.makeText(this, "Selecione um cliente", Toast.LENGTH_SHORT).show();
            return;
        }
        
        if (orcamentoAtual.getItensServicos().isEmpty() && orcamentoAtual.getItensProdutos().isEmpty()) {
            Toast.makeText(this, "Adicione pelo menos um item", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Salvar orçamento
        Cliente cliente = listaClientes.get(clienteSelecionadoIndex);
        orcamentoAtual.setClienteId(cliente.getId());
        
        // Observações e valores já devem estar atualizados via Listeners
        
        long orcamentoId = orcamentoDAO.inserirOrcamento(orcamentoAtual);
        
        if (orcamentoId > 0) {
            orcamentoAtual.setId(orcamentoId); // Importante para o ID aparecer no PDF
            
            // Gerar PDF
            new Thread(() -> {
                try {
                    // USANDO O NOVO SERVIÇO DE PDF
                    OrcamentoPdfService pdfService = new OrcamentoPdfService(this);
                    java.io.File pdfFile = pdfService.gerarPdf(orcamentoAtual, cliente);
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                            Toast.LENGTH_LONG).show();
                        PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                        
                        // Limpar formulário (Criar novo orcamento limpa dados, mas precisamos limpar UI)
                        inicializarOrcamento();
                        // Resetar campos visuais no Header e Footer não é trivial sem expor métodos de reset nos adapters
                        // A maneira mais simples é recriar a activity ou recriar os adapters.
                        // Como inicializarOrcamento cria novo objeto, os valores numéricos estão zerados.
                        // Mas os EditTexts nos adapters mantêm o texto antigo se não forem notificados.
                        
                        // Recriar adapters para reset visual completo
                        setupRecyclerView(); 
                        
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
