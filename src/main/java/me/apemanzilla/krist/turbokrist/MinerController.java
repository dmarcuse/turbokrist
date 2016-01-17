package me.apemanzilla.krist.turbokrist;

import java.util.ArrayList;
import java.util.List;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.krist.state.NodeStateListener;
import me.apemanzilla.krist.turbokrist.miners.Miner;
import me.apemanzilla.krist.turbokrist.miners.MinerFactory;
import me.apemanzilla.krist.turbokrist.miners.MinerInitException;
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

	private List<Miner> miners = new ArrayList<Miner>();

	private State state = State.NOT_READY;

	/**
	 * Creates a new MinerController @param options Options to use
	 */
	public MinerController(MinerOptions options) {
		this.options = options;
	}

	/**
	 * Creates all Miner objects based on options given upon construction
	 */
	public void createMiners() throws TurbokristFatalException {
		List<CLDevice> devices = options.getMiningDevices();
		for (CLDevice dev : devices) {
			Miner m;
			try {
				m = MinerFactory.createMiner(dev, options);
				miners.add(m);
				for (MinerControllerListener mcl : listeners) {
					mcl.minerCreated(m);
				}
			} catch (MinerInitException e) {
				for (MinerControllerListener mcl : listeners) {
					mcl.errorOccurred(e);
				}
			}
		}
		if (miners.size() > 0) {
			setState(State.READY);
		} else {
			TurbokristFatalException e = new TurbokristFatalException("Could not create any miners!");
			for (MinerControllerListener mcl : listeners) {
				mcl.errorOccurred(e);
			}
			throw e;
		}
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
		 * The MinerController is not running but is ready to run
		 */
		READY,
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
