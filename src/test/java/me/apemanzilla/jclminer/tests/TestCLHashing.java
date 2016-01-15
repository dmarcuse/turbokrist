package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.Test;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerUtils;

public class TestCLHashing extends OpenCLTest {

	private static String bytesToHex(byte[] in) {
		String out = "";
		for (int i = 0; i < in.length; i++) {
			out += String.format("%x", in[i]);
		}
		return out;
	}

	/**
	 * Tests whether the SHA256 digest implementation is producing correct
	 * results - compares OpenCL results to Java results
	 */
	@Test
	public void testHashing_digest() {
		// create an input that's 55 characters long (max supported input
		// length)
		String longInput = "";
		for (int i = 0; i < 55; i++) {
			longInput += (char) i;
		}
		String inputs[] = { "", "hello", "hello world", "ThIIs ^Is_ a T3ZT", longInput };
		CLKernel kernel = program.createKernel("testDigest");
		for (int i = 0; i < inputs.length; i++) {
			String input = inputs[i];
			byte[] bytes = MinerUtils.getBytes(input);
			Pointer<Byte> inputPtr = Pointer.allocateBytes(input.length());
			for (int j = 0; j < input.length(); j++) {
				inputPtr.set(j, bytes[j]);
			}
			CLBuffer<Byte> inputBuf = input.length() > 0 ? context.createByteBuffer(Usage.Input, inputPtr)
					: context.createByteBuffer(Usage.Input, 1), outputBuf = context.createByteBuffer(Usage.Output, 32);
			kernel.setArgs(inputBuf, input.length(), outputBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] { 1 });
			Pointer<Byte> outputPtr = outputBuf.read(queue, evt);
			byte[] got = new byte[32];
			for (int j = 0; j < 32; j++) {
				got[j] = outputPtr.get(j);
			}
			byte[] expect = MinerUtils.digest(bytes);
			String gotHex = bytesToHex(got), expectHex = bytesToHex(expect);
			if (!gotHex.equals(expectHex)) {
				fail(String.format("Expected %s, got %s for item %d", expectHex, gotHex, i));
			}
		}
	}

}
