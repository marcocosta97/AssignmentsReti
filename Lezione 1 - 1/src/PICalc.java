
/*
 * 
 * 
 * 
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import static java.lang.Math.abs;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * OVERVIEW: esercizio 1 lezione 1
 * 
 * @author Marco Costa - 545144
 */
public class PICalc implements Runnable {
    private float diff;
    private double calc_value;

    public PICalc(float diff) {
        this.diff = diff;
        this.calc_value = 4;
    }

    @Override
    public String toString() {
        return "Valore calcolato: " + calc_value + "\n" + "Math.PI: " + Math.PI + "\n" + "Differenza con Math.PI: "
                + abs(calc_value - Math.PI);
    }

    @Override
    public void run() {
        double i = -3;
        while (abs((calc_value - Math.PI)) > diff) {
            calc_value = calc_value + (4 / i);
            i = (i > 0) ? -i - 2 : -i + 2;
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException, ExecutionException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));
        System.out.print("Inserisci accuratezza: ");
        float diff = Float.parseFloat(reader.readLine());
        if (diff < 0) {
            System.err.println("Il valore non può essere negativo!");
            System.exit(1);
        }
        System.out.print("Inserisci secondi di attesa massima: ");
        int max_sec = Integer.parseInt(reader.readLine());
        if (max_sec < 0) {
            System.err.println("Il valore non può essere negativo!");
            System.exit(1);
        }

        PICalc p = new PICalc(diff);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<?> f = executor.submit(p);
        try {
            f.get(max_sec, TimeUnit.SECONDS);
        } catch (TimeoutException ex) {
            f.cancel(true);
            System.err.println("Il tempo è scaduto!");
        }
        executor.shutdown();

        System.out.println(p);

    }

}
