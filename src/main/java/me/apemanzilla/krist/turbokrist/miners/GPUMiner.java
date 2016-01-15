package me.apemanzilla.krist.turbokrist.miners;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerController;
import me.apemanzilla.krist.turbokrist.MinerOptions;
import me.apemanzilla.krist.turbokrist.MinerUtils;
import me.apemanzilla.krist.turbokrist.opencl.ProgramBuildException;
import me.apemanzilla.krist.turbokrist.opencl.ProgramBuilder;

/**
 * Used to mine Krist with graphics processing hardware at an accelerated rate.
 * This class should not be initialized directly, but instead through
 * {@link me.apemanzilla.krist.turbokrist.miners.MinerFactory MinerFactory}
 * 
 * @see Miner
 * @author apemanzilla
 *
 */
public final class GPUMiner extends Miner implements Runnable {

	private final CLContext context;
	private final CLQueue queue;
	private final CLKernel kernel;

	private final CLBuffer<Byte> addressBuffer;
	private CLBuffer<Byte> blockBuffer;
	private CLBuffer<Byte> prefixBuffer;
	private CLBuffer<Byte> outputBuf;
	
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
		ProgramBuilder pb = new ProgramBuilder("sha256.cl", "krist_miner.cl");
		CLProgram program;
		try {
			program = pb.build(context);
		} catch (ProgramBuildException e) {
			e.printStackTrace();
			throw new MinerInitException("Failed to build OpenCL program");
		}
		this.kernel = program.createKernel("krist_miner_basic");
		Pointer<Byte> addressPtr = Pointer.allocateBytes(10);
		byte[] addressBytes = MinerUtils.getBytes(options.getKristAddress().getAddress());
		addressPtr.setArray(addressBytes);
		this.addressBuffer = context.createByteBuffer(Usage.Input, addressPtr);
	}

	@Override
	protected void preMining(String block, int work) {
		Pointer<Byte> blockPtr = Pointer.allocateBytes(12);
		Pointer<Byte> prefixPtr = Pointer.allocateBytes(2);
		blockPtr.setArray(MinerUtils.getBytes(block));
		prefixPtr.setArray(MinerUtils.getBytes(MinerFactory.generatePrefix()));
		blockBuffer = context.createByteBuffer(Usage.Input, blockPtr);
		prefixBuffer = context.createByteBuffer(Usage.Input, prefixPtr);
		outputBuf = context.createByteBuffer(Usage.Output, 34);
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

	@Override
	public void run() {
		// TODO Auto-generated method stub

	}

}
