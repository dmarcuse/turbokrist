package me.apemanzilla.krist.state;

import me.lignum.jkrist.Krist;
import me.lignum.jkrist.KristAPIException;

import java.util.ArrayList;
import java.util.List;

/**
 * Constantly keeps track of the block and work values for Krist. Use with
 * {@link me.apemanzilla.krist.state.NodeStateListener NodeStateListener} for
 * best results.
 * 
 * @author apemanzilla
 *
 */
public class NodeState {
	private static Krist krist = new Krist("http://kristtest.lemmmy.pw");

	public static Krist getKrist() {
		return krist;
	}

	public List<NodeStateListener> listeners = new ArrayList<NodeStateListener>();

	private Thread daemon;

	private String block;
	private long work;

	/**
	 * Creates a {@code NodeState} daemon, which will monitor for block and work
	 * value changes.
	 * 
	 * @param refreshRate How often to send requests.
	 */
	public NodeState(final int refreshRate) {
		daemon = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						String newBlock = NodeState.getKrist().getLastBlock().getShortHash();
						if (block == null || !block.equals(newBlock)) {
							// block has changed
							long newWork = NodeState.getKrist().getWork();
							if (work == 0 || newWork != work) {
								// work has changed
								work = newWork;
							}
							block = newBlock;
							notifyListeners();
						}
						Thread.sleep(refreshRate);
					} catch (InterruptedException e) {

					}
				}
			}

		});
		daemon.setDaemon(true);
	}

	/**
	 * Notifies all {@link me.apemanzilla.krist.state.NodeStateListener
	 * NodeStateListener}s that the state has changed.
	 */
	private void notifyListeners() {
		for (NodeStateListener l : listeners) {
			l.stateChanged(block, work);
		}
	}

	/**
	 * Starts the monitor.
	 */
	public void start() {
		daemon.start();
	}

	/**
	 * Gets the current block.
	 * 
	 * @return The block as a 12-character String.
	 */
	public String getBlock() {
		return block;
	}

	/**
	 * Gets the current work.
	 * 
	 * @return The work value as a long.
	 */
	public long getWork() {
		return work;
	}

	/**
	 * Adds a {@link me.apemanzilla.krist.state.NodeStateListener
	 * NodeStateListener}. {@link
	 * me.apemanzilla.krist.state.NodeStateListener#stateChanged(String, long)
	 * NodeStateListener.stateChanged} will be invoked whenever the block or
	 * work changes.
	 * 
	 * @param nsl An object that implements {@link
	 * me.apemanzilla.krist.state.NodeStateListener NodeStateListener}
	 */
	public void addListener(NodeStateListener nsl) {
		listeners.add(nsl);
	}

}
