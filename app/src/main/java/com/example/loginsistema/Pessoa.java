package com.example.loginsistema;

/**
 * Modelo simples para representar um cadastro de pessoa física.
 */
public class Pessoa {

    private long id;
    private String nome;
    private String cpf;
    private String rua;
    private String numero;
    private String bairro;
    private String cep;
    private String genero;
    private String dataNascimento;

    public Pessoa() {
    }

    public Pessoa(long id,
                  String nome,
                  String cpf,
                  String rua,
                  String numero,
                  String bairro,
                  String cep,
                  String genero,
                  String dataNascimento) {
        this.id = id;
        this.nome = nome;
        this.cpf = cpf;
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
    }

    public Pessoa(String nome,
                  String cpf,
                  String rua,
                  String numero,
                  String bairro,
                  String cep,
                  String genero,
                  String dataNascimento) {
        this.nome = nome;
        this.cpf = cpf;
        this.rua = rua;
        this.numero = numero;
        this.bairro = bairro;
        this.cep = cep;
        this.genero = genero;
        this.dataNascimento = dataNascimento;
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

    public String getCpf() {
        return cpf;
    }

    public void setCpf(String cpf) {
        this.cpf = cpf;
    }

    public String getRua() {
        return rua;
    }

    public void setRua(String rua) {
        this.rua = rua;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getBairro() {
        return bairro;
    }

    public void setBairro(String bairro) {
        this.bairro = bairro;
    }

    public String getCep() {
        return cep;
    }

    public void setCep(String cep) {
        this.cep = cep;
    }

    public String getGenero() {
        return genero;
    }

    public void setGenero(String genero) {
        this.genero = genero;
    }

    public String getDataNascimento() {
        return dataNascimento;
    }

    public void setDataNascimento(String dataNascimento) {
        this.dataNascimento = dataNascimento;
    }
}
