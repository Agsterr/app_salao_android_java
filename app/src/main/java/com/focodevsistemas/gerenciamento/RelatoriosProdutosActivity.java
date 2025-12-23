package com.focodevsistemas.gerenciamento;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Activity de Relatórios de Produtos Premium.
 * Funcionalidades:
 * - Relatório de Produtos (com filtros por valor)
 * - Relatório de Vendas
 */
public class RelatoriosProdutosActivity extends AppCompatActivity {

    private FeatureGate featureGate;
    private TextInputEditText editTextValorMinimo;
    private TextInputEditText editTextValorMaximo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.activity_relatorios_produtos);
        
        setupActionBar();
        setupPremiumUI();
        setupViews();
        setupListeners();
    }
    
    private void setupViews() {
        editTextValorMinimo = findViewById(R.id.editTextValorMinimo);
        editTextValorMaximo = findViewById(R.id.editTextValorMaximo);
    }
    
    /**
     * Configura a UI baseada no plano do usuário (FREE ou PREMIUM).
     */
    private void setupPremiumUI() {
        featureGate = new FeatureGate(this);
        PlanManager planManager = PlanManager.getInstance(this);
        boolean isPremium = planManager.isPremium();
        
        // Card de aviso Premium (visível apenas para FREE)
        com.google.android.material.card.MaterialCardView cardAviso = findViewById(R.id.cardAvisoPremium);
        if (cardAviso != null) {
            cardAviso.setVisibility(isPremium ? View.GONE : View.VISIBLE);
        }
        
        // Desabilitar botões se for FREE
        com.google.android.material.button.MaterialButton buttonRelatorioProdutos = findViewById(R.id.buttonRelatorioProdutos);
        if (buttonRelatorioProdutos != null) {
            buttonRelatorioProdutos.setEnabled(isPremium);
            if (!isPremium) {
                buttonRelatorioProdutos.setAlpha(0.5f);
            }
        }
        
        com.google.android.material.button.MaterialButton buttonRelatorioVendas = findViewById(R.id.buttonRelatorioVendas);
        if (buttonRelatorioVendas != null) {
            buttonRelatorioVendas.setEnabled(isPremium);
            if (!isPremium) {
                buttonRelatorioVendas.setAlpha(0.5f);
            }
        }
        
        // Se for FREE, mostrar aviso mas permitir visualizar
        if (!isPremium) {
            android.widget.Toast.makeText(this, 
                "Você está visualizando em modo FREE. Faça upgrade para usar todas as funcionalidades.", 
                android.widget.Toast.LENGTH_LONG).show();
        }
    }

    private void setupActionBar() {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Relatórios de Produtos");
        }
    }

    private void setupListeners() {
        // Botão Relatório de Produtos
        com.google.android.material.button.MaterialButton buttonRelatorioProdutos = findViewById(R.id.buttonRelatorioProdutos);
        if (buttonRelatorioProdutos != null) {
            buttonRelatorioProdutos.setOnClickListener(v -> gerarRelatorioProdutos());
        }
        
        // Botão Relatório de Vendas
        com.google.android.material.button.MaterialButton buttonRelatorioVendas = findViewById(R.id.buttonRelatorioVendas);
        if (buttonRelatorioVendas != null) {
            buttonRelatorioVendas.setOnClickListener(v -> gerarRelatorioVendas());
        }
    }
    
    /**
     * Gera relatório de produtos em PDF com filtros por valor.
     */
    private void gerarRelatorioProdutos() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Relatório de Produtos");
            return;
        }
        
        // Obter valores mínimo e máximo dos filtros
        double valorMinimo = 0.0;
        double valorMaximo = Double.MAX_VALUE;
        try {
            String valorMinStr = editTextValorMinimo.getText().toString().trim();
            if (!valorMinStr.isEmpty()) {
                valorMinimo = Double.parseDouble(valorMinStr.replace(",", "."));
            }
        } catch (Exception e) {
            // Valor inválido, usar 0.0
        }
        try {
            String valorMaxStr = editTextValorMaximo.getText().toString().trim();
            if (!valorMaxStr.isEmpty()) {
                valorMaximo = Double.parseDouble(valorMaxStr.replace(",", "."));
            }
        } catch (Exception e) {
            // Valor inválido, usar MAX_VALUE
        }
        
        // Validar que valor mínimo não seja maior que máximo
        if (valorMinimo > valorMaximo) {
            android.widget.Toast.makeText(this, "O valor mínimo não pode ser maior que o valor máximo.", 
                android.widget.Toast.LENGTH_SHORT).show();
            return;
        }
        
        final double valorMin = valorMinimo;
        final double valorMax = valorMaximo;
        
        new Thread(() -> {
            try {
                ProdutoDAO produtoDAO = new ProdutoDAO(this);
                produtoDAO.open();
                List<Produto> todosProdutos = produtoDAO.getAllProdutos();
                produtoDAO.close();
                
                // Filtrar produtos por valor (usando preço de venda)
                List<Produto> produtosFiltrados = new ArrayList<>();
                for (Produto produto : todosProdutos) {
                    double precoVenda = produto.getPrecoVenda() > 0 ? produto.getPrecoVenda() : produto.getValorPadrao();
                    if (precoVenda >= valorMin && precoVenda <= valorMax) {
                        produtosFiltrados.add(produto);
                    }
                }
                
                if (produtosFiltrados.isEmpty()) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Nenhum produto encontrado com os filtros aplicados.", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                    return;
                }
                
                // Gerar ficha simples para enviar ao cliente
                File pdfFile = PDFGeneratorHelper.gerarFichaProdutos(this, produtosFiltrados);
                
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Ficha gerada com sucesso!\n" + pdfFile.getName(), 
                        android.widget.Toast.LENGTH_LONG).show();
                    PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                });
            } catch (Exception e) {
                android.util.Log.e("RelatoriosProdutosActivity", "Erro ao gerar PDF de produtos", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
    }
    
    /**
     * Gera relatório de vendas em PDF.
     */
    private void gerarRelatorioVendas() {
        PlanManager planManager = PlanManager.getInstance(this);
        if (!planManager.isPremium()) {
            PremiumBlockDialog.show(this, "Relatório de Vendas");
            return;
        }
        
        new Thread(() -> {
            try {
                VendaDAO vendaDAO = new VendaDAO(this);
                vendaDAO.open();
                ProdutoDAO produtoDAO = new ProdutoDAO(this);
                produtoDAO.open();
                ClienteDAO clienteDAO = new ClienteDAO(this);
                clienteDAO.open();
                VendaItemDAO vendaItemDAO = new VendaItemDAO(this);
                vendaItemDAO.open();
                
                java.util.List<Venda> vendas = vendaDAO.getAllVendas();
                
                if (vendas.isEmpty()) {
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(this, "Nenhuma venda encontrada.", 
                            android.widget.Toast.LENGTH_SHORT).show();
                    });
                    vendaDAO.close();
                    produtoDAO.close();
                    clienteDAO.close();
                    vendaItemDAO.close();
                    return;
                }
                
                File pdfFile = PDFGeneratorHelper.gerarPDFVendas(this, vendas, vendaDAO, produtoDAO, clienteDAO, vendaItemDAO);
                
                vendaDAO.close();
                produtoDAO.close();
                clienteDAO.close();
                vendaItemDAO.close();
                
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "PDF gerado com sucesso!\n" + pdfFile.getName(), 
                        android.widget.Toast.LENGTH_LONG).show();
                    PDFGeneratorHelper.compartilharPDF(this, pdfFile);
                });
            } catch (Exception e) {
                android.util.Log.e("RelatoriosProdutosActivity", "Erro ao gerar PDF de vendas", e);
                runOnUiThread(() -> {
                    android.widget.Toast.makeText(this, "Erro ao gerar PDF: " + e.getMessage(), 
                        android.widget.Toast.LENGTH_LONG).show();
                });
            }
        }).start();
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

