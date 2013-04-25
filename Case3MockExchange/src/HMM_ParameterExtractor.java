import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HMM_ParameterExtractor {

	private final static int[] emission_one = {-10,-5,0,5,10};
	private final static int[] emission_two = {1,3};
	private static double trust_probability = 0.6;

	public static double mostLikelyPriceChange(HMM_Trainer myTrainer){
		double max=0;
		int index=0;
		double[] conditionExpectation = expectationForEmissionOne(myTrainer);
		for(int i=0;i<myTrainer.next_state.length;i++){
			if(max<myTrainer.next_state[i]){
				max=myTrainer.next_state[i];
				index=i;
			}
		}
		if(max>trust_probability)
			return conditionExpectation[index];
		else
			return expectedPriceChange(myTrainer);
	}
	
	public static double mostLikelySpread(HMM_Trainer myTrainer){
		double max=0;
		int index=0;
		double[] conditionExpectation = expectationForEmissionTwo(myTrainer);
		for(int i=0;i<myTrainer.next_state.length;i++){
			if(max<myTrainer.next_state[i]){
				max=myTrainer.next_state[i];
				index=i;
			}
		}
		if(max>trust_probability)
			return conditionExpectation[index];
		else
			return expectedSpread(myTrainer);
	}
	
	public static double expectedPriceChange(HMM_Trainer myTrainer){
		double result=0;
		double[] conditionExpectation = expectationForEmissionOne(myTrainer);
		for(int i=0;i<conditionExpectation.length;i++){
			result+=myTrainer.next_state[i]*conditionExpectation[i];
		}
		return result;
	}
	
	public static double expectedSpread(HMM_Trainer myTrainer){
		double result=0;
		double[] conditionExpectation = expectationForEmissionTwo(myTrainer);
		for(int i=0;i<conditionExpectation.length;i++){
			result+=myTrainer.next_state[i]*conditionExpectation[i];
		}
		return result;
	}
	
	
	/**
	 * Based on a hmm trainer, computing the expectation of emission 1
	 * @param myTrainer
	 * @return expectation of emission 1
	 */
	public static double[] expectationForEmissionOne(HMM_Trainer myTrainer){
		double[] result = new double[myTrainer.num_hidden_states];
		
		for(int i=0;i<myTrainer.parameterB.length;i++){
			int k=0;
			for(int j=0;j<myTrainer.parameterB[i].length;j=j+2){
				result[i]+=emission_one[k]*(myTrainer.parameterB[i][j]+myTrainer.parameterB[i][j+1]);
				k+=1;
			}
			//
		}
		return result;
	}
	
	/**
	 * Based on a hmm trainer, computing the expectation of emission 2
	 * @param myTrainer
	 * @return expectation of emission 2
	 */
	public static double[] expectationForEmissionTwo(HMM_Trainer myTrainer){
		double[] result = new double[myTrainer.num_hidden_states];
		
		for(int i=0;i<myTrainer.parameterB.length;i++){
			int k=0;
			for(int j=0;j<myTrainer.parameterB.length;j++){
				double p = myTrainer.parameterB[i][j]+myTrainer.parameterB[i][j+2]
						+myTrainer.parameterB[i][j+4]+myTrainer.parameterB[i][j+6]+myTrainer.parameterB[i][j+8];
				result[i]+=p*emission_two[k];
				k+=1;
			}
			//result[i]*=myTrainer.current_state[i];
		}
		
		return result;
	}
	
	/**
	 * Average a list of hmm trainers
	 * @param trainers the list of trainers
	 * @return the hmm trainer who has averaged parameters from a list of hmm trainers
	 */
	public static HMM_Trainer AverageParameter(ArrayList<HMM_Trainer> trainers){
		int num_hidden_states = trainers.get(0).num_hidden_states;
		int num_observable_states = trainers.get(0).num_obvious_states;
		
		double[][] averageParameterA = new double[num_hidden_states][num_hidden_states];
		double[][] averageParameterB = new double[num_hidden_states][num_observable_states];
		double[] averagePI = new double[num_hidden_states];
		double[] averageCurrentState = new double[num_hidden_states];
		
		/**
		 * Initialize
		 */
		for(int m=0;m<num_hidden_states;m++){
			for(int n=0;n<num_hidden_states;n++){
				averageParameterA[m][n]=0;
			}
		}
		for(int m=0;m<num_hidden_states;m++){
			for(int n=0;n<num_observable_states;n++){
				averageParameterB[m][n]=0;
			}
		}
		for(int n=0;n<num_hidden_states;n++){
			averagePI[n]=0;
			averageCurrentState[n]=0;
		}
		
		/**
		 * Accumulate and average
		 */
		int num=trainers.size();
		for(int i=0;i<trainers.size();i++){
			for(int m=0;m<num_hidden_states;m++){
				for(int n=0;n<num_hidden_states;n++){
					averageParameterA[m][n]+=trainers.get(i).parameterA[m][n]/num;
				}
			}
			for(int m=0;m<num_hidden_states;m++){
				for(int n=0;n<num_observable_states;n++){
					averageParameterB[m][n]+=trainers.get(i).parameterB[m][n]/num;
				}
			}
			for(int n=0;n<num_hidden_states;n++){
				averagePI[n]+=trainers.get(i).pi[n]/num;
			}
			for(int n=0;n<num_hidden_states;n++){
				averageCurrentState[n]+=trainers.get(i).current_state[n]/num;
			}
		}
		
		return new HMM_Trainer(num_hidden_states,num_observable_states,1000,
				averageParameterA,averageParameterB,averagePI,averageCurrentState);
		
	}
	
	public static HMM_Trainer ParameterExtractor(List<ExchangeSimulator.BidAsk> myBidAsks){
		/**
    	 * Split <code>myBidAsks</code> into two double array
    	 * @author YJQ
    	 */
    	int num_to_predict = 0;
    	int num_samples = myBidAsks.size()-num_to_predict;
    	double []bids = new double[num_samples+num_to_predict];
    	double []asks = new double[num_samples+num_to_predict];
    	for(int i=0;i<num_samples+num_to_predict;i++){
    		bids[i]=myBidAsks.get(i).Bid;
    		asks[i]=myBidAsks.get(i).Ask;
    	}

    	/**
    	 * This is an example of how to use @class HMM_Trainer
    	 * This code equally split the bids and asks, each time use num_sample_once points to estimate HMM model, do it for num_round rounds
    	 * I give up to create two different HMM trainer for emission 1 and emission 2 respectively
    	 * Instead, I tread two emission sequences as independent event and combine them into a paired event
    	 * So the number of possible observable states are the product of each observable state 
    	 * @author YJQ
    	 */
    	// Provide general info
    	int num_hidden_states=2;
    	int num_emission_one_states=5;
    	int num_emission_two_states=2;
    	
        int num_samples_once = 100;
        int num_round = num_samples/num_samples_once;
        
        // Create some matrix for initializing HMM trainers' parameters
        double[][] A=new double[num_hidden_states][num_hidden_states];
        double[][] B1=new double[num_hidden_states][num_emission_one_states];
        double[][] B2=new double[num_hidden_states][num_emission_two_states];
        double[][] E=new double[num_hidden_states][num_emission_one_states*num_emission_two_states];
        double[] PI=new double[num_hidden_states];
        double[] CurrentState=new double[num_hidden_states];
        
        setParametersForHMM(A, B1, B2, E, PI,CurrentState);
        
        // Now you see how to Construct
        HMM_Trainer my_trainer=new HMM_Trainer(num_hidden_states,num_emission_one_states*num_emission_two_states,num_samples_once,A,E,PI,CurrentState);
        
        
        for(int i=0;i<num_round;i++){
        	// Obtain data for one round train
        	double []bids_used_once = new double[num_samples_once];
        	double []asks_used_once = new double[num_samples_once];
        	for(int j=0;j<num_samples_once;j++){
        		bids_used_once[j]=bids[j+i*num_samples_once];
        		asks_used_once[j]=asks[j+i*num_samples_once];
        	}
        	int[] emission_index = getEmissionPairIndex(bids_used_once,asks_used_once);
        	
        	// Train and then estimate which hidden state it is in
        	my_trainer.train_HMM_parameters(emission_index);
        	my_trainer.estimate_current_state(emission_index);

        	

        }
        //	Print train results
 
    	System.out.println("Train Results: ");
    	System.out.println("State Transition Probability Matrix: ");
    	System.out.println(Arrays.deepToString(my_trainer.parameterA));
    	System.out.println("Observable Event Probability Matrix: ");
    	System.out.println(Arrays.deepToString(my_trainer.parameterB));
    	System.out.println("Current State Probability Distribution: ");
    	System.out.println(Arrays.toString(my_trainer.pi));
    	System.out.println();
    	System.out.println();  
    	
        return my_trainer;
        
        
	}
	
	public static void predictBidAsks(List<ExchangeSimulator.BidAsk> originalBidAsks, HMM_Trainer my_trainer){

		int num_to_predict = originalBidAsks.size()-1;
		
		int num_samples = originalBidAsks.size();
        int []real_bids = new int[num_samples];
        int []real_asks = new int[num_samples];
        for(int i=0;i<originalBidAsks.size();i++){
        	real_bids[i]=originalBidAsks.get(i).Bid;
        	real_asks[i]=originalBidAsks.get(i).Ask;
        }
        int old_price = (originalBidAsks.get(0).Bid+originalBidAsks.get(0).Ask)/2;
        
        double[] predicted_prices=new double[num_to_predict];
        double[] predicted_bids=new double[num_to_predict];
        double[] predicted_asks=new double[num_to_predict];
        
        double[] price_change_errors = new double[num_to_predict];
        double[] spread_errors = new double[num_to_predict];
        
        double sumPriceChangeError = 0.0;
        double sumSpreadError = 0.0;
        
        System.out.println("Predition Begins: ");
        for(int i=0;i<num_to_predict;i++){
        	int[] bid_history=new int[i+1];
        	int[] ask_history=new int[i+1];
        	for(int j=0;j<i+1;j++){
        		bid_history[j]=real_bids[j];
        		ask_history[j]=real_asks[j];
        	}
        	int[] index_history=new int[i+1];
        	index_history = getEmissionPairIndex(bid_history, ask_history);
        	my_trainer.estimate_current_state(index_history);
        	my_trainer.estimate_next_state();
        	System.out.println("Next State Probability Distribution: ");
        	System.out.println(Arrays.toString(my_trainer.next_state));
        	
        	predicted_prices[i]=mostLikelyPriceChange(my_trainer)+old_price;
        	double predicted_spread = mostLikelySpread(my_trainer);
        	predicted_bids[i]=predicted_prices[i]-predicted_spread;
        	predicted_asks[i]=predicted_prices[i]+predicted_spread;
        	
        	price_change_errors[i]=mostLikelyPriceChange(my_trainer)-((real_bids[i+1]+real_asks[i+1])/2-old_price);
        	spread_errors[i]=predicted_spread-Math.abs(real_asks[i+1]-real_bids[i+1])/2;
        	
        	sumPriceChangeError+=Math.abs(price_change_errors[i]);
        	sumSpreadError+=Math.abs(spread_errors[i]);
        	
        	old_price = (real_bids[i+1]+real_asks[i+1])/2;
        }
        System.out.println();
        System.out.println();
        System.out.println("Price Change Error Details:");
        System.out.println("Sum Error: "+sumPriceChangeError);
        System.out.println("Average Error: "+sumPriceChangeError/num_to_predict);
        System.out.println("Spread Error Details:");
        System.out.println("Sum Error: "+sumSpreadError);
        System.out.println("Average Error: "+sumSpreadError/num_to_predict);
        
	}
	
    /**
     * Convert bid and ask data into emission 1 event's index
     * @param bids Bid values
     * @param asks Ask values
     * @return some index value in {0,1,2,3,4} corresponding to {-10,-5,0,5,10} which is the possible set of emission 1
     * @author YJQ
     */
	private static int[] getEmissionOneIndex(double[] bids, double[] asks){
    	int num_observed_samples=(bids.length+asks.length)/2;
    	
    	double []m = new double[num_observed_samples];
    	int []emission_index = new int[num_observed_samples];
    	
    	for(int i=0;i<num_observed_samples;i++){
    		m[i]=(bids[i]+asks[i])/2;
    	}
    	
    	for(int i=0;i<num_observed_samples;i++){
        	double price = (bids[i]+asks[i])/2;
        	if(i>=1){
            	int observed_emission = (int)(price - m[i-1]);
            	switch(observed_emission){
            		case -10:
            			emission_index[i]=0;
            			break;
            		case -5:
            			emission_index[i]=1;
            			break;
            		case 0:
            			emission_index[i]=2;
            			break;
            		case 5:
            			emission_index[i]=3;
            			break;
            		case 10:
            			emission_index[i]=4;
            			break;
            		default:
            			emission_index[i]=2;// FIX IT!
            			break;
            	}
        	}
        	else{
        		emission_index[i]=2;
        	}
    	}
    	return emission_index;
    }
    
	private static int[] getEmissionOneIndex(int[] bids, int[] asks){
    	int num_observed_samples=(bids.length+asks.length)/2;
    	
    	double []m = new double[num_observed_samples];
    	int []emission_index = new int[num_observed_samples];
    	
    	for(int i=0;i<num_observed_samples;i++){
    		m[i]=(bids[i]+asks[i])/2;
    	}
    	
    	for(int i=0;i<num_observed_samples;i++){
        	double price = (bids[i]+asks[i])/2;
        	if(i>=1){
            	int observed_emission = (int)(price - m[i-1]);
            	switch(observed_emission){
            		case -10:
            			emission_index[i]=0;
            			break;
            		case -5:
            			emission_index[i]=1;
            			break;
            		case 0:
            			emission_index[i]=2;
            			break;
            		case 5:
            			emission_index[i]=3;
            			break;
            		case 10:
            			emission_index[i]=4;
            			break;
            		default:
            			emission_index[i]=2;// FIX IT!
            			break;
            	}
        	}
        	else{
        		emission_index[i]=2;
        	}
    	}
    	return emission_index;
    }
    
    /**
     * Convert bid and ask data into emission 2 event's index
     * @param bids
     * @param asks
     * @return some index value in {0,1} corresponding to {1,3} which is the possible set of emission 2
     */
	private static int[] getEmissionTwoIndex(double[] bids, double[] asks){
    	int num_observed_samples=(bids.length+asks.length)/2;
    	int[] spread=new int[num_observed_samples];
    	
    	int []emission_index = new int[num_observed_samples];
    	
    	for(int i=0;i<num_observed_samples;i++){
    		spread[i]=(int)Math.abs((bids[i]-asks[i])/2);
    		switch(spread[i]){
    			case 1:
    				emission_index[i]=0;
    				break;
    			case 3:
    				emission_index[i]=1;
    				break;
    			default:
    				emission_index[i]=0;//	FIX IT!
    				break;
    		}
    	}
    	
    	return emission_index;
    }
    
	private static int[] getEmissionTwoIndex(int[] bids, int[] asks){
    	int num_observed_samples=(bids.length+asks.length)/2;
    	int[] spread=new int[num_observed_samples];
    	
    	int []emission_index = new int[num_observed_samples];
    	
    	for(int i=0;i<num_observed_samples;i++){
    		spread[i]=(int)Math.abs((bids[i]-asks[i])/2);
    		switch(spread[i]){
    			case 1:
    				emission_index[i]=0;
    				break;
    			case 3:
    				emission_index[i]=1;
    				break;
    			default:
    				emission_index[i]=0;//	FIX IT!
    				break;
    		}
    	}
    	
    	return emission_index;
    }
    
    /**
     * Pair two emission's index into single index
     * @param index1 emission 1's index
     * @param index2 emission 2's index
     * @return paired index
     */
	private static int pickUpIndex(int index1, int index2){
    	switch(index1){
    		case 0:
    			if(index2==0)
    				return 0;
    			else
    				return 1;
    		case 1:
    			if(index2==0)
    				return 2;
    			else
    				return 3;
    		case 2:
    			if(index2==0)
    				return 4;
    			else
    				return 5;
    		case 3:
    			if(index2==0)
    				return 6;
    			else
    				return 7;
    		case 4:
    			if(index2==0)
    				return 8;
    			else
    				return 9;
    		default:
    				return 0;
    	}
    }
    
    /**
     * Observe the price of bids and asks, recognize two emissions' index 
     * pair those two index sequences into single index sequence
     * @param bids bid price
     * @param asks ask price
     * @return observed paired index sequence 
     */
	private static int[] getEmissionPairIndex(double[] bids, double[] asks){
    	int[] emission_one_index = getEmissionOneIndex(bids,asks);
    	int[] emission_two_index = getEmissionTwoIndex(bids,asks);
    	int length = (emission_one_index.length+emission_two_index.length)/2;
    	int[] emission_pair_index = new int[length];
    	for(int i=0;i<length;i++){
    		emission_pair_index[i] = pickUpIndex(emission_one_index[i],emission_two_index[i]);
    	}
    	return emission_pair_index;
    }
	private static int[] getEmissionPairIndex(int[] bids, int[] asks){
    	int[] emission_one_index = getEmissionOneIndex(bids,asks);
    	int[] emission_two_index = getEmissionTwoIndex(bids,asks);
    	int length = (emission_one_index.length+emission_two_index.length)/2;
    	int[] emission_pair_index = new int[length];
    	for(int i=0;i<length;i++){
    		emission_pair_index[i] = pickUpIndex(emission_one_index[i],emission_two_index[i]);
    	}
    	return emission_pair_index;
    }
    
    /**
     * Just a messup method to set some matrix elements' value and provide them to HMM_Trainer
     * @param A for parameterA
     * @param B1 for parameterB in emission 1
     * @param B2 for parameterB in emission 2
     * @param PI for pi
     */
    public static void setParametersForHMM(double[][] A, double[][] B1, double[][] B2, 
    										double[][] E,double[] PI,double[] CurrentState){
    	A[0][0] = A[1][1] = 0.75;
    	A[0][1] = A[1][0] = 0.25;

    	// for emission 1
    	B1[0][0] = 0.4;
    	B1[0][1] = 0.2;
    	B1[0][2] = 0.2;
    	B1[0][3] = 0.1;
    	B1[0][4] = 0.1;
    	B1[1][0] = 0.1;
    	B1[1][1] = 0.1;
    	B1[1][2] = 0.2;
    	B1[1][3] = 0.2;
    	B1[1][4] = 0.4;
    	
    	
    	// for emission 2
    	B2[0][0] = 0.25;
    	B2[0][1] = 0.75;
    	B2[1][0] = 0.75;
    	B2[1][1] = 0.25;
    	
    	PI[0] = 0.5;
    	PI[1] = 0.5;
    	
    	CurrentState[0] = 0.5;
    	CurrentState[1] = 0.5;
    	
    	// Pair two emission sequence
    	for(int i=0;i<B1.length;i++){
    		int l=0;
    		for(int j=0;j<B1[i].length;j++){
    			for(int k=0;k<B2[i].length;k++){
    				E[i][l]=B1[i][j]*B2[i][k];
    				l+=1;
    			}
    		}
    	}
    }
    

    public static ExchangeSimulator.BidAsk predictedPriceAndSpread(int emission_one_index, int emission_two_index, int old_price){

    	int emission_one_value = emission_one[emission_one_index];
    	int emission_two_value = emission_two[emission_two_index];
    	System.out.println("price change: "+emission_one_value+"spread: "+emission_two_value);
    	int new_price = old_price+emission_one_value;
    	int spread = emission_two_value;
    	return new ExchangeSimulator.BidAsk(new_price-spread,new_price+spread);
    }

	
}
