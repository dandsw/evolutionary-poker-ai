
/**
 * Represents a simple neural network used by a poker bot to decide actions.
 * The network takes game state inputs, processes them through two hidden layers, and outputs a poker action.
 * It supports 9 input features, 2 hidden layers (16 and 8 nodes), and 4 outputs (RAISE, CALL, FOLD, and RAISE AMOUNT).
 */
public class NeuralNetwork {

    // Enum for possible bot actions
    public enum Actions { RAISE, CALL, FOLD }

    private double[][] h1;      // Weights for first hidden layer (16 nodes)
    private double[][] h2;      // Weights for second hidden layer (8 nodes)
    private double[][] output;  // Weights for output layer (4 outputs: 3 for action, 1 for raise amount)
    private double[] b;         // Bias vector (16 for h1, 8 for h2, 3 for action, 1 for raise = 28 total)

    private GameEngineSimulator engine; // Reference to game environment to access scaling constants
    private int raise;                  // Stores calculated raise amount when action is RAISE

    /**
     * Constructs a neural network using a GameEngineSimulator context and genetic weights.
     * @param engine Reference to the game simulation environment.
     * @param genes  BotGenetics object containing initialized weights and biases.
     */
    public NeuralNetwork(GameEngineSimulator engine, BotGenetics genes) {
        this.h1 = genes.getH1Weights();
        this.h2 = genes.getH2Weights();
        this.b = genes.getBias();
        this.output = genes.getOutputWeights();
        this.engine = engine;
    }

    /**
     * Decides what action the bot should take based on current game state inputs.
     * @return The selected poker action: RAISE, CALL, or FOLD
     */
    public Actions takeTurn(int chips, float handStrength, int gameStage, int pot, int round,
                            int numberFolded, int numRaisesThisRound, int numCallsThisRound) {
        double[] inputs = new double[9];

        
        inputs[0] = chips / engine.getStartingChips();                         // Chip ratio
        inputs[1] = handStrength;                                              // Hand strength [0, 1]
        inputs[2] = gameStage / 3.0;                                           // Preflop, flop, turn, river
        inputs[3] = pot / engine.getMaxPot();                                  // Pot size ratio
        inputs[4] = round / engine.getMaxRound();                              // Round progression
        inputs[5] = numberFolded / engine.getTotalBots();                      // Fold rate
        inputs[6] = numRaisesThisRound / engine.getTotalBots();               // Raise activity
        inputs[7] = numCallsThisRound / engine.getTotalBots();                // Call activity
        inputs[8] = ((engine.getStartingChips() * engine.getTotalBots()) - chips) 
                    / engine.getTotalBots();                                   // Average opponent stack

        return hiddenLayer1(inputs); // Feed into first hidden layer
    }

    /**
     * Processes the first hidden layer with ReLU activation.
     * @param inputs Array of 9 input values
     * @return Output of first hidden layer (16 nodes)
     */
    private Actions hiddenLayer1(double[] inputs) {
        double[] hiddenLayerNodes = new double[16];

        for (int i = 0; i < h1.length; i++) {
            double nodeValue = 0.0;
            for (int j = 0; j < h1[i].length; j++) {
                nodeValue += h1[i][j] * inputs[j];
            }
            nodeValue += b[i];  // Add bias for node
            if (nodeValue < 0) nodeValue = 0; 
            hiddenLayerNodes[i] = nodeValue;
        }

        return hiddenLayer2(hiddenLayerNodes);
    }

    /**
     * Processes the second hidden layer 
     * @param inputs Output from first hidden layer
     * @return Output of second hidden layer (8 nodes)
     */
    private Actions hiddenLayer2(double[] inputs) {
        double[] hiddenLayerNodes = new double[8];

        for (int i = 0; i < h2.length; i++) {
            double nodeValue = 0.0;
            for (int j = 0; j < h2[i].length; j++) {
                nodeValue += h2[i][j] * inputs[j];
            }
            nodeValue += b[i + 16]; // Bias for second layer starts after first 16
            if (nodeValue < 0) nodeValue = 0;  
            hiddenLayerNodes[i] = nodeValue;
        }

        return outputLayer(hiddenLayerNodes);
    }

    /**
     * Final output layer that determines action and calculates raise amount if needed.
     * @param inputs Output from second hidden layer
     * @return Chosen poker action
     */
    private Actions outputLayer(double[] inputs) {
        Actions action = foldCallOrRaise(inputs); // Determine action
        if (action == Actions.RAISE) {
            raise = raiseAmount(inputs); // Calculate raise only if needed
        }
        return action;
    }

    /**
     * Calculates scores for RAISE, CALL, and FOLD, applies softmax, and selects the highest.
     * @param inputs Output from hidden layer 2
     * @return Chosen action
     */
    private Actions foldCallOrRaise(double[] inputs) {
        double[] results = new double[3]; // One score for each action

        for (int i = 0; i < 3; i++) {
            double nodeValue = 0.0;
            for (int j = 0; j < output[i].length; j++) {
                nodeValue += output[i][j] * inputs[j];
            }
            nodeValue += b[i + 24]; // Bias for output layer starts after 24 values
            results[i] = nodeValue;
        }

        return softmax(results);
    }

    /**
     * Applies softmax to the output values and returns the action with the highest probability.
     * @param r Raw output values from foldCallOrRaise
     * @return Most likely action
     */
    private Actions softmax(double[] r) {
        double sum = 0;
        double[] values = new double[3];
        for (int i = 0; i < r.length; i++) {
            values[i] = Math.exp(r[i]);
            sum += values[i];
        }

        int index = 0;
        double maxVal = -Double.MAX_VALUE;

        // Normalize and select max
        for (int i = 0; i < values.length; i++) {
            values[i] /= sum;
            if (values[i] > maxVal) {
                maxVal = values[i];
                index = i;
            }
        }

        // Map index to action
        switch (index) {
            case 0: return Actions.RAISE;
            case 1: return Actions.CALL;
            default: return Actions.FOLD;
        }
    }

    /**
     * Calculates the amount to raise based on the fourth output neuron.
     * Applies ReLU to ensure non-negative result.
     * @param inputs Output from hidden layer 2
     * @return Raise amount (integer)
     */
    private int raiseAmount(double[] inputs) {
        double raiseAmount = 0;
        for (int i = 0; i < output[3].length; i++) {
            raiseAmount += output[3][i] * inputs[i];
        }
        raiseAmount += b[27]; // Final bias for raise neuron

        return (int) Math.max(0, raiseAmount); 
    }

    /**
     * Returns the raise amount calculated during the last decision.
     * Only meaningful if the last action was RAISE.
     * @return Integer raise amount
     */
    public int getRaise() {
        return raise;
    }
}
