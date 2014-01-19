package com.github.neuralnetworks.calculation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.neuralnetworks.architecture.Connections;
import com.github.neuralnetworks.architecture.Layer;
import com.github.neuralnetworks.architecture.Matrix;
import com.github.neuralnetworks.architecture.NeuralNetwork;
import com.github.neuralnetworks.util.UniqueList;
import com.github.neuralnetworks.util.Util;

/**
 * Default Implementation of the LayerCalculator interface
 * It takes advantage of the fact that the neural network is a graph with layers as nodes and connections between layers as links of the graph
 * The results are propagated within the graph
 */
public class LayerCalculatorImpl extends LayerCalculatorBase implements LayerCalculator, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public void calculate(NeuralNetwork neuralNetwork, Layer layer, Set<Layer> calculatedLayers, Map<Layer, Matrix> results) {
	List<ConnectionCalculateCandidate> ccc = new ArrayList<ConnectionCalculateCandidate>();
	orderConnections(neuralNetwork, layer, calculatedLayers, new UniqueList<Layer>(), ccc);
	calculate(results, ccc);
    }

    /**
     * Calculates single layer based on the network graph
     * 
     * For example the feedforward part of the backpropagation algorithm the initial parameters would be:
     * "currentLayer" will be the output layer of the network
     * "results" will contain only one entry for the input layer of the network - this is the training example
     * "calculatedLayers" will contain only one entry - the input layer
     * "inProgressLayers" will be empty
     * 
     * In the backpropagation part the initial parameters would be:
     * "currentLayer" will be the input layer of the network
     * "results" will contain only one entry for the output layer of the network - this is the calculated error derivative between the result of the network and the target value
     * "calculatedLayers" will contain only one entry - the output layer
     * "inProgressLayers" will be empty
     * 
     * This allows for single code to be used for the whole backpropagation, but also for RBMs, autoencoders, etc
     * 
     * @param calculatedLayers - layers that are fully calculated - the results for these layers can be used for calculating other parts of the network
     * @param inProgressLayers - layers which are currently calculated, but are not yet finished - not all connections to the layer are calculated and the result of the propagation through this layer cannot be used for another calculations
     * @param calculateCandidates - order of calculation
     * @param currentLayer - the layer which is currently being calculated.
     * @param neuralNetwork - the neural network.
     * @return
     */
    protected boolean orderConnections(NeuralNetwork neuralNetwork, Layer currentLayer, Set<Layer> calculatedLayers, Set<Layer> inProgressLayers, List<ConnectionCalculateCandidate> calculateCandidates) {
	boolean result = false;

	if (calculatedLayers.contains(currentLayer)) {
	    result = true;
	} else if (!inProgressLayers.contains(currentLayer)) {
	    inProgressLayers.add(currentLayer);
	    for (Connections c : currentLayer.getConnections(neuralNetwork)) {
		Layer opposite = Util.getOppositeLayer(c, currentLayer);
		if (orderConnections(neuralNetwork, opposite, calculatedLayers, inProgressLayers, calculateCandidates)) {
		    calculateCandidates.add(new ConnectionCalculateCandidate(c, currentLayer));
		}
	    }

	    result = true;

	    inProgressLayers.remove(currentLayer);
	    calculatedLayers.add(currentLayer);
	}

	return result;
    }
}
