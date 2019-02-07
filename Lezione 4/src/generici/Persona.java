package generici;

import java.io.Serializable;

/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public class Persona implements Serializable {
    private final String nome;
    private final String cognome;

    public Persona(String nome, String cognome) {
        this.nome = nome;
        this.cognome = cognome;
    }

    @Override
    public String toString() {
        return nome + " " + cognome;
    }
}
