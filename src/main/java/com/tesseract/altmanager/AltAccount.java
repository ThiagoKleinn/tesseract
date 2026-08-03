package com.tesseract.altmanager;

/**
 * Representa uma conta salva no AltManager.
 * Tipo CRACKED = só nome, sem senha.
 * Tipo MICROSOFT = email + senha criptografada.
 */
public class AltAccount {

    public enum Type { CRACKED, MICROSOFT }

    private final Type   type;
    private final String email;     // para MICROSOFT; para CRACKED é o nome
    private final String password;  // vazio se CRACKED; criptografado se MICROSOFT

    public AltAccount(Type type, String email, String password) {
        this.type     = type;
        this.email    = email;
        this.password = password;
    }

    public Type   getType()     { return type; }
    public String getEmail()    { return email; }
    public String getPassword() { return password; }

    /** Label exibido na lista — mostra só o nome/email sem a senha */
    public String getDisplayName() {
        if (type == Type.CRACKED) return email; // email = nome no modo cracked
        int at = email.indexOf('@');
        return at > 0 ? email.substring(0, at) : email;
    }
}