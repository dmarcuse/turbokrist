package me.apemanzilla.krist.turbokrist.miners;

/**
 * Represents a class that can 'listen' to a {@code Miner}, being notified when
 * blocks are solved.
 * 
 * @author apemanzilla
 *
 * @see Miner
 * 
 * @see MinerListener#blockSolved(Solution)
 */
public interface MinerListener {

	/**
	 * Called when a {@code Miner} object that is being listened to finds a
	 * solution.
	 * 
	 * @param sol The solution found by the miner.
	 */
	public void blockSolved(Solution sol);

}
