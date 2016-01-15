package me.apemanzilla.krist.turbokrist.miners;

import java.util.Observable;

/**
 * Represents an object that can be used to mine Krist. In addition to the
 * abstract methods included here, the {@link #hashes} variable should be
 * incremented as hashes are generated and checked.
 * 
 * @author apemanzilla
 *
 */
public abstract class Miner extends Observable {

	/**
	 * Represents the number of hashes completed by this miner. This variable
	 * should be incremented as mining is done, and it is automatically set back
	 * to 0 when mining is started.
	 */
	protected long hashes = 0;

	private long startTime = 0;

	private long prevHashes = 0;

	private long prevTime = 0;

	/**
	 * Internal method to be run before hash rate counter is started - should be
	 * used for any initialization.
	 */
	protected abstract void preMining(String block, int work);

	/**
	 * Internal method to start mining.
	 */
	protected abstract void startMining(String block, int work);

	/**
	 * Tells the miner to begin mining.
	 * 
	 * @param block
	 *            The previous block in the chain.
	 * @param work
	 *            The work value.
	 */
	public void start(String block, int work) {
		hashes = 0;
		startTime = 0;
		prevHashes = 0;
		prevTime = 0;
		preMining(block, work);
		startTime = System.currentTimeMillis();
		startMining(block, work);
	}

	/**
	 * Internal method to stop mining.
	 */
	protected abstract void stopMining();

	/**
	 * Stops a miner.
	 */
	public void stop() {
		stopMining();
	}

	/**
	 * Checks whether the miner is currently running
	 * 
	 * @return True if it is, false otherwise.
	 */
	public abstract boolean isMining();

	/**
	 * Checks if the miner has the solution to the block.
	 * 
	 * @return True if it does have the solution, false otherwise.
	 */
	public abstract boolean hasSolution();

	/**
	 * Gets the solution that the miner has produced.
	 * 
	 * @see Solution
	 * @return The solution produced by the miner.
	 */
	public abstract Solution getSolution();

	protected void notifyListeners() {
		setChanged();
		notifyObservers();
	}

	/**
	 * Gets the average hash rate since the miner was started.
	 * 
	 * @return The average hash rate of this miner, in hashes per second.
	 */
	public double getAverageHashrate() {
		if (hashes == 0 || startTime == 0)
			return 0;
		return (double) hashes / (double) ((System.currentTimeMillis() - startTime) - 1000);
	}

	/**
	 * Gets the average hash rate since this method was last called.
	 * 
	 * @return The average hash rate of this miner since this method was last
	 *         called, in hashes per second.
	 */
	public double getRecentHashrate() {
		if (hashes == 0 || startTime == 0)
			return 0;
		if (prevHashes == 0 || prevTime == 0) {
			prevHashes = hashes;
			prevTime = System.currentTimeMillis();
			return getAverageHashrate();
		} else {
			double n = (double) (hashes - prevHashes) / (double) ((System.currentTimeMillis() - prevTime) / 1000);
			prevHashes = hashes;
			prevTime = System.currentTimeMillis();
			return n;
		}
	}

	/**
	 * If applicable, destroys the {@code Miner}, releasing all held resources.
	 */
	public void destroy() {

	}
}
