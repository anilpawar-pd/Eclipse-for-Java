import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) throws IOException {

        
   
        SimpleCase3Job myJob = new SimpleCase3Job();
        
        myJob.computeAverageHMMModel();
        //myJob.testPredictionAccuracy();

        String myFilename="SimDataCase3_49.csv";
        List<ExchangeSimulator.BidAsk> theBidAsks = GetBidAsksFromFile(myFilename);
        double cash = ExchangeSimulator.RunSimulator(myJob.getCase3Implementation(), theBidAsks);
        System.out.println("We have cash of " + cash);
        
    }

    public static List<ExchangeSimulator.BidAsk> GetBidAsksFromFile(String myFilename) throws IOException {
        List<ExchangeSimulator.BidAsk> theBidAsks = new ArrayList<ExchangeSimulator.BidAsk>();
        BufferedReader in = new BufferedReader(new FileReader(myFilename));
        while (in.ready()) {
            String[] aLines = in.readLine().split(",");
            theBidAsks.add(new ExchangeSimulator.BidAsk(Integer.parseInt(aLines[0]), Integer.parseInt(aLines[1])));
        }
        in.close();
        return theBidAsks;
    }
    
    public static List<ExchangeSimulator.BidAsk> GetBidAsksFromFile(String myFilename, 
    																int begin, int end) throws IOException {
        List<ExchangeSimulator.BidAsk> theBidAsks = new ArrayList<ExchangeSimulator.BidAsk>();
        BufferedReader in = new BufferedReader(new FileReader(myFilename));
        int num_read = 0;
        while (in.ready()) {
            String[] aLines = in.readLine().split(",");
            if(num_read<=end && num_read>=begin){
            	theBidAsks.add(new ExchangeSimulator.BidAsk(Integer.parseInt(aLines[0]), 
            			Integer.parseInt(aLines[1])));
            }
            num_read+=1;
        }
        in.close();
        return theBidAsks;
    }
}
