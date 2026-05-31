
import java.util.ArrayList;


/**
 * Represents an AI-controlled poker bot.
 * It uses a neural network to decide actions during the game and tracks performance for evolutionary evaluation.
 * Includes logic for betting, raising, calling, folding, hand strength evaluation, and fitness scoring.
 */
public class PokerBot implements Comparable<PokerBot> {

    private int chips;                         // Current chip count
    public Hand h = new Hand();                // Current hand (2 cards)
    private float handStrength = 0;            // Precomputed hand strength
    private GameEngineSimulator engine;        // Game engine reference
    private boolean smallBlind = false;        // Whether bot is small blind
    private boolean bigBlind = false;          // Whether bot is big blind
    private NeuralNetwork genes;               // Neural network decision system
    private BotGenetics botGenetics;           // Genetic encoding of bot
    public int raiseAmount = 0;                // Amount bot wants to raise
    private int amountWagered = 0;             // Total amount wagered in current round
    private int lastGameStage = -1;            // Tracks if board state changed
    private Hand bestCards = null;             // Best 5-card hand
    private boolean folded = false;            // Whether bot has folded
    private int handsWon = 0;                  // Number of hands won
    private int numRaises = 0;                 // Count of raises
    private int numFold = 0;                   // Count of folds
    private int numCalled = 0;                 // Count of calls

    private int totalChipsWon = 0;             // Chips won over all rounds
    private int totalChipsStarted = 0;         // Starting chips for fitness
    private int totalRounds = 0;               // Total rounds played

    private String name;                       // Bot's name

    /**
     * Constructs a PokerBot with genetic weights and a name.
     */
    public PokerBot(BotGenetics g, String name) {
        this.botGenetics = g;
        this.name = name;
    }

    /**
     * Uses the neural network to choose an action: RAISE, CALL, or FOLD.
     * Applies the resulting action and updates state accordingly.
     */
    public NeuralNetwork.Actions takeTurn() {
        calculateHandStrength();

        NeuralNetwork.Actions action = genes.takeTurn(
            chips, handStrength, engine.getGameStage(), engine.getPot(),
            engine.getRound(), engine.getFolded(), engine.getNumberOfRaise(),
            engine.getNumberOfCalls());

        switch (action) {
            case FOLD:
                if (bigBlind && engine.getGameStage() == 0) {
                    chips -= engine.getBigBlind();
                    engine.addToPot(engine.getBigBlind());
                } else if (smallBlind && engine.getGameStage() == 0) {
                    chips -= engine.getSmallBlind();
                    engine.addToPot(engine.getSmallBlind());
                }
                numFold++;
                break;

            case CALL:
                call();
                numCalled++;
                break;

            case RAISE:
                numRaises++;
                raise();
                break;
        }

        return action;
    }

    /**
     * Matches the current call amount in the game.
     */
    public void call() {
        int call = engine.getCallAmount();
        int amtToCall = call - amountWagered;

        if (chips - amtToCall <= 0) {
            engine.addToPot(chips);
            chips = 0;
        } else {
            chips -= amtToCall;
            engine.addToPot(amtToCall);
            amountWagered += amtToCall;
        }
    }

    /**
     * Executes a raise, modifying pot and internal chip count accordingly.
     */
    public void raise() {
        setRaise();
        int amtToCall = engine.getCallAmount() - amountWagered;

        if (chips - amtToCall - raiseAmount > 0) {
            chips -= (amtToCall + raiseAmount);
            engine.addToPot(amtToCall + raiseAmount);
            engine.increaseCall(raiseAmount);
            amountWagered += amtToCall + raiseAmount;
            engine.addtoMinRaise(raiseAmount);
        } else {
            engine.addToPot(chips);
            engine.increaseCall(chips - amtToCall);
            engine.addtoMinRaise(chips - amtToCall);
            chips = 0;
        }
    }

    /**
     * Calculates the strength of the current hand using precomputed statistics.
     */
    public void calculateHandStrength() {
        if (lastGameStage < engine.getGameStage()) {
            lastGameStage = engine.getGameStage();
            if (lastGameStage == 0) {
                h.sortCards();
                handStrength = HandStrengthGenerator.getPrecomputedStrengths().get(h.toString());
            } else {
                setBestCards();
                String key = getKey();
                handStrength = HandStrengthGenerator.getPrecomputedStrengths().get(key);
            }
        }
    }

    /**
     * Builds a key string for the current best 5-card hand for strength lookup.
     */
    private String getKey() {
        String rank = bestCards.getRank().toString();
        switch (rank) {
            case "HIGH_CARD":
                return rank + " | " + bestCards.getHighCard();
            case "PAIR":
                return rank + " | " + bestCards.getHighPair();
            case "TWO_PAIR":
                return rank + " | " + bestCards.getHighPair() + bestCards.getSecondPair();
            case "THREE_OF_A_KIND":
                return rank + " | " + bestCards.getThreeOfAKind();
            case "STRAIGHT":
            case "STRAIGHT_FLUSH":
                return rank + " | " + bestCards.getHighCard();
            case "FLUSH":
                return rank;
            case "FULL_HOUSE":
                return rank + " | " + bestCards.getHighPair() + bestCards.getThreeOfAKind();
            case "FOUR_OF_A_KIND":
                return rank + " | " + bestCards.getFourOfAKind();
            default:
                System.out.println("Error");
                return " ";
        }
    }

    /**
     * Finds the best possible 5-card hand combining hole and community cards.
     */
    private void setBestCards() {
        ArrayList<ArrayList<Card>> boardCombos = generate3CardCombinations(engine.getCommunityCards());

        for (ArrayList<Card> combo : boardCombos) {
            Hand potential = new Hand(combo);
            potential.addHand(h);
            potential.rankHand();

            if (bestCards == null || potential.compareTo(bestCards) > 0) {
                bestCards = new Hand(potential);
            }
        }
    }

    /**
     * Generates all 3-card combinations from the community cards.
     */
    private ArrayList<ArrayList<Card>> generate3CardCombinations(ArrayList<Card> cards) {
        ArrayList<ArrayList<Card>> result = new ArrayList<>();
        if (cards.size() < 3 || cards.size() > 5) return result;
        backtrack(cards, 0, new ArrayList<>(), result);
        return result;
    }

    /**
     * Helper method for recursively building 3-card combinations.
     */
    private void backtrack(ArrayList<Card> cards, int start, ArrayList<Card> current, ArrayList<ArrayList<Card>> result) {
        if (current.size() == 3) {
            result.add(new ArrayList<>(current));
            return;
        }

        for (int i = start; i < cards.size(); i++) {
            current.add(cards.get(i));
            backtrack(cards, i + 1, current, result);
            current.remove(current.size() - 1);
        }
    }

    /**
     * Resets bot state for a new round with given chips.
     */
    public void reset(int chips) {
        totalChipsWon += this.chips;
        this.chips = chips;
        totalChipsStarted += chips;
        this.h = new Hand();
        this.bestCards = null;
        this.raiseAmount = 0;
        this.amountWagered = 0;
        this.lastGameStage = -1;
    }

    /**
     * Resets blinds and round-related state.
     */
    public void setDefaultSettings() {
        bigBlind = false;
        smallBlind = false;
        h = new Hand();
        amountWagered = 0;
        lastGameStage = -1;
    }

    /**
     * Calculates and returns the fitness score of the bot.
     * Penalizes one-dimensional behavior and rewards chip gain and win rate.
     */
    private double evaluateFitness() {
        double chipScore = (totalChipsWon - totalChipsStarted) / (double) totalChipsStarted;
        chipScore = Math.max(0, chipScore);

        double winScore = handsWon / (double) Math.max(1, totalRounds);
        double raisePenalty = Math.max(0, getRaiseRate() - 0.4);
        double foldPenalty = Math.max(0, getFoldRate() - 0.95);

        // Penalize bots that only fold/call/raise or never use any
        if (getCallRate() == 1 || getFoldRate() == 1 || getRaiseRate() == 1) return 0;
        if (getCallRate() == 0 || getFoldRate() == 0 || getRaiseRate() == 0) return 0;

        return 0.7 * chipScore + 0.8 * winScore - 0.5 * raisePenalty - 0.2 * foldPenalty;
    }

    @Override
    public int compareTo(PokerBot other) {
        return Double.compare(this.evaluateFitness(), other.evaluateFitness());
    }

    public void increaseRound() {
        totalRounds++;
    }

    public void handWon() {
        handsWon++;
    }

    // ==== Getter Methods ====

    public double getFitness(){ 
        return evaluateFitness(); 
    }

    public BotGenetics getGenes(){ 
        return botGenetics; 
    }

    public boolean getFolded(){ 
        return folded; 
    }

    public Hand getBestCards(){
        setBestCards();
        return bestCards;
    }

    public int getChips(){ 
        return chips; 
    }

    public String getName(){ 
        return name; 
    }

    public double getRaiseRate(){
        return numRaises / (double) Math.max(1, totalRounds);
    }

    public double getFoldRate(){
        return numFold / (double) Math.max(1, totalRounds);
    }

    public double getCallRate(){
        return numCalled / (double) Math.max(1, totalRounds);
    }

    public int getHandsWon(){ 
        return handsWon; 
    }

    // ==== Setter Methods ====

    public void setSmallBlind(){ 
        smallBlind = true; 
    }

    public void setBigBlind(){ 
        bigBlind = true; 
    }

    public void addChips(int c){ 
        chips += c; 
    }

    public void setEngine(GameEngineSimulator engine) {
        this.engine = engine;
        this.genes = new NeuralNetwork(engine, botGenetics); // Initialize neural net using game context
    }

    public void addCard(Card c) {
        h.addCard(c);
    }

    public void setRaise() {
        raiseAmount = genes.getRaise() + engine.getMinRaise();
    }
}
