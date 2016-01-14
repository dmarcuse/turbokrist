package me.apemanzilla.krist.turbokrist.miners;

/**
 * Thrown when there is a problem creating a
 * {@link me.apemanzilla.krist.turbokrist.miners.Miner Miner} object.
 * 
 * @author apemanzilla
 *
 */
public class MinerInitException extends Exception {

	private static final long serialVersionUID = -8068156271735087933L;

	public MinerInitException() {
		super();
	}

	public MinerInitException(String s) {
		super(s);
	}
}
