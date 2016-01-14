package me.apemanzilla.krist.turbokrist.cli;

import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;

/**
 * Command-line launcher for turbokrist OpenCL miner
 * @author apemanzilla
 *
 */
public class Launcher {

	private static Options options = new Options();
	
	static {
		options.addOption(Option.builder("h").longOpt("host").hasArg().argName("address").desc("The Krist address to mine for").build());
		options.addOption(Option.builder("p").longOpt("profiler").desc("Start the system profiler to optimize mining").build());
		options.addOption(Option.builder("l").longOpt("list-devices").desc("Show a list of compatible devices").build());
		options.addOption(Option.builder("?").longOpt("help").desc("Show command-line usage").build());
	}
	
	public static void printHelp() {
		HelpFormatter hf = new HelpFormatter();
		// show in order defined
		hf.setOptionComparator(null);
		hf.printHelp("turbokrist -h <address>", options);
	}
	
	public static void main(String[] args) {
		
	}

}
