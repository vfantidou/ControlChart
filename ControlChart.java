import java.text.DecimalFormat;
import org.apache.commons.math3.distribution.GammaDistribution ;
import java.util.Arrays;


class Parameters {
    public int a, b, r, i, j, k;

    public Parameters(int a, int b, int r, int i, int j, int k){
        if (a > b || i == j || i == k || j == k){
            System.exit(1);
        }
        this.a = a;
        this.b = b;
        this.r = r;
        this.i = i;
        this.j = j;
        this.k = k;
    }
	
    public String toString(){
        return a + "," + b + "," + i + "," + j + "," + k + "," + r;
    }
}

class Simulation {
	
	private double ucl, lcl;
    private GammaDistribution gamma;
    private double[] refSample; 
    private double[] controlSample; 
    private Parameters parameters;

    public Simulation(int m, int n, double l, double al){
        
        gamma = new GammaDistribution(al, l); 
        refSample = new double[m];
        controlSample = new double[n];
        makeRefSample();
    }

    public void initParameters(Parameters p) {
        parameters = p;

        lcl = refSample[parameters.a - 1];
        ucl = refSample[parameters.b - 1];
    }

    private void makeRefSample() {
        
        for (int i = 0; i < refSample.length; i++){
            refSample[i] = gamma.sample(); 
        }
        Arrays.sort(refSample);
    }

    public double computeFAR(int samples) {
        int sumL = 0;
        double far = 0;
        int[] d = new int[samples];

        for (int s = 0; s < samples; s++){
            makeControlSample();
            d[s] = checkSample();
        }

        sumL = computeSumL(d);

        far = (double)sumL / (double)(samples - 4);

        return far;
    }

    private int computeSumL(int[] d) {
        int s = 0;

        for (int i=4 ; i < d.length; i++){
            if (d[i] + d[i - 1] + d[i - 2] + d[i - 3] >= 4)
                s++;
        }

        return s;
    }

    private int checkSample() {
        double yi = controlSample[parameters.i - 1];
        double yj = controlSample[parameters.j - 1];
        double yk = controlSample[parameters.k - 1];
        int R = 0;

        if (yi > ucl || yi < lcl)
            return 1;

        if (yj > ucl || yj < lcl)
            return 1;

        if (yk > ucl || yk < lcl)
            return 1;

        for (int i = 0; i < controlSample.length; i++){
            if (controlSample[i] <= ucl && controlSample[i] >= lcl)
                R++;
        }

        if (R < parameters.r)
            return 1;

        return 0;
    }

    private void makeControlSample() {
        for (int i = 0; i < controlSample.length; i++){
            controlSample[i] = gamma.sample();
        }
    }
}

public class Main {
    public static void main(String[] args) {
        final DecimalFormat df = new DecimalFormat("#0.00%");
        double far, ar;

        int m = 40;
        int n = 5;

        Simulation simulation = new Simulation(m, n, 1, 1);

        running(simulation, df, n);
    }

    private static void running(Simulation simulation, DecimalFormat df, int n) {
	
        for (int i = 1; i <= n - 4 ; i++) {
            for (int j = i + 1; j <= n - 1; j++){
                for (int k = j + 1; k <= n; k++){
                    Parameters parameters = new Parameters(
                            4,149,9,i,j,k       
                    );
                    simulation.initParameters(parameters);

                    System.out.println(" (a, b, i, j, k, r): " + parameters);
                    System.out.println("FAR: " + df.format(simulation.computeFAR(10000)));
                    System.out.println();
                }
            }
        }
    }
}
