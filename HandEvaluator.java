
/**
 * The HandEvaluator class is responsible for determining the ranking of poker hands.
 * It includes logic to evaluate different hand types (e.g., Pair, Flush, Full House)
 * and set the appropriate metadata on a Hand object.
 */
public class HandEvaluator {

    
    public HandEvaluator() {}

    /**
     * Enumeration representing the possible poker hand rankings.
     */
    public enum Ranking {
        HIGH_CARD, PAIR, TWO_PAIR, THREE_OF_A_KIND,
        STRAIGHT, FLUSH, FULL_HOUSE, FOUR_OF_A_KIND, STRAIGHT_FLUSH
    }

    /**
     * Determines the best ranking for a given Hand.
     * Applies hand ranking rules in descending order of strength.
     * @param cards The hand to evaluate.
     */
    public static void rankHand(Hand cards) {
        if (cards.size() > 4 && isStraightFlush(cards)) {
            cards.setRank(Ranking.STRAIGHT_FLUSH);
        } else if (cards.size() > 4 && isFourOfAKind(cards)) {
            cards.setRank(Ranking.FOUR_OF_A_KIND);
        } else if (cards.size() > 4 && isFullHouse(cards)) {
            cards.setRank(Ranking.FULL_HOUSE);
        } else if (cards.size() > 4 && isFlush(cards)) {
            cards.setRank(Ranking.FLUSH);
        } else if (cards.size() > 4 && isStraight(cards)) {
            cards.setRank(Ranking.STRAIGHT);
        } else if (cards.size() > 4 && isThreeOfAKind(cards)) {
            cards.setRank(Ranking.THREE_OF_A_KIND);
        } else if (cards.size() > 4 && isTwoPair(cards)) {
            cards.setRank(Ranking.TWO_PAIR);
        } else if (isPair(cards)) {
            cards.setRank(Ranking.PAIR);
        } else {
            // Default to high card if no other rank matches
            cards.setHighCard(cards.getHand().get(cards.size() - 1).getCardWorth());
            cards.setRank(Ranking.HIGH_CARD);
        }
    }

    /**
     * Checks if the hand is a Straight Flush.
     * @param cards The hand to evaluate.
     * @return True if it's both a straight and a flush.
     */
    private static boolean isStraightFlush(Hand cards) {
        return isStraight(cards) && isFlush(cards);
    }

    /**
     * Checks if the hand contains Four of a Kind.
     * @param cards The hand to evaluate.
     * @return True if any rank appears four times.
     */
    private static boolean isFourOfAKind(Hand cards) {
        int[] rankCounts = new int[15]; // Index represents card rank
        for (Card card : cards.getHand()) {
            rankCounts[card.getCardWorth()]++;
            if (rankCounts[card.getCardWorth()] == 4) {
                cards.setFourOfAKind(card.getCardWorth()); // Set the four-of-a-kind rank
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the hand is a Full House.
     * Full House = Three of a Kind + Pair
     * @param cards The hand to evaluate.
     * @return True if both three-of-a-kind and a pair exist.
     */
    private static boolean isFullHouse(Hand cards) {
        return isThreeOfAKind(cards) && isTwoPair(cards);
    }

    /**
     * Checks if all cards are of the same suit.
     * @param cards The hand to evaluate.
     * @return True if all cards share the same suit.
     */
    private static boolean isFlush(Hand cards) {
        int[] suitCounts = new int[4]; // Index 0=clubs, 1=diamonds, 2=hearts, 3=spades
        for (Card card : cards.getHand()) {
            switch (card.getCardSuit()) {
                case 'c': suitCounts[0]++; break;
                case 'd': suitCounts[1]++; break;
                case 'h': suitCounts[2]++; break;
                case 's': suitCounts[3]++; break;
            }
        }

        // Return true if any suit appears exactly 5 times
        for (int val : suitCounts) {
            if (val == 5) return true;
        }
        return false;
    }

    /**
     * Checks if the hand forms a consecutive sequence of card ranks.
     * Assumes the cards are already sorted.
     * @param cards The hand to evaluate.
     * @return True if ranks form a consecutive sequence.
     */
    private static boolean isStraight(Hand cards) {
        for (int i = 0; i < cards.size() - 1; i++) {
            int diff = cards.getHand().get(i + 1).getCardWorth() - cards.getHand().get(i).getCardWorth();
            if (diff != 1) {
                return false;
            }
        }

        // Set the highest card in the straight
        cards.setHighCard(cards.getHand().get(cards.size() - 1).getCardWorth());
        return true;
    }

    /**
     * Checks if the hand contains Three of a Kind.
     * @param cards The hand to evaluate.
     * @return True if any rank appears three times.
     */
    private static boolean isThreeOfAKind(Hand cards) {
        int[] rankCounts = new int[15];
        for (Card card : cards.getHand()) {
            rankCounts[card.getCardWorth()]++;
            if (rankCounts[card.getCardWorth()] == 3) {
                cards.setThreeOfAKind(card.getCardWorth());
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the hand contains Two Pair.
     * @param cards The hand to evaluate.
     * @return True if exactly two pairs of cards with the same rank exist.
     */
    private static boolean isTwoPair(Hand cards) {
        int pairs = 0;
        int truePairs = 0;
        int[] rankCounts = new int[15];

        for (Card card : cards.getHand()) {
            rankCounts[card.getCardWorth()]++;
            if (rankCounts[card.getCardWorth()] == 2) {
                pairs++;
            }
        }

        // Track the two highest pairs
        for (int i = 2; i < rankCounts.length; i++) {
            if (rankCounts[i] == 2) {
                truePairs++;
                // Update secondPair and highPair
                cards.setSecondPair(cards.getHighPair());
                cards.setHighPair(i);
            }
            if (truePairs == 2) {
                return true;
            }
        }

        return pairs == 2;
    }

    /**
     * Checks if the hand contains a single Pair.
     * @param cards The hand to evaluate.
     * @return True if one rank appears twice.
     */
    private static boolean isPair(Hand cards) {
        int[] rankCounts = new int[15];
        for (Card card : cards.getHand()) {
            rankCounts[card.getCardWorth()]++;
            if (rankCounts[card.getCardWorth()] == 2) {
                cards.setHighPair(card.getCardWorth()); // Capture rank of the pair
                return true;
            }
        }
        return false;
    }
}
