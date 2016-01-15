package me.apemanzilla.jclminer.tests;

import org.junit.After;
import org.junit.Before;

import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLKernel;
import com.nativelibs4java.opencl.CLProgram;
import com.nativelibs4java.opencl.CLQueue;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.krist.turbokrist.opencl.ProgramBuildException;
import me.apemanzilla.krist.turbokrist.opencl.ProgramBuilder;

public class OpenCLTest {

	CLContext context;
	CLQueue queue;
	CLProgram program;

	@Before
	public void setUp() throws ProgramBuildException {
		// Set up context and queue
		context = JavaCL.createBestContext();
		queue = context.createDefaultQueue();
		// Build program
		ProgramBuilder pb = new ProgramBuilder("sha256.cl", "krist_miner.cl", "test_kernels.cl");
		pb.addBuildOption("-Werror");
		pb.defineMacro("UNIT_TESTING", 1);
		program = pb.build(context);
		// Test that CL code can be compiled and the testCompile kernel can be
		// run without errors
		CLKernel kernel = program.createKernel("testCompile");
		CLEvent compilationTest = kernel.enqueueNDRange(queue, new int[] { 1 });
		compilationTest.waitFor();
	}

	@After
	public void tearDown() throws Exception {
		context.release();
	}

}
