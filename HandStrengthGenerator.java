
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The HandStrengthGenerator class is responsible for simulating and 
 * precomputing the win probabilities (hand strengths) of poker hands.
 * It handles both generic two-card starting hands and specific ranked 5-card hands.
 */
public class HandStrengthGenerator {

    // Number of simulations to run per hand for estimating strength
    private static final int SIMULATIONS_PER_HAND = 5000;

    // Map to store precomputed hand strengths: key = hand description, value = win probability
    private static final Map<String, Float> precomputedStrengths = new HashMap<>();

    /**
     * Generates precomputed hand strength values for all types of hands.
     */
    public static void generateAllPrecomputedStrengths() {
        preFlopSim();                  // Simulate all preflop two-card hand combinations
        simulateSpecificRankHands();  // Simulate specific ranked 5-card hands
    }

    /**
     * Retrieves the map of all precomputed hand strengths.
     * @return Map of hand string -> float hand strength
     */
    public static Map<String, Float> getPrecomputedStrengths() {
        return precomputedStrengths;
    }

    /**
     * Generates all unique two-card hand combinations from a full deck.
     * @return List of all two-card hands
     */
    private static List<Hand> generateAllTwoCardHands() {
        List<Hand> hands = new ArrayList<>();
        Deck deck = new Deck();
        ArrayList<Card> cards = deck.getDeck();  // All cards in the deck
    
        int n = cards.size();
        for (int i = 0; i < n; i++) {
            for (int j = i + 1; j < n; j++) {
                Card c1 = cards.get(i);
                Card c2 = cards.get(j);
                hands.add(new Hand(c1, c2));  // Create and add new two-card hand
            }
        }
    
        return hands;
    }

    /**
     * Simulates pre-flop poker scenarios for every two-card starting hand,
     * calculating their win/tie rates against random opponents.
     */
    private static void preFlopSim(){
        List<Hand> allTwoCardHands = generateAllTwoCardHands();

        for (Hand hand : allTwoCardHands) {
            int win = 0, tie = 0;  // Track win and tie outcomes

            for (int i = 0; i < SIMULATIONS_PER_HAND; i++) {
                Deck deck = new Deck();
                deck.shuffle();

                // Remove hero (our) cards from the deck
                for (Card card : hand.getHand()) {
                    deck.getDeck().removeIf(c -> c.equals(card));
                }

                // Generate community board (flop only)
                ArrayList<Card> board = new ArrayList<>();
                while (board.size() < 3) {
                    Card c = deck.deal();
                    board.add(c);
                }

                // Generate opponent hand
                Card opp1 = deck.deal();
                Card opp2 = deck.deal();
                Hand opponent = new Hand(opp1, opp2);

                // Evaluate hero's full hand
                Hand heroFullHand = new Hand(board);
                heroFullHand.addHand(hand);
                heroFullHand.rankHand();

                // Evaluate opponent's full hand
                Hand oppFullHand = new Hand(board);
                oppFullHand.addHand(opponent);
                oppFullHand.rankHand();

                // Compare results
                int result = heroFullHand.compareTo(oppFullHand);
                if (result > 0) win++;
                else if (result == 0) tie++;
            }

            hand.sortCards();  // Normalize hand order for consistent key
            String key = hand.toString();
            float handStrength = (win + tie / 2.0f) / SIMULATIONS_PER_HAND;
            precomputedStrengths.put(key, handStrength);  // Store result
        }
    }

    /**
     * Generates representative 5-card hands for each hand ranking category (e.g. pairs, straights, etc.).
     * @return List of representative hands by rank category
     */
    private static List<Hand> generateRepresentativeHandsByRank() {
        List<Hand> hands = new ArrayList<>();
        char[] suits = {'c', 'd', 'h', 's'};  // Possible suits

        // HIGH CARD hands
        for (int rank = 7; rank <= 14; rank++) {
            hands.add(new Hand(
                new Card(rank, 'c'), new Card(2, 'd'), new Card(3, 'h'),
                new Card(4, 's'), new Card(5, 'c')));
        }

        // PAIR hands
        for (int rank = 2; rank <= 14; rank++) {
            if (rank == 3){
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                   new Card(4, 'h'), new Card(5, 's'), new Card(9, 'c')));
            } else if (rank == 5){
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                   new Card(3, 'h'), new Card(4, 's'), new Card(9, 'c')));
            } else if (rank == 9){
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                   new Card(4, 'h'), new Card(5, 's'), new Card(8, 'c')));
            } else {
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                   new Card(3, 'h'), new Card(5, 's'), new Card(9, 'c')));
            }
        }

        // TWO PAIR hands
        for (int rank = 2; rank <= 14; rank++) {
            for (int rank2 = rank+1; rank2 <=14; rank2++){
                if (rank == 9){
                    hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                       new Card(rank2, 'h'), new Card(rank2, 's'), new Card(5, 'c')));
                } else if (rank2 == 9){
                    hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                       new Card(rank2, 'h'), new Card(rank2, 's'), new Card(10, 'c')));
                } else {
                    hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'h'),
                                       new Card(rank2, 'h'), new Card(rank2, 's'), new Card(9, 'c')));
                } 
            } 
        }

        // THREE OF A KIND hands
        for (int rank = 2; rank <= 14; rank++) {
            if (rank == 5){
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'd'), new Card(rank, 'h'),
                                   new Card(4, 's'), new Card(9, 'c')));
            } else if (rank == 9) {
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'd'), new Card(rank, 'h'),
                                   new Card(5, 's'), new Card(10, 'c')));
            } else {
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'd'), new Card(rank, 'h'),
                                   new Card(5, 's'), new Card(9, 'c')));
            }
        }

        // FOUR OF A KIND hands
        for (int rank = 2; rank <= 14; rank++) {
            if (rank == 3){
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'd'), new Card(rank, 'h'), new Card(rank, 's'),
                                   new Card(4, 'h')));
            } else {
                hands.add(new Hand(new Card(rank, 'c'), new Card(rank, 'd'), new Card(rank, 'h'), new Card(rank, 's'),
                                   new Card(3, 'h')));
            }
        }

        // FULL HOUSE hands
        for (int three = 2; three <= 14; three++) {
            for (int pair = 2; pair <= 14; pair++) {
                if (pair == three) continue;
                hands.add(new Hand(new Card(three, 'c'), new Card(three, 'd'), new Card(three, 'h'),
                                   new Card(pair, 'c'), new Card(pair, 'd')));
            }
        }

        // STRAIGHT hands
        for (int low = 2; low <= 10; low++) {
            List<Card> straight = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int val = low + i;
                straight.add(new Card(val, suits[i % 4]));
            }
            hands.add(new Hand(new ArrayList<>(straight)));
        }

        // FLUSH hands
        hands.add(new Hand(new Card(2, 'c'), new Card(6, 'c'), new Card(9, 'c'), new Card(10, 'c'), new Card(12, 'c')));

        // STRAIGHT FLUSH hands
        for (int low = 1; low <= 10; low++) {
            List<Card> sf = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                int val = (low + i - 1) % 13 + 1;
                if (val == 1) val = 14;
                sf.add(new Card(val, 's'));
            }
            hands.add(new Hand(new ArrayList<>(sf)));
        }

        return hands;
    }

    /**
     * Simulates matchups between ranked 5-card hands and random 5-card opponent hands.
     * Stores resulting average win rates per rank type.
     */
    private static void simulateSpecificRankHands() {
        List<Hand> testHands = generateRepresentativeHandsByRank();

        for (Hand ourHand : testHands) {
            ourHand.rankHand();
            String rank = ourHand.getRank().toString();

            int win = 0,  tie = 0;

            for (int j = 0; j < SIMULATIONS_PER_HAND; j++) {
                Deck newDeck = new Deck();
                newDeck.shuffle();

                // Generate random opponent hand
                Hand opponent = new Hand();
                while (opponent.size() < 5) {
                    Card c = newDeck.deal();
                    if (!ourHand.contains(c)) opponent.addCard(c);
                }

                opponent.rankHand();

                // Compare our hand to opponent's
                int result = ourHand.compareTo(opponent);
                if (result > 0) win++;
                else if (result == 0) tie++;
            }

            // Construct a unique key for each type of ranked hand
            String key = "";
            float handStrength = (win + tie / 2.0f) / SIMULATIONS_PER_HAND;
            switch (rank){
                case "HIGH_CARD":
                    key = rank + " | " +  ourHand.getHighCard();
                    break;
                case "PAIR":
                    key = rank + " | " +  ourHand.getHighPair();
                    break;
                case "TWO_PAIR": 
                    key = rank + " | " + ourHand.getHighPair() + ourHand.getSecondPair();
                    break;
                case "THREE_OF_A_KIND":
                    key = rank + " | " +  ourHand.getThreeOfAKind();
                    break;
                case "STRAIGHT": 
                    key = rank + " | " + ourHand.getHighCard();
                    break;
                case "FLUSH":
                    key = rank;
                    break;
                case "FULL_HOUSE": 
                    key = rank + " | " + ourHand.getHighPair() + ourHand.getThreeOfAKind();
                    break;
                case "FOUR_OF_A_KIND":
                    key = rank + " | " +  ourHand.getFourOfAKind();
                    break;
                case "STRAIGHT_FLUSH": 
                    key = rank + " | " + ourHand.getHighCard();
                    break;
            }   
            precomputedStrengths.putIfAbsent(key, handStrength);
        }
    }
}
