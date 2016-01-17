package me.apemanzilla.krist.turbokrist.miners;

import java.util.Arrays;

import org.bridj.Pointer;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerOptions;
import me.apemanzilla.krist.turbokrist.MinerUtils;
import me.apemanzilla.krist.turbokrist.opencl.ProgramBuildException;
import me.apemanzilla.krist.turbokrist.opencl.ProgramBuilder;

/**
 * Used to mine Krist with graphics processing hardware at an accelerated rate.
 * This class should not be initialized directly, but instead through {@link
 * me.apemanzilla.krist.turbokrist.miners.MinerFactory MinerFactory}
 * 
 * @see Miner @author apemanzilla
 *
 */
public final class GPUMiner extends Miner implements Runnable {

	private final String deviceName;

	private final CLContext context;
	private final CLQueue queue;
	private final CLKernel kernel;

	private final int[] workSize;

	private final CLBuffer<Byte> addressBuffer;
	private CLBuffer<Byte> blockBuffer;
	private CLBuffer<Byte> prefixBuffer;
	private CLBuffer<Byte> outputBuffer;

	private long work;

	private Thread manager;
	private boolean run = false;

	private boolean destroyed = false;

	/**
	 * Creates a GPUMiner object. This constructor should not be used - you
	 * should instead use {@link
	 * me.apemanzilla.krist.turbokrist.miners.MinerFactor MinerFactory}.
	 * 
	 * @param dev @param options @throws MinerInitException
	 */
	GPUMiner(CLDevice dev, MinerOptions options) throws MinerInitException {
		this.deviceName = dev.getName().trim();
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
		Pointer<Byte> addressPtr = Pointer.allocateBytes(10).order(context.getByteOrder());
		byte[] addressBytes = MinerUtils.getBytes(options.getKristAddress().getAddress());
		addressPtr.setArray(addressBytes);
		this.addressBuffer = context.createByteBuffer(Usage.Input, addressPtr);
		this.workSize = new int[] { options.getWorkSize(MinerFactory.generateSignature(dev)) };
	}

	@Override
	public String getName() {
		return "GPU Miner on " + deviceName;
	}

	@Override
	protected void preMining(String block, int work) {
		Pointer<Byte> blockPtr = Pointer.allocateBytes(12).order(context.getByteOrder());
		Pointer<Byte> prefixPtr = Pointer.allocateBytes(2).order(context.getByteOrder());
		blockPtr.setArray(MinerUtils.getBytes(block));
		prefixPtr.setArray(MinerUtils.getBytes(MinerFactory.generatePrefix()));
		blockBuffer = context.createByteBuffer(Usage.Input, blockPtr);
		prefixBuffer = context.createByteBuffer(Usage.Input, prefixPtr);
		outputBuffer = context.createByteBuffer(Usage.Output, 34);
		this.work = work;
	}

	@Override
	protected void startMining(String block, int work) {
		if (!destroyed) {
			manager = new Thread(this);
			run = true;
			manager.start();
		}
	}

	@Override
	protected void stopMining() {
		if (!destroyed) {
			run = false;
			if (manager != null) {
				manager.interrupt();
			}
		}
	}

	@Override
	public boolean isMining() {
		return (!destroyed && manager != null && manager.isAlive());
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
	public void destroy() {
		stop();
		destroyed = true;
		context.release();
		queue.release();
		kernel.release();
		addressBuffer.release();
		blockBuffer.release();
		prefixBuffer.release();
		outputBuffer.release();
	}

	@Override
	public void run() {
		long base = 0;
		kernel.setArgs(addressBuffer, blockBuffer, prefixBuffer, base, work, outputBuffer);
		while (run) {
			CLEvent mine = kernel.enqueueNDRange(queue, workSize);
			Pointer<Byte> outputPtr = outputBuffer.read(queue, mine);
			mine.release();
			hashes += workSize[0];
			base += workSize[0];
			if (outputPtr.getByteAtIndex(0) != 0 && run) {
				System.out.println("Solved");
				char[] sol = MinerUtils.getChars(outputPtr.getBytes());
				Solution s = new Solution(new String(Arrays.copyOfRange(sol, 0, 10)),
						new String(Arrays.copyOfRange(sol, 10, 22)), new String(Arrays.copyOfRange(sol, 22, 34)));
				solved(s);
				break;
			}
			kernel.setArg(3, base);
		}
	}
}
