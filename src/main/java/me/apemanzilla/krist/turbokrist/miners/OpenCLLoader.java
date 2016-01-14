package me.apemanzilla.krist.turbokrist.miners;

import java.io.IOException;
import java.io.InputStream;

import me.apemanzilla.krist.turbokrist.MinerOptions;

/**
 * Loads the necessary OpenCL code for mining. Use the
 * {@link #load(MinerOptions)} method.
 * 
 * @author apemanzilla
 *
 */
public class OpenCLLoader {

	private OpenCLLoader() {
	}

	private static String[] files = { "/sha256.cl", "krist_miner.cl" };

	/**
	 * Loads the necessary code for mining as a single String.
	 * 
	 * @param options
	 *            Can be used to specify options when loading the OpenCL code.
	 * @return A single String of the concatenated contents of the code files.
	 *         If there is a problem loading the code, will instead return
	 *         {@code null}.
	 */
	public static String load(MinerOptions options) {
		try {
			String code = "";
			for (String f : files) {
				InputStream is = OpenCLLoader.class.getResourceAsStream(f);
				if (is == null)
					return null;
				if (is.available() == 0)
					return null;
				byte[] data = new byte[is.available()];
				is.read(data, 0, is.available());
				code += new String(data);
				code += "\n";
			}
			return code;
		} catch (IOException e) {
			return null;
		}
	}

}
