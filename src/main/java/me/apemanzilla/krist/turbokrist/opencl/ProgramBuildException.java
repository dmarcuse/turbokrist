package me.apemanzilla.krist.turbokrist.opencl;

/**
 * Thrown when a problem is encountered using
 * {@link me.apemanzilla.krist.turbokrist.opencl.ProgramBuilder ProgramBuilder}.
 * 
 * @author apemanzilla
 *
 */
public class ProgramBuildException extends Exception {
	private static final long serialVersionUID = -7578219783003280034L;

	public ProgramBuildException() {
		super();
	}

	public ProgramBuildException(String s) {
		super(s);
	}
}
