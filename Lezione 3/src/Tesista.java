/*
 * 
 * 
 * 
 */


public final class Tesista extends UtenteLaboratorio {

    public Tesista() {
        super();
    }

    @Override
    public int compareTo(UtenteLaboratorio t) {
        if(t.getClass() == Tesista.class)
            return 0;
        if(t.getClass() == Studente.class)
            return -1;
        
        return 1;
    }
    
}
