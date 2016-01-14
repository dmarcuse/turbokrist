package me.apemanzilla.krist.turbokrist.cli;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.krist.turbokrist.miners.MinerFactory;

/**
 * Command-line launcher for turbokrist OpenCL miner
 * 
 * @author apemanzilla
 *
 */
public class Launcher {

	private static Options options = new Options();

	static {
		options.addOption(Option.builder("h").longOpt("host").hasArg().argName("address")
				.desc("The Krist address to mine for").build());
		options.addOption(
				Option.builder("p").longOpt("profiler").desc("Start the system profiler to optimize mining").build());
		options.addOption(
				Option.builder("l").longOpt("list-devices").desc("Show a list of compatible devices").build());
		options.addOption(Option.builder("v").longOpt("verbose").desc("Enable verbose logging").build());
		options.addOption(Option.builder("?").longOpt("help").desc("Show command-line usage").build());
	}

	public static void printHelp() {
		HelpFormatter hf = new HelpFormatter();
		// show in order defined
		hf.setOptionComparator(null);
		hf.printHelp("turbokrist -h <address>", options);
	}

	public static void listDevices() {
		Map<Integer, CLDevice> devices = MinerFactory.getDeviceIDs();
		System.out.format("%20s | %11s | %4s\n", "Name", "Signature", "ID");
		Set<Entry<Integer, CLDevice>> es = devices.entrySet();
		for (Entry<Integer, CLDevice> e : es) {
			System.out.format("%20s | %11s | %4s\n", e.getValue().getName().trim(),
					MinerFactory.getSignature(e.getValue()), e.getKey());
		}
	}

	public static void main(String[] args) throws ParseException {
		CommandLine cmd = new DefaultParser().parse(options, args);
		boolean verbose = cmd.hasOption('v');
		if (cmd.hasOption('l')) {
			if (verbose)
				System.out.println("Listing compatible OpenCL devices");
			listDevices();
			System.exit(1);
		} else if (cmd.hasOption('p')) {
			if (verbose)
				System.out.println("Running system profiler");
			// TODO: run profiler
			System.exit(1);
		} else if (cmd.hasOption('?')) {
			if (verbose)
				System.out.println("Come on, do you really need verbose output when looking at the help?");
			printHelp();
			System.exit(1);
		}
	}

}
