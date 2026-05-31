
import java.util.ArrayList;
import java.util.Collections;

/**
 * Represents a standard deck of 52 playing cards.
 * Provides functionality to create, shuffle, deal, and access the deck.
 */
public class Deck {

    private ArrayList<Card> deckCards; // List holding all cards currently in the deck

    /**
     * Default constructor: initializes the deck with all 52 standard cards.
     * Cards range from rank 2 to 14 (Ace) across all four suits.
     */
    public Deck() {
        deckCards = new ArrayList<>();
        // Iterate over all four suits
        for (char suit : new char[] {'c', 'd', 'h', 's'}) {  // 'c'=clubs, 'd'=diamonds, 'h'=hearts, 's'=spades
            // Iterate over all ranks from 2 (Two) to 14 (Ace)
            for (int rank = 2; rank <= 14; rank++) {
                deckCards.add(new Card(rank, suit)); // Add new card with current rank and suit
            }
        }
    }

    /**
     * Returns a string representation of the deck.
     * @return A formatted string listing all cards currently in the deck.
     */
    @Override
    public String toString() {
        return "Deck: " + deckCards.toString(); // Convert the full list of cards to a string
    }

    /**
     * Deals the top card of the deck (first in the list).
     * @return The dealt Card object, or null if the deck is empty.
     */
    public Card deal() {
        if (!deckCards.isEmpty()) {
            return deckCards.remove(0); // Remove and return the first card in the deck
        }
        return null; // Return null if the deck is empty
    }

    /**
     * Shuffles the cards in the deck randomly
     */
    public void shuffle() {
        Collections.shuffle(deckCards);
    }

    /**
     * Returns the list of cards currently in the deck.
     * This provides direct access for external manipulation (e.g., removal).
     * @return ArrayList of Card objects representing the current deck.
     */
    public ArrayList<Card> getDeck() {
        return deckCards;
    }
}
