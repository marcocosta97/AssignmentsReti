package banca;


import generici.Persona;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

/*
 * 
 * 
 * 
 */

/**
 * Classe che rappresenta l'oggetto ContoCorrente, ad ogni oggetto è associata
 * una persona (il correntista) e una lista di movimenti
 * I metodo della classe sono protetti, accessibili pubblicamente dall'interfaccia della classe Banca
 * @author mc - Marco Costa - 545144
 */
public class ContoCorrente implements Serializable, Iterable<Movimento> {
    private static final long serialVersionUID = 1L;
    
    private Persona correntista;
    private List<Movimento> listaMovimenti;
    
    protected ContoCorrente(Persona correntista) {
        if(correntista == null)
            throw new NullPointerException("il correntista è null");
        
        this.correntista = correntista;
        this.listaMovimenti = new ArrayList<>();
    }

    
    /**
     * aggiunta di un movimento al conto 
     * @throws IllegalArgumentException se la causale non è permessa
     * 
     * @param causale la stringa contenente la causale
     * @return il nuovo movimento aggiunto
     */
    protected Movimento addMovimento(String causale) throws IllegalArgumentException {
        Movimento v = new Movimento(causale);
        
        listaMovimenti.add(v);
        
        return v;
    }
    
    @Override
    public String toString() {
        String s = "";
        
        s += "ContoCorrente{" + "correntista=" + correntista + ", listaMovimenti=\n";       
        s += "\t" + Arrays.toString(listaMovimenti.toArray());
        s += "};";
        
        return s;
    }

    /**
     * restituisce un nuovo iteratore sui movimenti del conto
     * 
     * @return l'iteratore
     */
    @Override
    public Iterator<Movimento> iterator() {
        return new ContoCorrenteIterator(listaMovimenti);
    }
    
    /**
     * classe wrapper per nascondere il metodo remove dall'iteratore
     */
    private static class ContoCorrenteIterator implements Iterator<Movimento> {
        private final Iterator<Movimento> it;
        
        public ContoCorrenteIterator(List<Movimento> m) {   
            it = m.iterator();
        }
        
        @Override
        public boolean hasNext() {
            return it.hasNext();
        }

        @Override
        public Movimento next() {
            return it.next();
        }
    }  
}
