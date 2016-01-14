package me.apemanzilla.krist.turbokrist.miners;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;

import me.apemanzilla.krist.turbokrist.MinerOptions;

/**
 * Used to mine Krist with graphics processing hardware at an accelerated rate.
 * This class should not be initialized directly, but instead through
 * {@link me.apemanzilla.krist.turbokrist.miners.MinerFactory MinerFactory}
 * 
 * @see Miner
 * @author apemanzilla
 *
 */
public final class GPUMiner extends Miner {

	private final CLContext context;
	private final CLQueue queue;
	private final CLKernel kernel;

	/**
	 * Creates a GPUMiner object. This constructor should not be used - you
	 * should instead use
	 * {@link me.apemanzilla.krist.turbokrist.miners.MinerFactor MinerFactory}.
	 * 
	 * @param dev
	 * @param options
	 * @throws MinerInitException
	 */
	GPUMiner(CLDevice dev, MinerOptions options) throws MinerInitException {
		this.context = dev.getPlatform().createContext(null, new CLDevice[] { dev });
		this.queue = context.createDefaultQueue();
		String code = OpenCLLoader.load(options);
		if (code == null) {
			throw new MinerInitException("Failed to load OpenCL code.");
		}
		CLProgram program = context.createProgram(code);
		this.kernel = program.createKernel("krist_miner_basic");
	}

	@Override
	protected void startMining(String block, int work) {
		// TODO Auto-generated method stub

	}

	@Override
	protected void stopMining() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean isMining() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean hasSolution() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Solution getSolution() {
		// TODO Auto-generated method stub
		return null;
	}

}
