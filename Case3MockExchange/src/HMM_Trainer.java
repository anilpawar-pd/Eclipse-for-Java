
/**
 * A <code>HMM_Trainner</code> object will take in an observed sequence, self-setup HMM parameters' initial value,
 * use observed sequence to figure out all significant HMM parameters. At this time, I just set every variable field as public. 
 * Feel free to revise to private.
 * @author YJQ
 * @version 1.0
 */

public class HMM_Trainer {

	/**
	 * If you want to further change the model of HMM, modify these final values
	 */
	public int num_hidden_states;
	public int num_obvious_states;
    
    /**
     * Constructor of trainer
     * @param hidden_states how many hidden states
     * @param obvious_state how many obvious states
     * @param observed_samples how many samples to be used
     * @param A initial value
     * @param B initial value
     * @param PI initial value
     */
    HMM_Trainer(int hidden_states,int obvious_state,int observed_samples,
    		double[][] A, double[][] B, double[] PI, double[] CurrentStatePossibility){
    	num_hidden_states=hidden_states;
    	num_obvious_states=obvious_state;
    	num_observed_samples=observed_samples;
    	
    	parameterA = new double[num_hidden_states][num_hidden_states];
    	parameterB = new double[num_hidden_states][num_obvious_states];
    	pi = new double[num_hidden_states];
    	current_state = new double[num_hidden_states];
    	next_state = new double[num_hidden_states];
    	
    	if(first_train){
    		init_HMM_parameters(A,B,PI,CurrentStatePossibility);
    	}
    }
    
    /**
     * Will not be initialize until <code>train_HMM_parameters</code> function is called
     */
    public int num_observed_samples;
    
    /**
     * Here comes the significant HMM parameters you are interested in.
     * parameterA stores the hidden state transition probability distribution
     * parameterB stores the observation symbol probability distribution
     * pi stores the initial probability distribution of hidden states
     */
    public double [][]parameterA;
    public double [][]parameterB;
    public double []pi;
    public double[] current_state;
    public double[] next_state;
    
    /**
     * Here comes the necessary intermediately variables for estimating HMM parameters
     */
    private double [][]alpha;
    private double [][]beta;
    private double [][]L;
    private double [][][]H;
    
    /**
     * In the first round of training, the default initial parameter of HMM will be set.
     * But after hence we should keep updating them, so no need to setup initial value.
     * Just a symbol to record if it is the first round of train
     */
    private boolean first_train = true;
    
    /**
     * Setup HMM parameters' initial values according to tips in pdf instruction
     * @return nothing but the inner variables in the object is initialized
     */
    public void init_HMM_parameters(double[][] A, double[][] B, double[] PI,double[] CurrentStatePossibility){
    	
    	if(first_train){
    		
    		for(int i=0;i<A.length;i++){
    			for(int j=0;j<A[i].length;j++){
    				parameterA[i][j]=A[i][j];
    			}
    		}
    		
    		for(int i=0;i<B.length;i++){
    			for(int j=0;j<B[i].length;j++){
    				parameterB[i][j]=B[i][j];
    			}
    		}
    		
    		for(int i=0;i<PI.length;i++){
    			pi[i]=PI[i];
    			current_state[i]=CurrentStatePossibility[i];
    			next_state[i]=0;
    		}

    	}
    	
    }

    /**
     * Initialize necessary intermediately parameters
     */
    public void init_HMM_computing_parameters(){

    	if(first_train){
            alpha = new double[num_hidden_states][num_observed_samples];
            beta = new double[num_hidden_states][num_observed_samples];
            L = new double[num_hidden_states][num_observed_samples];
            H = new double[num_hidden_states][num_hidden_states][num_observed_samples];
    	}

    	for(int k=0;k<num_hidden_states;k++){
    		for(int t=0;t<num_observed_samples;t++){
    			alpha[k][t]=0;
    			beta[k][t]=0;
    			L[k][t]=0;
    			for(int l=0;l<num_hidden_states;l++){
    				H[k][l][t]=0;
    			}
    		}
    	}
    }

    /**
     * Compute parameter alpha at index <code>index</code>
     * @param index position of currently computing alpha
     */
    private void compute_alpha(int index,int emission_index){
    	if(index==0){
    		for(int k=0;k<num_hidden_states;k++){
    			alpha[k][index]=pi[k]*parameterB[k][emission_index];
    		}
        }else{
        	//int u = emission_index[index];
        	for(int k=0;k<num_hidden_states;k++){
        		double sum=0;
        		for(int l=0;l<num_hidden_states;l++){
        			sum+=alpha[l][index-1]*parameterA[l][k];
        		}
        		alpha[k][index]=parameterB[k][emission_index]*sum;
        	}
        }
    }
    
    /**
     * Compute parameter beta at index <code>index</code>
     * @param index position of currently computing beta
     */
    private void compute_beta(int index,int emission_index){
    	if(index==num_observed_samples-1){
    		for(int k=0;k<num_hidden_states;k++){
    			beta[k][index]=1;
    		}
        }else{
        	//int u = emission_index[index+1];//observation_index(m[index],bid,ask);
        	for(int k=0;k<num_hidden_states;k++){
        		beta[k][index]=0;
        		for(int l=0;l<num_hidden_states;l++){
        			beta[k][index]+=parameterA[k][l]*parameterB[l][emission_index]*beta[l][index+1];
        		}
        	}

        }
    }
    
    /**
     * Compute intermediately variable for training
     * @return P(u)
     */
    private double compute_Pu(){
    	double Pu=0.0;
    	for(int k=0;k<num_hidden_states;k++){
    		for(int t=0;t<num_observed_samples;t++){
    			Pu+=alpha[k][t]*beta[k][t];
    		}
    	}
    	Pu/=num_observed_samples;
    	return Pu;
    }
    
    /**
     * Compute statistic parameter L for estimate HMM model
     * @param Pu result from function <code>compute_Pu</code>
     */
    private void compute_L(double Pu, int index){
    	for(int k=0;k<num_hidden_states;k++){
    		L[k][index]=alpha[k][index]*beta[k][index]/Pu;
    	}
    }

    
    /**
     * Compute statistic parameter H for estimate HMM model
     * @param Pu result from function <code>compute_Pu</code>
     */
    private void compute_H(double Pu,int index,int emission_index){
    	for(int k=0;k<num_hidden_states;k++){
    		for(int l=0;l<num_hidden_states;l++){
    			//int u=emission_index[index+1];
    			H[k][l][index]=alpha[k][index]*parameterA[k][l]*parameterB[l][emission_index]*beta[l][index+1]/Pu;
    		}
    	}	
    }

    
    /**
     * After all intermediately variables setup, we can update our estimation of the HMM model
     */
    private void update_HMM_parameters(int[] emission_index){
    	for(int k=0;k<num_hidden_states;k++){
    		pi[k]=L[k][1];
    	}
    	
    	for(int k=0;k<num_hidden_states;k++){
    		for(int l=0;l<num_hidden_states;l++){
    			double numerator=0.0;
    			double denominator=0.0;
    			for(int t=0;t<num_observed_samples-1;t++){
    				numerator+=H[k][l][t];
    				denominator+=L[k][t];
    			}
    			parameterA[k][l]=numerator/denominator;
    		}
    	}
    	
    	for(int i=0;i<num_hidden_states;i++){
    		for(int j=0;j<num_obvious_states;j++){
    			double numerator=0;
    			double denominator=0;
    			for(int t=0;t<num_observed_samples;t++){
    				if(emission_index[t]==j){
    					numerator+=L[i][t];
    				}
    				denominator+=L[i][t];
    			}
    			parameterB[i][j]=numerator/denominator;
    		}
    	}
    }
    
    /**
     * Only public function exposed to client. Use <code>observed_sequence</code> which of <code>num_sample_used</code> length
     * to train HMM model, get updated HMM parameters
     * @param num_sample_used number of incoming data points
     * @param observed_sequence observed data, e.g. which event happens in the observable states
     */
    public void train_HMM_parameters(int[] observed_sequence){
    	
    	// in case of polluted computing parameters like alpha, beta, L and H
    	init_HMM_computing_parameters();

    	
    	for(int t=0;t<num_observed_samples;t++){
    		compute_alpha(t,observed_sequence[t]);
    	}
    	for(int t=num_observed_samples-1;t>=0;t--){
    		if(t==num_observed_samples-1){
    			compute_beta(t,-1);
    		}
    		else
    			compute_beta(t,observed_sequence[t+1]);
    	}
    	
    	double Pu=compute_Pu();
    	
    	for(int t=0;t<num_observed_samples;t++){
    		compute_L(Pu,t);
    	}
    	
    	for(int t=0;t<num_observed_samples-1;t++){
    		compute_H(Pu,t,observed_sequence[t+1]);
    	}
    	
    	update_HMM_parameters(observed_sequence);
    	first_train=false;
    }
    
    /**
     * Based on observed_samples, computing the probability distribution of current state
     * @param observed_samples
     */
    public void estimate_current_state(int[] observed_samples){

    	int observed_length=observed_samples.length;
    	
    	double[][] forward = new double[num_hidden_states][observed_length];
    	double[][] backward = new double[num_hidden_states][observed_length];
    	
    	for(int t=0;t<observed_length;t++){
    		if(t==0){
        		for(int k=0;k<num_hidden_states;k++){
        			forward[k][t]=pi[k]*parameterB[k][0];
        		}
            }else{
            	int u = observed_samples[t];
            	for(int k=0;k<num_hidden_states;k++){
            		double sum=0;
            		for(int l=0;l<num_hidden_states;l++){
            			sum+=forward[l][t-1]*parameterA[l][k];
            		}
            		forward[k][t]=parameterB[k][u]*sum;
            	}
            }
    	}
    	
    	for(int t=observed_length-1;t>=0;t--){
    		if(t==observed_length-1){
        		for(int k=0;k<num_hidden_states;k++){
        			backward[k][t]=1;
        		}
            }else{
            	int u = observed_samples[t+1];//observation_index(m[index],bid,ask);
            	for(int k=0;k<num_hidden_states;k++){
            		backward[k][t]=0;
            		for(int l=0;l<num_hidden_states;l++){
            			backward[k][t]+=parameterA[k][l]*parameterB[l][u]*backward[l][t+1];
            		}
            	}

            }
    	}
    		
    	double Pu=0.0;
    	for(int k=0;k<num_hidden_states;k++){
    		for(int t=0;t<observed_length;t++){
    			Pu+=forward[k][t]*backward[k][t];
    		}
    	}
    	Pu/=observed_length;
    	
    	
	    for(int k=0;k<num_hidden_states;k++){
	    	current_state[k]=forward[k][observed_length-1]*backward[k][observed_length-1]/Pu;
//	    	if(current_state[k]*0!=0){
//	    		System.out.println("current state now is NaN");
//	    	}
	    }
    }
    
    public void estimate_next_state(){
    	for(int i=0;i<num_hidden_states;i++){
    		next_state[i]=0;
    		for(int j=0;j<num_hidden_states;j++){
    			next_state[i]+=current_state[j]*parameterA[j][i];
    		}
    	}
    }
    
    public int[] predict_emission_index_pair(){
    	int[] emission_index=new int[2];
    	int k=0;
    	double max=current_state[0];
    	for(int i=0;i<current_state.length;i++){
    		if(max<current_state[i]){
    			max=current_state[i];
    			k=i;
    		}
    	}
    	int p=0;
    	max=parameterB[k][0];
    	for(int i=0;i<parameterB[k].length;i++){
    		if(max<parameterB[k][i]){
    			p=i;
    			max=parameterB[k][i];
    		}
    	}
    	emission_index[1] = p%2;
    	emission_index[0] = (p-emission_index[1])/2;
    	return emission_index;
    	
    }
    
}
