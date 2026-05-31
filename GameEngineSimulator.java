
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Iterator;


/**
 * GameEngine class fully simulates a poker game.
 * Manages bots, deals cards, conducts betting rounds, and determines winners.
 */
public class GameEngineSimulator {

    private static Deck d = new Deck();                        // Shared deck of cards
    private static ArrayList<Card> communityCards = new ArrayList<>(); // Community cards shared by all players

    private int gameStage = -1;        // 0 = preflop, 1 = flop, 2 = turn, 3 = river
    private int pot = 0;               // Total chips in the pot
    public int round = 0;                // Current game round
    private int bigBlind;             // Value of big blind (small blind = bigBlind / 2)
    private int turns;                // Max number of rounds in the game
    private int numberFolded = 0;     // Number of bots that folded this round
    private int maxPot = 0;           // Tracks max pot seen for scaling
    private int numberOfBots = 0;     // Total number of bots in the game
    private int callAmount;           // Current call amount bots must match
    private int minRaise = 0;         // Minimum raise amount allowed
    private int numberOfRaises = 0;   // Count of raises this round
    private int numberOfCalls = 0;    // Count of calls this round

    private LinkedList<PokerBot> pokerBots = new LinkedList<>();  // All bots in the simulation
    private Queue<PokerBot> botsInRound = new LinkedList<>();     // Bots active in current hand
    private final int startingChips;                              // Chips each bot starts with

    /**
     * Constructs the game simulator with blinds, number of turns, and initial chip count.
     * @param blind Big blind value
     * @param turns Number of rounds to simulate
     * @param startingChips Initial chips per bot
     */
    public GameEngineSimulator(int blind, int turns, int startingChips) {
        this.bigBlind = blind;
        this.turns = turns;
        this.startingChips = startingChips;
    }

    /**
     * Adds poker bots to the game.
     * @param bots Variable-length list of PokerBot objects
     */
    
    public void addBots(PokerBot... bots) {
        for (PokerBot bot : bots) {
            pokerBots.add(bot);
            maxPot += startingChips;
            numberOfBots++;
        }
    }

    /**
     * Resets the game state and removes bots that have run out of chips.
     */
    protected void resetGame() {
        d = new Deck();  // Reset deck
        Iterator<PokerBot> iter = pokerBots.iterator();

        while (iter.hasNext()) {
            PokerBot bot = iter.next();
            if (bot.getChips() == 0) {
                iter.remove(); // Eliminate bankrupt bot
            } else {
                bot.setDefaultSettings(); // Reset round-specific state
            }
        }

        communityCards.clear();
        pot = 0;
        round++;
        gameStage = -1;
        numberFolded = 0;
        numberOfRaises = 0;
        numberOfCalls = 0;
    }

    /**
     * Simulates the full multi-hand poker game until one bot remains or max rounds reached.
     */
    public void simulateGame() {
        outer:
        while (round <= turns && pokerBots.size() > 1) {
            d.shuffle();

            // Assign blinds
            pokerBots.get(pokerBots.size() - 2).setSmallBlind();
            pokerBots.get(pokerBots.size() - 1).setBigBlind();

            botsInRound.clear();

            // Deal two hole cards to each bot
            for (PokerBot bot : pokerBots) {
                bot.addCard(d.deal());
                bot.addCard(d.deal());
                botsInRound.add(bot);
            }

            gameStage++;
            pokerBots.add(pokerBots.remove()); // Rotate dealer

            callAmount = bigBlind;
            minRaise = bigBlind;

            // Preflop betting
            if (runBettingRound()) {
                resetGame();
                continue outer;
            }

            dealFlop();

            // Post-flop betting
            if (runBettingRound()) {
                resetGame();
                continue outer;
            }

            dealCard(); // Turn

            if (runBettingRound()) {
                resetGame();
                continue outer;
            }

            dealCard(); // River

            if (runBettingRound()) {
                resetGame();
                continue outer;
            }

            // Determine winner and reset
            determineWinner(botsInRound);
            resetGame();
        }
    }

    /**
     * Executes a full betting round among active bots.
     * @return true if the hand ends early (due to all but one folding), false otherwise.
     */
    protected boolean runBettingRound() {
        int actionsTaken = 0;
        int maxActions = botsInRound.size();

        while (botsInRound.size() > 1 && actionsTaken < maxActions) {
            PokerBot bot = botsInRound.remove();
            if (bot.getChips() > 0) {
                NeuralNetwork.Actions action = bot.takeTurn();
                bot.increaseRound();

                if (action == NeuralNetwork.Actions.FOLD) {
                    maxActions--;
                    numberFolded++;
                    continue;
                }

                if (action == NeuralNetwork.Actions.RAISE) {
                    actionsTaken = 0;
                    numberOfRaises++;
                } else {
                    numberOfCalls++;
                    actionsTaken++;
                }

                botsInRound.add(bot);

                // If only one bot remains, award the pot
                if (botsInRound.size() == 1) {
                    botsInRound.remove().addChips(pot);
                    round++;
                    return true;
                }

            } else {
                actionsTaken++;
                botsInRound.add(bot); // Keep them in queue but skip turn
            }
        }

        return false;
    }

    /**
     * Determines the winner(s) among the remaining bots and awards the pot.
     * Handles splitting the pot in the event of a tie.
     */
    private void determineWinner(Queue<PokerBot> bots) {
        ArrayList<PokerBot> winningBots = new ArrayList<>();
        PokerBot bot1 = bots.remove(); // First contender
        winningBots.add(bot1);

        while (!bots.isEmpty()) {
            PokerBot bot = bots.remove();
            int comparison = bot.getBestCards().compareTo(winningBots.get(0).getBestCards());

            if (comparison > 0) {
                winningBots.clear();
                winningBots.add(bot);
            } else if (comparison == 0) {
                winningBots.add(bot); // Tie
            }
        }

        if (winningBots.size() == 1) {
            winningBots.get(0).addChips(pot);
            winningBots.get(0).handWon();
        } else {
            int winningShare = pot / winningBots.size();
            for (PokerBot winner : winningBots) {
                winner.addChips(winningShare);
                winner.handWon();
            }
        }
    }

    // ===== Getter Methods =====

    public LinkedList<PokerBot> getBots() {
        return pokerBots;
    }

    public int getStartingChips() {
        return startingChips;
    }

    /**
     * Deals the flop (first 3 community cards).
     */
    public void dealFlop() {
        for (int i = 0; i < 3; i++) {
            communityCards.add(d.deal()); // Deal 3 cards face-up
        }
        gameStage++;
    }

    /**
     * Deals one community card (used for turn and river).
     */
    public void dealCard() {
        gameStage++;
        communityCards.add(d.deal());
    }

    // ===== Getter Methods =====

    public int getGameStage() {
        return gameStage;
    }

    public int getPot() {
        return pot;
    }

    public int getMaxPot() {
        return maxPot;
    }

    public int getTotalBots() {
        return numberOfBots;
    }

    public int getFolded() {
        return numberFolded;
    }

    public int getMaxRound() {
        return turns;
    }

    public int getRound() {
        return round;
    }

    public Deck getDeck() {
        return d;
    }

    public ArrayList<Card> getCommunityCards() {
        return communityCards;
    }

    public int getMinRaise() {
        return minRaise;
    }

    public int getBigBlind() {
        return bigBlind;
    }

    public int getSmallBlind() {
        return bigBlind / 2;
    }

    public int getNumberOfRaise() {
        return numberOfRaises;
    }

    public int getNumberOfCalls() {
        return numberOfCalls;
    }

    public int getCallAmount() {
        return callAmount;
    }

    // ===== Setter Methods =====

    /**
     * Adds chips to the pot.
     * @param c Amount to add
     */
    public void addToPot(int c) {
        pot += c;
    }

    /**
     * Increases the current call amount if the raise is positive.
     * @param c Raise amount
     */
    public void increaseCall(int c) {
        if (c > 0) {
            callAmount += c;
        }
    }

    /**
     * Increases the minimum raise amount for the round.
     * @param c Amount to add to the minimum raise
     */
    public void addtoMinRaise(int c) {
        minRaise += c;
    }
}
