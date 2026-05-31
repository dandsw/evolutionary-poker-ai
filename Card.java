
/**
 * Represents a single playing card with a rank and suit.
 * Provides comparison, equality, and string representation functionalities.
 */
public class Card implements Comparable<Card> {

    private int rank; // Rank of the card: 2-10 for numbered cards, 11=Jack, 12=Queen, 13=King, 14=Ace
    private char suit; // Suit of the card: 'c' = clubs, 'd' = diamonds, 'h' = hearts, 's' = spades

    /**
     * Constructs a new Card with the given rank and suit.
     * @param rank Integer representing the card's rank.
     * @param suit Character representing the card's suit.
     */
    public Card(int rank, char suit) {
        this.rank = rank;
        this.suit = suit;
    }

    /**
     * Copy constructor - creates a new Card that is a copy of the given Card.
     * @param other The Card to copy.
     */
    public Card(Card other) {
        this.rank = other.rank;
        this.suit = other.suit;
    }

    /**
     * Returns the numeric rank of the card.
     * @return Integer value of the card's rank.
     */
    public int getCardWorth() {
        return rank;
    }

    /**
     * Returns the character representation of the suit.
     * @return Character representing the suit of the card.
     */
    public int getCardSuit() {
        return suit;
    }

    /**
     * Returns a human-readable string representation of the card.
     * Converts rank to face value if necessary.
     * @return A string like "Card: Jc" for Jack of Clubs.
     */
    @Override
    public String toString() {
        String rankStr;
        // Convert rank to its appropriate string representation
        switch (rank) {
            case 11: rankStr = "J"; break; // Jack
            case 12: rankStr = "Q"; break; // Queen
            case 13: rankStr = "K"; break; // King
            case 14: rankStr = "A"; break; // Ace
            default: rankStr = String.valueOf(rank); break; // Numeric cards
        }
        return "Card: " + rankStr + suit;
    }

    /**
     * Checks whether this card is equal to another object.
     * Equality is based on both rank and suit.
     * @param obj The object to compare with.
     * @return True if obj is a Card with the same rank and suit, otherwise false.
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Card) {
            Card other = (Card) obj;
            return this.rank == other.rank && this.suit == other.suit;
        }
        return false;
    }

    /**
     * Compares this card with another for ordering.
     * First compares ranks; if equal, compares suits (by char value).
     * @param other The other Card to compare to.
     * @return 1 if this > other, -1 if this < other, 0 if equal.
     */
    @Override
    public int compareTo(Card other) {
        if (this.rank > other.rank) {
            return 1;
        } else if (this.rank < other.rank) {
            return -1;
        } else if (this.suit > other.suit) { // Compare suits alphabetically if ranks are equal
            return 1;
        } else if (this.suit < other.suit) {
            return -1;
        } else {
            return 0;
        }
    }
}
