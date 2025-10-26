package com.example.appdetestes;

import java.text.NumberFormat;
import java.util.Locale;

public class Produto {
    private long id;
    private String nome;
    private double valorPadrao;
    private String descricao;
    private String imagemUri;

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }
    public double getValorPadrao() { return valorPadrao; }
    public void setValorPadrao(double valorPadrao) { this.valorPadrao = valorPadrao; }
    public String getDescricao() { return descricao; }
    public void setDescricao(String descricao) { this.descricao = descricao; }
    public String getImagemUri() { return imagemUri; }
    public void setImagemUri(String imagemUri) { this.imagemUri = imagemUri; }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("pt", "BR"));
        return nome + " (" + nf.format(valorPadrao) + ")";
    }
}