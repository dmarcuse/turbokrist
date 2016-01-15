package me.apemanzilla.jclminer.tests;

import static org.junit.Assert.*;

import org.bridj.Pointer;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

import com.nativelibs4java.opencl.CLBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLMem.Usage;

import me.apemanzilla.krist.turbokrist.MinerUtils;

/**
 * This class contains multiple JUnit tests that test the OpenCL portions of JCLMiner code.
 * This should be used to ensure that valid results are produced when modifying the OpenCL code.
 * @author apemanzilla
 *
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class TestCLMacros extends OpenCLTest {
	
	/**
	 * Tests whether the RR macro (rotate right) is producing expected results - compares output from OpenCL to Java's results
	 */
	@Test
	public void testMacro_RR() {
		int[] input = {1, 5, -20, -190, 40, Integer.MAX_VALUE, Integer.MIN_VALUE, 0};
		int[] dist = new int[input.length];
		for (int i = 0; i < dist.length; i++)
			dist[i] = 1;
		Pointer<Integer>
				inputPtr = Pointer.allocateInts(input.length).order(context.getByteOrder()),
				distPtr = Pointer.allocateInts(input.length).order(context.getByteOrder());
		for (int i = 0; i < input.length; i++) {
			inputPtr.set(i, input[i]);
			// default value should be 1
			distPtr.set(i, dist[i]);
		}
		CLBuffer<Integer>
				inputBuf = context.createIntBuffer(Usage.Input, inputPtr),
				distBuf = context.createIntBuffer(Usage.Input, distPtr),
				outputBuf = context.createIntBuffer(Usage.Output, input.length);
		CLKernel kernel = program.createKernel("testRR", inputBuf, distBuf, outputBuf, input.length);
		CLEvent evt = kernel.enqueueNDRange(queue, new int[] {input.length});
		Pointer<Integer> outputPtr = outputBuf.read(queue, evt);
		for (int i = 0; i < input.length; i++) {
			int
					got = outputPtr.get(i).intValue(),
					expected = Integer.rotateRight(input[i], dist[i]);
			assertEquals(String.format("Got %d, expected %d", got, expected), expected, got);
		}
	}
	
	/**
	 * Tests whether the PAD macro is producing expected results
	 */
	@Test
	public void testMacro_PAD() {
		// create an input that's 55 characters long (max supported input length)
		String longInput = "";
		for (int i = 0; i < 55; i++) {
			longInput += (char) i;
		}
		String inputs[] = {"", "hello", "hello world", "ThIIs ^Is_ a T3ZT", longInput};
		CLKernel kernel = program.createKernel("testPadding");
		for (int j = 0; j < inputs.length; j++) {
			String input = inputs[j];
			byte[] inpbytes = MinerUtils.getBytes(input);
			Pointer<Byte> inputPtr = Pointer.allocateBytes(input.length());
			for (int i = 0; i < input.length(); i++) {
				inputPtr.set(i, inpbytes[i]);
			}
			CLBuffer<Byte>
					inputBuf = input.length() > 0 ? context.createByteBuffer(Usage.Input, inputPtr) : context.createByteBuffer(Usage.Input, 1),
					outputBuf = context.createByteBuffer(Usage.Output, 64);
			kernel.setArgs(inputBuf, input.length(), outputBuf);
			CLEvent evt = kernel.enqueueNDRange(queue, new int[] {1});
			Pointer<Byte> outputPtr = outputBuf.read(queue, evt);
			byte[] output = new byte[64];
			for (int i = 0; i < 64; i++) {
				output[i] = outputPtr.get(i);
			}
			byte[] expect = MinerUtils.padMessage(MinerUtils.getBytes(input));
			for (int i = 0; i < 64; i++) {
				if (output[i] != expect[i]) {
					fail(String.format("Mismatch on byte %d when padding item %d: read byte %d, wanted byte %d", i, j, output[i], expect[i]));
				}
			}
		}
	}
}
