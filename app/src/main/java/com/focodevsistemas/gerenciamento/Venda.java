package com.focodevsistemas.gerenciamento;

public class Venda {
    private long id;
    private long produtoId;
    private long dataVenda; // timestamp
    private int tipoPagamento; // 0=AVISTA, 1=APRAZO
    private double valorTotal;
    private String observacao;
    private long clienteId;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public long getProdutoId() { return produtoId; }
    public void setProdutoId(long produtoId) { this.produtoId = produtoId; }
    public long getDataVenda() { return dataVenda; }
    public void setDataVenda(long dataVenda) { this.dataVenda = dataVenda; }
    public int getTipoPagamento() { return tipoPagamento; }
    public void setTipoPagamento(int tipoPagamento) { this.tipoPagamento = tipoPagamento; }
    public double getValorTotal() { return valorTotal; }
    public void setValorTotal(double valorTotal) { this.valorTotal = valorTotal; }
    public String getObservacao() { return observacao; }
    public void setObservacao(String observacao) { this.observacao = observacao; }
    public long getClienteId() { return clienteId; }
    public void setClienteId(long clienteId) { this.clienteId = clienteId; }
}
