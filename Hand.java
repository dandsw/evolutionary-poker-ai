
import java.util.ArrayList;


/**
 * Represents a hand of playing cards.
 * Provides methods to add cards, evaluate rank, compare with other hands,
 * and manage hand-specific data such as pairs, high card, etc.
 */
public class Hand implements Comparable<Hand> {

    private ArrayList<Card> handCards; // List of cards currently in this hand

    // Variables used to store evaluated hand information
    private HandEvaluator.Ranking rank; // Hand ranking (e.g., PAIR, STRAIGHT, etc.)
    private int highCard; // Value of the highest card (used in HIGH_CARD, STRAIGHT, etc.)
    private int highPair; // Rank of the highest pair
    private int secondPair; // Rank of the second pair (used in TWO_PAIR)
    private int threeOfAKind; // Rank of the three-of-a-kind
    private int fourOfAKind; // Rank of the four-of-a-kind

    /**
     * Default constructor: creates an empty hand.
     */
    public Hand() {
        handCards = new ArrayList<>();
    }

    /**
     * Constructs a hand with a variable number of cards.
     * @param cards A list of Card objects to add to the hand.
     */
    public Hand(Card... cards) {
        handCards = new ArrayList<>();
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Constructs a hand from an existing list of cards.
     * @param h ArrayList of Card objects to initialize the hand.
     */
    public Hand(ArrayList<Card> h) {
        handCards = h;
    }

    /**
     * Copy constructor: creates a new hand with the same content and metadata as another hand.
     * @param other The Hand object to copy.
     */
    public Hand(Hand other) {
        this.handCards = other.handCards;
        this.rank = other.rank;
        this.highCard = other.highCard;
        this.highPair = other.highPair;
        this.secondPair = other.secondPair;
        this.threeOfAKind = other.threeOfAKind;
        this.fourOfAKind = other.fourOfAKind;
    }

    /**
     * Adds a single card to the hand.
     * @param card The Card object to add.
     */
    public void addCard(Card card) {
        handCards.add(card);
    }

    /**
     * Adds a list of cards to the hand.
     * @param cards List of Card objects to add.
     */
    public void addCards(ArrayList<Card> cards) {
        for (Card card : cards) {
            addCard(card);
        }
    }

    /**
     * Adds all cards from another hand to this hand.
     * @param other The Hand to merge into this one.
     */
    public void addHand(Hand other) {
        for (int i = 0; i < other.getHand().size(); i++) {
            addCard(other.getHand().get(i));
        }
    }

    /**
     * Sorts the cards in the hand and evaluates the hand rank using HandEvaluator.
     */
    public void rankHand() {
        sortCards();
        HandEvaluator.rankHand(this); 
    }

    /**
     * Clears the hand and resets all rank-related variables.
     */
    public void clear() {
        handCards.clear();
        rank = null;
        highCard = 0;
        highPair = 0;
        secondPair = 0;
        threeOfAKind = 0;
        fourOfAKind = 0;
    }

    /**
     * Sorts the hand using insertion sort by card rank and suit.
     */
    public void sortCards() {
        for (int i = 1; i < handCards.size(); i++) {
            Card toInsert = handCards.get(i);
            int j = i;
            while (j > 0 && toInsert.compareTo(handCards.get(j - 1)) < 0) {
                handCards.set(j, handCards.get(j - 1));
                j--;
            }
            handCards.set(j, toInsert);
        }
    }

    /**
     * Checks if the hand contains a specific card.
     * @param target The card to search for.
     * @return True if found, false otherwise.
     */
    public boolean contains(Card target) {
        for (Card c : handCards) {
            if (c.equals(target)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns a string representation of the hand.
     * @return Space-separated string of card descriptions.
     */
    @Override
    public String toString() {
        String handStr = "";
        for (Card cards : handCards) {
            handStr = handStr + cards.toString() + " ";
        }
        return handStr;
    }

    /**
     * Compares this hand to another based on rank and tie-breaking logic.
     * @param other The other Hand to compare against.
     * @return 1 if this hand is better, -1 if worse, 0 if tied.
     */
    @Override
    public int compareTo(Hand other) {
        if (this.getRank().ordinal() > other.getRank().ordinal()) {
            return 1;
        } else if (this.getRank().ordinal() < other.getRank().ordinal()) {
            return -1;
        } else {
            return compareSameRank(other);
        }
    }

    /**
     * Breaks ties between two hands of the same rank using rank-specific logic.
     * @param other The hand to compare to.
     * @return Comparison result (-1, 0, or 1).
     */
    private int compareSameRank(Hand other) {
        switch (rank) {
            case HIGH_CARD:
                return checkHighestCard(other);
            case STRAIGHT:
            case STRAIGHT_FLUSH:
                return Integer.compare(this.getHighCard(), other.getHighCard());
            case PAIR:
                if (this.getHighPair() != other.getHighPair()) {
                    return Integer.compare(this.getHighPair(), other.getHighPair());
                }
                return checkHighestCard(other);
            case TWO_PAIR:
                if (this.getHighPair() != other.getHighPair()) {
                    return Integer.compare(this.getHighPair(), other.getHighPair());
                } else if (this.getSecondPair() != other.getSecondPair()) {
                    return Integer.compare(this.getSecondPair(), other.getSecondPair());
                }
                return checkHighestCard(other);
            case THREE_OF_A_KIND:
                if (this.getThreeOfAKind() != other.getThreeOfAKind()) {
                    return Integer.compare(this.getThreeOfAKind(), other.getThreeOfAKind());
                }
                return checkHighestCard(other);
            case FLUSH:
                return 0; // HandEvaluator assumes equal flushes are ties
            case FULL_HOUSE:
                if (this.getThreeOfAKind() != other.getThreeOfAKind()) {
                    return Integer.compare(this.getThreeOfAKind(), other.getThreeOfAKind());
                } else {
                    return Integer.compare(this.getHighPair(), other.getHighPair());
                }
            case FOUR_OF_A_KIND:
                if (this.getFourOfAKind() != other.getFourOfAKind()) {
                    return Integer.compare(this.getFourOfAKind(), other.getFourOfAKind());
                }
                return checkHighestCard(other);
            default:
                return 0;
        }
    }

    /**
     * Compares individual card values from highest to lowest to break ties.
     * @param other The hand to compare against.
     * @return 1 if this hand wins, -1 if other wins, 0 if identical.
     */
    private int checkHighestCard(Hand other) {
        for (int i = this.handCards.size() - 1; i >= 0; i--) {
            int myCard = this.getHand().get(i).getCardWorth();
            int otherCard = other.getHand().get(i).getCardWorth();
            if (myCard > otherCard) return 1;
            else if (myCard < otherCard) return -1;
        }
        return 0;
    }

    // ======= Getter Methods =======

    public int size() {
        return handCards.size();
    }

    public int getHighCard() {
        return highCard;
    }

    public int getHighPair() {
        return highPair;
    }

    public int getSecondPair() {
        return secondPair;
    }

    public int getThreeOfAKind() {
        return threeOfAKind;
    }

    public int getFourOfAKind() {
        return fourOfAKind;
    }

    public ArrayList<Card> getHand() {
        return handCards;
    }

    public HandEvaluator.Ranking getRank() {
        return rank;
    }

    // ======= Setter Methods =======

    public void setRank(HandEvaluator.Ranking newRank) {
        rank = newRank;
    }

    public void setHighCard(int rank) {
        highCard = rank;
    }

    public void setHighPair(int rank) {
        highPair = rank;
    }

    public void setSecondPair(int rank) {
        secondPair = rank;
    }

    public void setThreeOfAKind(int rank) {
        threeOfAKind = rank;
    }

    public void setFourOfAKind(int rank) {
        fourOfAKind = rank;
    }
}
