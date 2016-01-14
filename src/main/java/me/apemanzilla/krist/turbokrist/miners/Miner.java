package me.apemanzilla.krist.turbokrist.miners;

import java.util.Observable;

public abstract class Miner extends Observable {

	// used for getting hash rate
	protected long hashes = 0;
	protected long startTime = 0;
	
	// used for getting recent hash rate
	private long prevHashes = 0;
	private long prevTime = 0;
	
	protected abstract void startMining(String block, int work);
	
	/**
	 * Tells the miner to begin mining.
	 * @param block The previous block in the chain.
	 * @param work The work value.
	 */
	public void start(String block, int work) {
		hashes = 0;
		startTime = System.currentTimeMillis();
		prevHashes = 0;
		prevTime = 0;
		startMining(block, work);
	}
	
	protected abstract void stopMining();
	
	/**
	 * Stops a miner.
	 */
	public void stop() {
		stopMining();
	}
	
	/**
	 * Checks whether the miner is currently running
	 * @return True if it is, false otherwise.
	 */
	public abstract boolean isMining();
	
	/**
	 * Checks if the miner has the solution to the block.
	 * @return True if it is, false otherwise.
	 */
	public abstract boolean hasSolution();
	
	/**
	 * Gets the solution that the miner has produced.
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
	 * @return The average hash rate of this miner, in hashes per second.
	 */
	public double getAverageHashrate() {
		if (hashes == 0 || startTime == 0) return 0;
		return (double) hashes / (double) ((System.currentTimeMillis() - startTime) - 1000);
	}
	
	/**
	 * Gets the average hash rate since this method was last called.
	 * @return The average hash rate of this miner since this method was last called, in hashes per second.
	 */
	public double getRecentHashrate() {
		if (hashes == 0 || startTime == 0) return 0;
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
}
