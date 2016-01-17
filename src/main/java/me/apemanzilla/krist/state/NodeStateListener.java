package me.apemanzilla.krist.state;

/**
 * 
 * @author apemanzilla @see NodeState
 */
public interface NodeStateListener {

	/**
	 * Called whenever the block or work changes.
	 * 
	 * @param newBlock The new block as a 12-character String @param newWork The
	 * new work as a long
	 */
	public void stateChanged(String newBlock, long newWork);

}
