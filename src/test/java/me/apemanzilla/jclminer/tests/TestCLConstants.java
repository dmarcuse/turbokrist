package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Test;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerUtils;

public class TestCLConstants extends OpenCLTest {

	@Test
	public void testConstant_K() {
		int[] K = MinerUtils.K;
		CLBuffer<Integer> outputBuf = context.createIntBuffer(Usage.Output, K.length);
		CLKernel kernel = program.createKernel("testK", outputBuf);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] { K.length });
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < K.length; i++) {
			int got = outputPtr.getIntAtIndex(i), expected = K[i];
			assertEquals(String.format("Got %d, expected %d for item %d", got, expected, i), expected, got);
		}
	}

}
