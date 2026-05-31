
/**
 * The main class for initializing and running the PokerBot simulation.
 * This class precomputes hand strengths and starts a genetic algorithm-based simulation
 * using the BotSelector class to evolve poker-playing bots.
 */
public class main {

    /**
     * Entry point of the PokerBot simulation.
     * It precomputes hand strengths and launches the evolutionary simulation.
     *
     * @param args command-line arguments (not used here)
     */
    public static void main(String[] args){
        
        // Notify user that hand strength mapping is starting
        System.out.println("Generating hand strength map...");
        
        // Generate precomputed hand strength values for all possible starting hands
        HandStrengthGenerator.generateAllPrecomputedStrengths();

        // Notify user that the simulation is starting
        System.out.println("Starting simulation...");
    
        // Instantiate a BotSelector to run the simulation
        // Arguments: 2000 generations, 500 bots per generation, blind = 6, 200 turns, 240 starting chips
        BotSelector test = new BotSelector(1000, 500, 6, 200, 240);
    }
}
