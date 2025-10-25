package com.example.appdetestes;

public class Servico {

    private long id;
    private String nome;
    private int tempo; // Tempo em minutos

    public Servico() {
    }

    public Servico(long id, String nome, int tempo) {
        this.id = id;
        this.nome = nome;
        this.tempo = tempo;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public int getTempo() {
        return tempo;
    }

    public void setTempo(int tempo) {
        this.tempo = tempo;
    }
}
