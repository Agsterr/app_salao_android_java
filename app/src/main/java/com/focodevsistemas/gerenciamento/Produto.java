package com.focodevsistemas.gerenciamento;

import java.text.NumberFormat;
import java.util.Locale;

public class Produto {
    private long id;
    private String nome;
    private double valorPadrao; // Preço de venda (mantido para compatibilidade)
    private double precoAquisicao; // Preço de aquisição
    private double precoVenda; // Preço de venda
    private String descricao;
    private String imagemUri;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getValorPadrao() { return valorPadrao; }
    public void setValorPadrao(double valorPadrao) { this.valorPadrao = valorPadrao; }
    public double getPrecoAquisicao() { return precoAquisicao; }
    public void setPrecoAquisicao(double precoAquisicao) { this.precoAquisicao = precoAquisicao; }
    public double getPrecoVenda() { return precoVenda; }
    public void setPrecoVenda(double precoVenda) { this.precoVenda = precoVenda; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getImagemUri() { return imagemUri; }
    public void setImagemUri(String imagemUri) { this.imagemUri = imagemUri; }
    
    /**
     * Calcula o lucro unitário (preço de venda - preço de aquisição)
     */
    public double getLucroUnitario() {
        return precoVenda - precoAquisicao;
    }
    
    /**
     * Calcula a margem de lucro em percentual
     */
    public double getMargemLucro() {
        if (precoAquisicao > 0) {
            return ((precoVenda - precoAquisicao) / precoAquisicao) * 100;
        }
        return 0.0;
    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nome + " (" + nf.format(valorPadrao) + ")";
    }
}
