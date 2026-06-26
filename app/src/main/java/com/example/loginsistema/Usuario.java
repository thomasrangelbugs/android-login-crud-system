package com.example.loginsistema;

/**
 * Classe simples para representar um usuário do sistema.
 * Guarda nome de usuário, senha e dica de senha.
 */
public class Usuario {

    private final String nomeUsuario;
    private final String senha;
    private final String dicaSenha;

    public Usuario(String nomeUsuario, String senha, String dicaSenha) {
        this.nomeUsuario = nomeUsuario;
        this.senha = senha;
        this.dicaSenha = dicaSenha;
    }

    public String getNomeUsuario() {
        return nomeUsuario;
    }

    public String getSenha() {
        return senha;
    }

    public String getDicaSenha() {
        return dicaSenha;
    }
}
