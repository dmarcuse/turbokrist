package me.apemanzilla.krist.state;

import java.util.List;

import me.apemanzilla.kristapi.KristAPI;
import me.apemanzilla.kristapi.exceptions.RemoteErrorException;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;

/**
 * Constantly keeps track of the block and work values for Krist. Use with
 * {@link me.apemanzilla.krist.state.NodeStateListener NodeStateListener} for
 * best results.
 * 
 * @author apemanzilla
 *
 */
public class NodeState {

	public List<NodeStateListener> listeners;

	private Thread daemon;

	private String block;
	private Object block_lock = new Object();
	private long work;
	private Object work_lock = new Object();

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
						String newBlock = KristAPI.getBlock();
						if (block == null || !block.equals(block)) {
							// block has changed
							long newWork = KristAPI.getWork();
							if (work == 0 || newWork != work) {
								// work has changed
								synchronized (work_lock) {
									work = newWork;
								}
							}
							synchronized (block_lock) {
								block = newBlock;
							}
							notifyListeners();
							Thread.sleep(refreshRate);
						}
					} catch (SyncnodeDownException e) {

					} catch (RemoteErrorException e) {

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
		notifyAll();
		synchronized (block_lock) {
			synchronized (work_lock) {
				for (NodeStateListener l : listeners) {
					l.stateChanged(block, work);
				}
			}
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
		synchronized (block_lock) {
			return block;
		}
	}

	/**
	 * Gets the current work.
	 * 
	 * @return The work value as a long.
	 */
	public long getWork() {
		synchronized (work_lock) {
			return work;
		}
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
