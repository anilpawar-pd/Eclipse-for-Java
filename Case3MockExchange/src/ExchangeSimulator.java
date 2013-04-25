import org.chicago.cases.AbstractCase3;

import java.util.List;

// This is just for testing. In case you want a simple
// simulator and don't want to have to go through optionscity
public class ExchangeSimulator {
	
    public static double RunSimulator(AbstractCase3.Case3 aAlgo, List<BidAsk> myBidAsks) {

        /**
         * To be continued
         */
        double myCurrentCash = 0;
        int myCurrentPosition = 0;
        int numBidAsks = myBidAsks.size();

        for (int i =0; i != numBidAsks; i++) {
            BidAsk myBidAsk = myBidAsks.get(i);
            int myReturn = aAlgo.newBidAsk(myBidAsk.Bid, myBidAsk.Ask);

            if(myReturn != 0 && Math.abs(myReturn) != 1) {
                throw new IllegalStateException("Return Value must be (-1, 0, 1)");
            }

            if(myReturn != 0) {
                int myFillPrice = myReturn > 0 ? myBidAsk.Ask : myBidAsk.Bid;
                aAlgo.newOrderFill(myFillPrice, myReturn);
                myCurrentPosition += myReturn;
                if(Math.abs(myCurrentPosition) > 1) {
                    throw new IllegalStateException("Cannot Have Position Larger than 1");
                }

                myCurrentCash += -1 * myFillPrice * myReturn;
            }
        }

        if(myCurrentPosition == 1) {
            myCurrentCash += myBidAsks.get(numBidAsks - 1).Bid;
        } else if(myCurrentPosition == -1) {
            myCurrentCash -= myBidAsks.get(numBidAsks + 1).Ask;
        }

        return myCurrentCash;
    }
    

    public static class BidAsk {
        public final int Bid;
        public final int Ask;

        public BidAsk(int bid, int ask) {
            Bid = bid;
            Ask = ask;
        }
    }
    
}
