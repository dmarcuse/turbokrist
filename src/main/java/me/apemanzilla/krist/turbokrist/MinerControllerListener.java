package me.apemanzilla.krist.turbokrist;

import me.apemanzilla.krist.turbokrist.miners.Miner;
import me.apemanzilla.krist.turbokrist.miners.Solution;

/**
 * Allows an object implementing this to be notified when an attached
 * MinerController has a major change.
 * 
 * @author apemanzilla
 * 
 * @see MinerController#addListener(MinerControllerListener)
 * MinerController.addListener
 *
 */
public interface MinerControllerListener {

	/**
	 * Called when a Miner is successfully created @param m The Miner that was
	 * created
	 */
	public void minerCreated(Miner m);

	/**
	 * Called when a block is being submitted
	 */
	public void submittingBlock(Solution s);

	/**
	 * Called when a block change is detected
	 */
	public void blockChanged(String newBlock);

	/**
	 * Called when an error occurs
	 */
	public void errorOccurred(Exception e);

}
