
import java.io.File;
import java.io.IOException;
import java.net.Inet4Address;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/*
 * 
 * 
 * 
 */

/**
 * Implementazione singlethreaded del Parser
 * 
 * @author mc - Marco Costa - 545144
 */
public class ParserSingleThreaded extends Parser {
    /* regex che corrisponde ad un indirizzo IP situato all'inizio della stringa */
    private static final String regex = "^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
    
    /**
     * Parsing singlethreaded del file "f"
     * 
     * @param f il file
     * @return la stringa
     * @throws IOException se non è possibile leggere o aprire il file o se è mal formattato
     */
    @Override
    public String parseFile(File f) throws IOException {
        String[] lines = parse(f);
        Pattern pattern = Pattern.compile(regex); /* compilazione del regex */
        
        for(int i = 0; i < lines.length; i++) /* per ogni linea del file effettuo la risoluzione */
        {
            Matcher matcher = pattern.matcher(lines[i]);
            if(matcher.find())
                lines[i] = lines[i].replaceFirst(regex, Inet4Address.getByName(matcher.group()).getHostName());
            else
                throw new IOException("[!!] Errore di parsing! File mal formattato!");
        }
        
        return String.join("\n", lines); /* unisco l'array in una stringa singola */
    }
    
}
