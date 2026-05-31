
import java.util.Random;

/**
 * Represents the genetic encoding of a poker bot's neural network weights and biases.
 * Provides methods to retrieve network layers and apply mutations to simulate evolution.
 */
public class BotGenetics {

    private double[][] h1Weights;     // Weights for the first hidden layer of the neural network
    private double[][] h2Weights;     // Weights for the second hidden layer
    private double[][] outputWeights; // Weights for the output layer
    private double[] bias;            // Bias values applied across network layers

    /**
     * Constructs a BotGenetics object with the specified weights and biases.
     *
     * @param h1 Weights for the first hidden layer
     * @param h2 Weights for the second hidden layer
     * @param output Weights for the output layer
     * @param bias Bias vector
     */
    public BotGenetics(double[][] h1, double[][] h2, double[][] output, double[] bias){
        this.h1Weights = h1;
        this.h2Weights = h2;
        this.bias = bias;
        this.outputWeights = output;
    }

    // ===== Getter Methods =====

    public double[][] getH1Weights(){
        return h1Weights;
    }

    public double[][] getH2Weights(){
        return h2Weights;
    }

    public double[][] getOutputWeights(){
        return outputWeights;
    }

    public double[] getBias(){
        return bias;
    }

    /**
     * Applies random mutations to the neural network weights and biases based on generation.
     * Mutation rate decreases as the generation number increases to encourage convergence.
     *
     * @param generation The current generation number of evolution
     */
    public void mutateGenes(int generation){
        double mutationRate;

        // Mutation rate decreases as generations progress
        if (generation < 500) mutationRate = 0.08;
        else if (generation < 1000) mutationRate = 0.05;
        else if (generation < 1500) mutationRate = 0.03;
        else mutationRate = 0.01;

        Random rand = new Random();

        // Mutate weights of first hidden layer
        for (int i = 0; i < h1Weights.length; i++){
            for (int j = 0; j < h1Weights[i].length; j++){
                if (rand.nextDouble() <= mutationRate){
                    // Randomly increment or decrement the weight slightly
                    h1Weights[i][j] += rand.nextDouble() > 0.5 ? 0.1 : -0.1;
                }
            }
        }

        // Mutate weights of second hidden layer
        for (int i = 0; i < h2Weights.length; i++){
            for (int j = 0; j < h2Weights[i].length; j++){
                if (rand.nextDouble() <= mutationRate){
                    h2Weights[i][j] += rand.nextDouble() > 0.5 ? 0.1 : -0.1;
                }
            }
        }

        // Mutate weights of output layer
        for (int i = 0; i < outputWeights.length; i++){
            for (int j = 0; j < outputWeights[i].length; j++){
                if (rand.nextDouble() <= mutationRate){
                    outputWeights[i][j] += rand.nextDouble() > 0.5 ? 0.1 : -0.1;
                }
            }
        }

        // Mutate bias values
        for (int i = 0; i < bias.length; i++){
            if (rand.nextDouble() <= mutationRate){
                bias[i] += rand.nextDouble() > 0.5 ? 0.1 : -0.1;
            }
        }
    }
}
