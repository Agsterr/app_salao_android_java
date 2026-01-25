package com.focodevsistemas.gerenciamento;

public class Cliente {

    private long id;
    private String nome;
    private String email;
    private String telefone;
    private String endereco;

    public Cliente() {
    }

    public Cliente(long id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getEndereco() { return endereco; }
    public void setEndereco(String endereco) { this.endereco = endereco; }

    @Override
    public String toString() {
        return nome != null ? nome : "";
    }
}
