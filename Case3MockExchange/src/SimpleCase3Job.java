import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.chicago.cases.AbstractCase3;

public class SimpleCase3Job extends AbstractCase3 {
	
	public class ExchangeTracer{
		boolean hold=false;
		int num_good_rounds=0;
		ArrayList<Integer> trace=new ArrayList<Integer>();
		//int current_return;
		double real_price_change;
		ArrayList<Double> predicted_price_change=new ArrayList<Double>();
		double old_bid,old_ask;
		boolean initialized=false;
		int round=0;
	}
	
    // Just Implement the below two methods.
    public class SimpleCase3Impl implements Case3 {
        int Cash = 0;
        int Position = 0;
        boolean WaitingForOrderConfirmation = false;
        ExchangeTracer exchangeTracer=new ExchangeTracer();

        @Override
        public int newBidAsk(double bid, double ask) {
            log("Received New Bid Ask of " + bid + " : " + ask);
            
            if(!exchangeTracer.initialized){
            	exchangeTracer.old_ask=ask;
            	exchangeTracer.old_bid=bid;
            	exchangeTracer.initialized=true;
            }else{
            	exchangeTracer.real_price_change=(bid+ask-(exchangeTracer.old_ask+exchangeTracer.old_bid))/2;
            	exchangeTracer.old_ask=ask;
            	exchangeTracer.old_bid=bid;
            }
            exchangeTracer.predicted_price_change.add(HMM_ParameterExtractor.expectedPriceChange(averageTrainer));
            
            
            if(exchangeTracer.num_good_rounds==2){
            	if(exchangeTracer.predicted_price_change.get(exchangeTracer.round)>0){
            		exchangeTracer.trace.add(1);
            		exchangeTracer.hold=true;
            		exchangeTracer.num_good_rounds=0;
            		Position=1;
            	}
            }
            else if(exchangeTracer.real_price_change>0 && exchangeTracer.predicted_price_change.get(exchangeTracer.round)>0){
            	exchangeTracer.num_good_rounds+=1;
            	exchangeTracer.trace.add(0);
            	Position=0;
            }else if(exchangeTracer.hold==true){
            	if(exchangeTracer.real_price_change<0 && exchangeTracer.predicted_price_change.get(exchangeTracer.round)!=0){
            		exchangeTracer.hold=false;
            		exchangeTracer.trace.add(-1);
            		Position=-1;
            	}
            }else{
            	exchangeTracer.trace.add(0);
            	Position=0;
            }
            exchangeTracer.round+=1;

            // If we are still waiting for an order fill just return 0
            if(WaitingForOrderConfirmation) {
                return 0;
            }

            // If we have no position on go long like a boss
            if(Position == 0) {
                log("Going Long!");
                WaitingForOrderConfirmation = true;
                return 1;
            }

            // If we have a long position on, clear out of it
            if(Position == 1) {
                log("Clearing Position");
                WaitingForOrderConfirmation = true;
                return -1;
            }

            return 0;
        }

        @Override
        public void newOrderFill(double price, long volume) {
            log("Received New Order Fill of " + volume + " at price " + price);
            Position += volume;
            Cash += -1 * price * volume;
            log("New Position Info (Position, Cash): (" + Position + ", " + Cash + ")");
            WaitingForOrderConfirmation = false;
        }
    }

    // No need to change the stuff below here.
    AbstractCase3.Case3 theCase = new SimpleCase3Impl();

    @Override
    public Case3 getCase3Implementation() {
        return theCase;
    }

    @Override
    protected void log(String msg) {
        try {
            super.log(msg);
        } catch (NullPointerException e) {
            System.out.println(msg);
        }
    }
    
    
    
    
    public ArrayList<HMM_Trainer> trainers=new ArrayList<HMM_Trainer>();
    public HMM_Trainer averageTrainer;
    
    /**
	 * Use all 50 simDataCase3 files to draw out a good hmm trainer
	 */
    public void computeAverageHMMModel() throws IOException{
    	
        for(int i=0;i<49;i++){
        	String myFilename = "SimDataCase3_";
        	Integer index=new Integer(i);
        	myFilename=myFilename.concat(index.toString().concat(".csv"));
        	List<ExchangeSimulator.BidAsk> myBidAsks = Main.GetBidAsksFromFile(myFilename);
        	HMM_Trainer tempTrainer = HMM_ParameterExtractor.ParameterExtractor(myBidAsks);
        	trainers.add(tempTrainer);
        }
        averageTrainer = HMM_ParameterExtractor.AverageParameter(trainers);
        
        System.out.println("Average HMM Parameters are Obtained.");
    	System.out.println("State Transition Probability Matrix: ");
    	System.out.println(Arrays.deepToString(averageTrainer.parameterA));
    	System.out.println("Observable Event Probability Matrix: ");
    	System.out.println(Arrays.deepToString(averageTrainer.parameterB));
    	System.out.println("Current State Probability Distribution: ");
    	System.out.println(Arrays.toString(averageTrainer.pi));
    	System.out.println();
    	
//    	System.out.println("Condition Expectation for Emission 1: ");
//    	System.out.println(Arrays.toString(HMM_ParameterExtractor.expectationForEmissionOne(averageTrainer)));
//    	System.out.println("Condition Expectation for Emission 2: ");
//    	System.out.println(Arrays.toString(HMM_ParameterExtractor.expectationForEmissionTwo(averageTrainer)));
//    	System.out.println();
        
    	System.out.println("Expectation of Price Change: "+
    						HMM_ParameterExtractor.expectedPriceChange(averageTrainer));
    	System.out.println("Expectation of Spread: "+HMM_ParameterExtractor.expectedSpread(averageTrainer));
    	System.out.println();
    	
    	System.out.println("Most Likely Price Change: "+
				HMM_ParameterExtractor.mostLikelyPriceChange(averageTrainer));
    	System.out.println("Most Likely Spread: "+HMM_ParameterExtractor.mostLikelySpread(averageTrainer));
    	System.out.println();
    }
    /**
     * 
     */
    public void testPredictionAccuracy() throws IOException{
    	
    	String testFileName = "SimDataCase3_49.csv";
    	List<ExchangeSimulator.BidAsk> realBidAsks = Main.GetBidAsksFromFile(testFileName,0,299);
    	
    	HMM_ParameterExtractor.predictBidAsks(realBidAsks,averageTrainer);
    }
}
