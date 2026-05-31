
import java.util.*;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Manages the genetic algorithm used to evolve PokerBots across multiple generations.
 * Handles population initialization, fitness evaluation, selection, mutation, crossover, and file logging.
 */
public class BotSelector {

    int botsPerGen;                          // Number of bots per generation
    private int currGeneration = 0;          // Index of the current generation
    private int totalGenerations;            // Total number of generations to simulate
    private BotGenetics bestOfLastGen = null;// Stores the best-performing bot of the last generation

    /**
     * Constructs the BotSelector and begins the evolutionary simulation.
     * @param generations Number of generations to simulate
     * @param botsPerGen  Number of bots per generation
     * @param blind       Big blind amount
     * @param turns       Number of rounds per simulation
     * @param startingChips Number of chips each bot starts with
     */
    public BotSelector(int generations, int botsPerGen, int blind, int turns, int startingChips) {
        this.totalGenerations = generations;
        this.botsPerGen = botsPerGen;

        BotGenetics[] genes = generateGenetics(); // Create initial random population

        for (int i = 0; i < generations; i++) {
            currGeneration = i;
            System.out.println("Generation: " + i);
            genes = runSimulation(genes, blind, turns, startingChips);
            System.out.println();
        }

        // Save the best-performing bot's weights to a file
        if (bestOfLastGen != null) {
            try (FileWriter writer = new FileWriter("best_bot_weights.txt")) {
                writer.write("=== Best Bot of Final Generation ===\n\n");
                writer.write("Hidden Layer 1 Weights:\n" + Arrays.deepToString(bestOfLastGen.getH1Weights()) + "\n\n");
                writer.write("Hidden Layer 2 Weights:\n" + Arrays.deepToString(bestOfLastGen.getH2Weights()) + "\n\n");
                writer.write("Output Weights:\n" + Arrays.deepToString(bestOfLastGen.getOutputWeights()) + "\n\n");
                writer.write("Biases:\n" + Arrays.toString(bestOfLastGen.getBias()) + "\n");
            } catch (IOException e) {
                System.err.println("Error writing best bot weights: " + e.getMessage());
            }
        }
    }

    /**
     * Runs simulations for a generation of bots and returns a new generation of evolved genetics.
     */
    private BotGenetics[] runSimulation(BotGenetics[] genetics, int blind, int turns, int startingChips) {
        Random rand = new Random();
        PriorityQueue<PokerBot> generation = new PriorityQueue<>(Collections.reverseOrder());

        PokerBot[] bots = new PokerBot[botsPerGen];
        for (int i = 0; i < bots.length; i++) {
            bots[i] = new PokerBot(genetics[i], "Bot " + i);
        }

        // Simulate multiple games with random 5-bot matchups
        for (int numGames = 0; numGames < (botsPerGen / 5) * 10; numGames++) {
            GameEngineSimulator engine = new GameEngineSimulator(blind, turns, startingChips);

            Set<Integer> chosen = new HashSet<>();
            while (chosen.size() < 5) {
                chosen.add(rand.nextInt(botsPerGen)); // Randomly choose 5 unique bots
            }

            for (int botNum : chosen) {
                bots[botNum].reset(startingChips);
                bots[botNum].setEngine(engine);
                engine.addBots(bots[botNum]);
            }

            engine.simulateGame(); // Play one full game
        }

        generation.addAll(Arrays.asList(bots)); // Add all bots to a priority queue for fitness ranking
        return selectBestPerformingBots(generation);
    }

    /**
     * Generates the initial population of bot genetics with random weights and biases.
     */
    private BotGenetics[] generateGenetics() {
        Random rand = new Random();
        BotGenetics[] genetics = new BotGenetics[botsPerGen];

        for (int i = 0; i < botsPerGen; i++) {
            double[][] h1 = new double[16][9];
            double[][] h2 = new double[8][16];
            double[][] output = new double[4][8];
            double[] bias = new double[28];

            // Randomize weights between -0.8 and 0.8
            for (double[] row : h1)
                for (int j = 0; j < row.length; j++)
                    row[j] = rand.nextDouble() * 1.6 - 0.8;

            for (double[] row : h2)
                for (int j = 0; j < row.length; j++)
                    row[j] = rand.nextDouble() * 1.6 - 0.8;

            for (double[] row : output)
                for (int j = 0; j < row.length; j++)
                    row[j] = rand.nextDouble() * 1.6 - 0.8;

            for (int j = 0; j < bias.length; j++)
                bias[j] = rand.nextDouble() * 0.4 - 0.2;

            genetics[i] = new BotGenetics(h1, h2, output, bias);
        }

        return genetics;
    }

    /**
     * Computes the average fitness of a population.
     */
    public double getAverageFitness(PriorityQueue<PokerBot> queue) {
        double totalFitness = 0;
        int count = 0;

        for (PokerBot bot : queue) {
            totalFitness += bot.getFitness();
            count++;
        }

        return count == 0 ? 0 : totalFitness / count;
    }

    /**
     * Selects the top-performing bots, applies mutation, and generates offspring for the next generation.
     */
    private BotGenetics[] selectBestPerformingBots(PriorityQueue<PokerBot> gen) {
        System.out.println("Generations average fitness: " + getAverageFitness(gen));

        ArrayList<BotGenetics> nextGen = new ArrayList<>();

        if (currGeneration == totalGenerations-1) {
            bestOfLastGen = gen.remove().getGenes(); // Save best of last gen for export
        }

        // Top 10% copied directly, next 30% mutated, rest are offspring
        for (int i = 0; i < (int)(botsPerGen * 0.4); i++) {
            if (i == 0) {
                PokerBot b = gen.remove();
                System.out.println(b.getName() + " had the best fitness Score: " + b.getFitness());
                System.out.printf("Percent of the time raised: %.2f%%\n", b.getRaiseRate() * 100);
                System.out.printf("Percent of the time called/checked: %.2f%%\n", b.getCallRate() * 100);
                System.out.printf("Percent of the time folded: %.2f%%\n", b.getFoldRate() * 100);
                System.out.println("Total hands won: " + b.getHandsWon());

                nextGen.add(b.getGenes());
            } else if (i < (int)(botsPerGen * 0.1)) {
                nextGen.add(gen.remove().getGenes());
            } else {
                BotGenetics b = gen.remove().getGenes();
                b.mutateGenes(currGeneration); // Mutate to introduce variation
                nextGen.add(b);
            }
        }

        // Fill remaining population via crossover (offspring creation)
        while (nextGen.size() < botsPerGen) {
            Random rand = new Random();
            int parent1Index = rand.nextInt(nextGen.size());
            int parent2Index = rand.nextInt(nextGen.size());
            nextGen.add(createOffspring(nextGen.get(parent1Index), nextGen.get(parent2Index)));
        }

        return nextGen.toArray(new BotGenetics[0]);
    }

    /**
     * Combines two parent genetics into a new BotGenetics object via uniform crossover.
     */
    private BotGenetics createOffspring(BotGenetics p1, BotGenetics p2) {
        Random rand = new Random();

        double[][] ChildH1Weights = new double[16][9];
        double[][] ChildH2Weights = new double[8][16];
        double[][] ChildOutputWeights = new double[4][8];
        double[] ChildBiasWeights = new double[28];

        // Uniform crossover for each weight matrix
        for (int i = 0; i < 16; i++)
            for (int j = 0; j < 9; j++)
                ChildH1Weights[i][j] = rand.nextFloat() < 0.5 ? p1.getH1Weights()[i][j] : p2.getH1Weights()[i][j];

        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 16; j++)
                ChildH2Weights[i][j] = rand.nextFloat() < 0.5 ? p1.getH2Weights()[i][j] : p2.getH2Weights()[i][j];

        for (int i = 0; i < 4; i++)
            for (int j = 0; j < 8; j++)
                ChildOutputWeights[i][j] = rand.nextFloat() < 0.5 ? p1.getOutputWeights()[i][j] : p2.getOutputWeights()[i][j];

        for (int i = 0; i < 28; i++)
            ChildBiasWeights[i] = rand.nextFloat() < 0.5 ? p1.getBias()[i] : p2.getBias()[i];

        return new BotGenetics(ChildH1Weights, ChildH2Weights, ChildOutputWeights, ChildBiasWeights);
    }
}
