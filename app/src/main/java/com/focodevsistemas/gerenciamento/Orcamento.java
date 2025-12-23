package com.focodevsistemas.gerenciamento;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que representa um orçamento.
 * Pode ser para serviços ou produtos.
 */
public class Orcamento {
    
    private long id;
    private long clienteId;
    private String tipo; // "SERVICO" ou "PRODUTO"
    private long dataCriacao; // timestamp
    private double valorTotal;
    private String observacoes;
    private int status; // 0 = Pendente, 1 = Aprovado, 2 = Rejeitado
    
    // Para orçamentos de serviços
    private List<OrcamentoItemServico> itensServicos;
    
    // Para orçamentos de produtos
    private List<OrcamentoItemProduto> itensProdutos;
    
    public Orcamento() {
        itensServicos = new ArrayList<>();
        itensProdutos = new ArrayList<>();
        status = 0; // Pendente por padrão
    }
    
    // Getters e Setters
    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    
    public long getClienteId() { return clienteId; }
    public void setClienteId(long clienteId) { this.clienteId = clienteId; }
    
    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }
    
    public long getDataCriacao() { return dataCriacao; }
    public void setDataCriacao(long dataCriacao) { this.dataCriacao = dataCriacao; }
    
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    
    public String getObservacoes() { return observacoes; }
    public void setObservacoes(String observacoes) { this.observacoes = observacoes; }
    
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    
    public List<OrcamentoItemServico> getItensServicos() { return itensServicos; }
    public void setItensServicos(List<OrcamentoItemServico> itensServicos) { this.itensServicos = itensServicos; }
    
    public List<OrcamentoItemProduto> getItensProdutos() { return itensProdutos; }
    public void setItensProdutos(List<OrcamentoItemProduto> itensProdutos) { this.itensProdutos = itensProdutos; }
    
    /**
     * Classe interna para itens de serviço no orçamento
     */
    public static class OrcamentoItemServico {
        private long servicoId;
        private String nomeServico;
        private int quantidade;
        private double valorUnitario;
        private double valorTotal;
        
        public long getServicoId() { return servicoId; }
        public void setServicoId(long servicoId) { this.servicoId = servicoId; }
        
        public String getNomeServico() { return nomeServico; }
        public void setNomeServico(String nomeServico) { this.nomeServico = nomeServico; }
        
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        
        public double getValorUnitario() { return valorUnitario; }
        public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }
        
        public double getValorTotal() { return valorTotal; }
        public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    }
    
    /**
     * Classe interna para itens de produto no orçamento
     */
    public static class OrcamentoItemProduto {
        private long produtoId;
        private String nomeProduto;
        private int quantidade;
        private double valorUnitario;
        private double valorTotal;
        
        public long getProdutoId() { return produtoId; }
        public void setProdutoId(long produtoId) { this.produtoId = produtoId; }
        
        public String getNomeProduto() { return nomeProduto; }
        public void setNomeProduto(String nomeProduto) { this.nomeProduto = nomeProduto; }
        
        public int getQuantidade() { return quantidade; }
        public void setQuantidade(int quantidade) { this.quantidade = quantidade; }
        
        public double getValorUnitario() { return valorUnitario; }
        public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }
        
        public double getValorTotal() { return valorTotal; }
        public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    }
}

