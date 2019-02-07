/*
 * 
 * 
 * 
 */

/**
 *
 * @author mc
 */
public final class Professore extends UtenteLaboratorio {
    public Professore() {
        super();
    }

    @Override
    public int compareTo(UtenteLaboratorio t) {
        if(t.getClass() == Professore.class)
            return 0;
        
        return -1;
    }

    @Override
    public String toString() {
        return "Prof. " + super.toString(); 
    }
    
    
      
}
