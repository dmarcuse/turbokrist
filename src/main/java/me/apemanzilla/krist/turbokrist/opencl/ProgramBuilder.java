package me.apemanzilla.krist.turbokrist.opencl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.nativelibs4java.opencl.CLContext;
import com.nativelibs4java.opencl.CLProgram;

/**
 * Used to build CLPrograms for mining, with customizable properties.
 * 
 * @author apemanzilla
 *
 */
public class ProgramBuilder {

	private final List<String> files;
	private final List<String> buildOptions = new ArrayList<String>();
	private final Map<String, Object> macros = new HashMap<String, Object>();

	/**
	 * Creates a new {@code ProgramBuilder}. Specify source files (included in
	 * the compiled binary) to be added to the program.
	 * 
	 * @param files
	 */
	public ProgramBuilder(String... files) {
		this.files = Arrays.asList(files);
	}

	/**
	 * Gets an array of the files to be included in the program.
	 * 
	 * @return A 'safe' array containing the files to be included. Changes to
	 * this array will not affect the program.
	 */
	public String[] getFiles() {
		return (String[]) files.toArray();
	}

	/**
	 * Adds a build option to the program.
	 * 
	 * @param option
	 */
	public void addBuildOption(String option) {
		buildOptions.add(option);
	}

	/**
	 * Gets an array of the options to be used when building the program.
	 * 
	 * @return A 'safe' array containing the build options to be used. Changes
	 * to this array will not affect the output.
	 */
	public String[] getBuildOptions() {
		return (String[]) buildOptions.toArray();
	}

	/**
	 * Adds a macro to be defined when building the program.
	 * 
	 * @param name The macro name. @param value The macro value.
	 */
	public void defineMacro(String name, Object value) {
		macros.put(name, value);
	}

	/**
	 * Builds and returns the program for a {@code CLContext}.
	 * 
	 * @param context @return @throws ProgramBuildException
	 */
	public CLProgram build(CLContext context) throws ProgramBuildException {
		// load raw code
		String code[] = new String[files.size()];
		for (int i = 0; i < files.size(); i++) {
			try {
				InputStream is = ProgramBuilder.class
						.getResourceAsStream((files.get(i).charAt(0) == '/' ? "" : "/") + files.get(i));
				if (is == null)
					throw new ProgramBuildException(String.format("Failed to load resource %s", files.get(i)));
				if (is.available() > 0) {
					byte[] data = new byte[is.available()];
					is.read(data, 0, is.available());
					code[i] = new String(data);
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new ProgramBuildException(String.format("IOException thrown loading %s", files.get(i)));
			}
		}
		CLProgram p = context.createProgram(code);
		// define macros
		p.defineMacros(macros);
		// build program
		for (String option : buildOptions) {
			p.addBuildOption(option);
		}
		return p.build();
	}

}
