package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Test;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerUtils;

public class TestCLMining extends OpenCLTest {

	@Test
	public void testMining_hashToLong() {
		String[] inputs = { "", "hello", "hi", "ADLGeag3" };
		CLKernel kernel = program.createKernel("testHashToLong");
		for (String input : inputs) {
			byte[] hashed = MinerUtils.digest(MinerUtils.getBytes(input));
			Pointer<Byte> inputPtr = Pointer.allocateBytes(hashed.length).order(context.getByteOrder());
			for (int i = 0; i < hashed.length; i++) {
				inputPtr.set(i, hashed[i]);
			}
			CLBuffer<Byte> inputBuf = context.createByteBuffer(Usage.Input, inputPtr);
			CLBuffer<Long> outputBuf = context.createLongBuffer(Usage.Output, 1);
			kernel.setArgs(inputBuf, outputBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] { 1 });
			Pointer<Long> outputPtr = outputBuf.read(queue, evt);
			long expect = MinerUtils.hashToLong(hashed);
			long got = outputPtr.get(0);
			assertEquals(expect, got);
		}
	}

}
