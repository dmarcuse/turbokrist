package me.apemanzilla.krist.turbokrist;

import me.apemanzilla.krist.state.NodeStateListener;
import me.apemanzilla.krist.turbokrist.miners.MinerListener;
import me.apemanzilla.krist.turbokrist.miners.Solution;

/**
 * Wraps and controls multiple
 * {@link me.apemanzilla.krist.turbokrist.miners.Miner Miner} objects, handling
 * logic for block changes and submission, and other important features.
 * 
 * @author apemanzilla
 *
 */
public class MinerController implements MinerListener, NodeStateListener {

	@Override
	public void blockSolved(Solution sol) {

	}

	@Override
	public void stateChanged(String newBlock, long newWork) {

	}

}
