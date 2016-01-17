package me.apemanzilla.krist.turbokrist;

import java.util.ArrayList;
import java.util.List;

import me.apemanzilla.krist.state.NodeStateListener;
import me.apemanzilla.krist.turbokrist.miners.MinerListener;
import me.apemanzilla.krist.turbokrist.miners.Solution;

/**
 * Wraps and controls multiple {@link
 * me.apemanzilla.krist.turbokrist.miners.Miner Miner} objects, handling logic
 * for block changes and submission, and other important features.
 * 
 * @author apemanzilla
 *
 */
public class MinerController implements Runnable, MinerListener, NodeStateListener {

	private final MinerOptions options;

	private List<MinerControllerListener> listeners = new ArrayList<MinerControllerListener>();

	private State state = State.NOT_READY;

	public MinerController(MinerOptions options) {
		this.options = options;
	}

	@Override
	public void run() {

	}

	public void addListener(MinerControllerListener mcl) {
		listeners.add(mcl);
	}

	@Override
	public void blockSolved(Solution sol) {

	}

	@Override
	public void stateChanged(String newBlock, long newWork) {

	}

	private void setState(State state) {
		this.state = state;
	}

	public State getState() {
		return state;
	}

	public enum State {
		/**
		 * MinerController object has been created, but Miners have not been
		 * created yet.
		 */
		NOT_READY,
		/**
		 * The MinerController is not running
		 */
		NOT_RUNNING,
		/**
		 * Miners are being started
		 */
		STARTING,
		/**
		 * Miners are running
		 */
		RUNNING,
		/**
		 * Miners are stopping
		 */
		STOPPING,
		/**
		 * A block is being submitted
		 */
		SUBMITTING,
		/**
		 * An error has occurred
		 */
		ERROR
	}
}
