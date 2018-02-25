package me.apemanzilla.krist.turbokrist.cli;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.security.SecureRandom;
import java.util.Timer;
import java.util.TimerTask;

import me.lignum.jkrist.Address;
import me.lignum.jkrist.Block;
import me.lignum.jkrist.KristAPIException;
import org.apache.commons.lang.RandomStringUtils;
import org.apache.commons.lang.StringUtils;

import me.apemanzilla.krist.state.NodeState;
import me.apemanzilla.krist.state.NodeStateListener;
import me.apemanzilla.krist.turbokrist.MinerOptions;
import me.apemanzilla.krist.turbokrist.MinerUtils;
import me.apemanzilla.krist.turbokrist.miners.Miner;
import me.apemanzilla.krist.turbokrist.miners.MinerFactory;
import me.apemanzilla.krist.turbokrist.miners.MinerGroup;
import me.apemanzilla.krist.turbokrist.miners.MinerInitException;
import me.apemanzilla.krist.turbokrist.miners.MinerListener;
import me.apemanzilla.krist.turbokrist.miners.Solution;

public class Controller implements MinerListener, NodeStateListener {
	private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
	
	private final MinerOptions options;
	private NodeState state;
	private MinerGroup miners;
	private Thread status;
	private long blocks = 0; // one can dream
	private long startTime = System.currentTimeMillis();
	private Timer autoRestart;

	private void printStatus() {
		String recentSpeedStr = StringUtils.center("Now " + MinerUtils.formatSpeed((long) miners.getRecentHashrate()), 19);
		String blocksStr = StringUtils.center(blocks + " blocks", 15);
		double blocksPerMinute = (double) blocks / ((double) (System.currentTimeMillis() - startTime) / 60000);
		String bpmStr = StringUtils.center(String.format("%.2f blocks/minute", blocksPerMinute), 25);
		System.out.format("%s|%s|%s\n", recentSpeedStr, blocksStr, bpmStr);
	}
	
	public Controller(MinerOptions options) throws MinerInitException {
		this.options = options;
		
		if (options.isRelay()) {
			try {
				setupTempAddress();
			} catch (IOException | KristAPIException e) {
				e.printStackTrace();
			}
		}
		
		state = new NodeState(options.getStateRefreshRate());
		state.addListener(this);
		Miner[] m = MinerFactory.createAll(options).toArray(new Miner[0]);
		miners = new MinerGroup(m);
		miners.addListener(this);
		status = new Thread(new Runnable() {
			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					if (miners.isMining()) {
						printStatus();
					}
				}
			}
		});
		status.start();
	}
	
	private void setupTempAddress() throws IOException, KristAPIException {
		if (options.getPrivatekey() == null) {
			File privatekeyFile = new File("privatekey");
			
			if (privatekeyFile.exists()) {
				options.setPrivatekey(new String(Files.readAllBytes(privatekeyFile.toPath()), StandardCharsets.UTF_8));
			} else {
				// generate a 64-char secure random pw
				options.setPrivatekey(RandomStringUtils.random(64, 0, ALPHANUM.length(),
					false, false, ALPHANUM.toCharArray(), new SecureRandom()));
				
				Files.write(privatekeyFile.toPath(), options.getPrivatekey().getBytes());
			}
		}
		
		options.setTempAddress(Address.makeV2Address(options.getPrivatekey()));
		NodeState.getKrist().login(options.getPrivatekey()); // make sure the addy exists
	}
	
	public void start() {
		state.start();
	}

	@Override
	public void stateChanged(String newBlock, long newWork) {
		synchronized (miners) {
			if (autoRestart != null) {
				autoRestart.cancel();
				autoRestart = null;
			}
			if (miners.isMining()) {
				miners.stop();
			}
			System.out.format("Mining for block '%s' - work %d.\n", newBlock, newWork);
			miners.start(newBlock, (int) newWork);
		}
	}

	@Override
	public void blockSolved(Solution sol) {
		synchronized (miners) {
			miners.stop();
			System.out.format("Submitting solution '%s' > ", sol.getNonce());
			try {
				String encoded = sol.getNonce();
				
				Block block = NodeState.getKrist().submitBlock(options.getMiningAddress(), encoded);

				if (block != null) {
					if (options.isRelay()) {
						NodeState.getKrist().makeTransaction(options.getPrivatekey(), options.getDepositAddress(), block.getValue());
					}
					
					System.out.println("Success! Mined block '" + block.getShortHash() + "'.");
					blocks++;
					autoRestart = new Timer();
					autoRestart.schedule(new TimerTask() {
						@Override
						public void run() {
							miners.start(state.getBlock(), (int) state.getWork());
						}
						
					}, 15000);
				} else {
					System.out.println("Rejected.");
					miners.start(state.getBlock(), (int) state.getWork());
				}
			} catch (KristAPIException e) {
				System.out.println("Error!");
				e.printStackTrace();
				miners.start(state.getBlock(), (int) state.getWork());
			}
		}
	}

}
