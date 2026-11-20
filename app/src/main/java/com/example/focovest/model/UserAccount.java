package com.example.focovest.model;

/**
 * Representa a conta autenticada do aluno.
 * Por segurança, não armazena nem serializa a senha do usuário em memória ou Intents.
 */
public class UserAccount {
    public String id;
    public String fullName;
    public String username;
    public String email;
    public String city;
    public String role; // "aluno" | "organizador"

    public UserAccount() {}

    public UserAccount(String id, String fullName, String username,
                       String email, String city, String role) {
        this.id = id;
        this.fullName = fullName;
        this.username = username;
        this.email = email;
        this.city = city;
        this.role = role;
    }
}
