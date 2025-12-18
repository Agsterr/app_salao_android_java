package com.focodevsistemas.gerenciamento;

public class VendaItem {
    private long id;
    private long vendaId;
    private long produtoId;
    private int quantidade;
    private double valorUnitario;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getVendaId() { return vendaId; }
    public void setVendaId(long vendaId) { this.vendaId = vendaId; }

    public long getProdutoId() { return produtoId; }
    public void setProdutoId(long produtoId) { this.produtoId = produtoId; }

    public int getQuantidade() { return quantidade; }
    public void setQuantidade(int quantidade) { this.quantidade = quantidade; }

    public double getValorUnitario() { return valorUnitario; }
    public void setValorUnitario(double valorUnitario) { this.valorUnitario = valorUnitario; }

    public double getTotal() { return quantidade * valorUnitario; }
}
