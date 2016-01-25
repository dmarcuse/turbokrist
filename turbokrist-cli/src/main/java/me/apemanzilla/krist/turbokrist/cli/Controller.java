package me.apemanzilla.krist.turbokrist.cli;

import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;

import me.apemanzilla.krist.api.exceptions.InvalidNonceException;
import me.apemanzilla.krist.api.exceptions.SyncnodeDownException;
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

	private final MinerOptions options;
	private NodeState state;
	private MinerGroup miners;
	private Thread status;
	private long blocks = 0; // one can dream
	private long startTime = System.currentTimeMillis();
	private Timer autoRestart;

	private void printStatus() {
		String recentSpeedStr = StringUtils.center("Now " + MinerUtils.formatSpeed((long) miners.getRecentHashrate()), 19);
		String avgSpeedStr = StringUtils.center("Avg " + MinerUtils.formatSpeed((long) miners.getAverageHashrate()), 19);
		String blocksStr = StringUtils.center(blocks + " blocks", 15);
		double blocksPerMinute = (double) blocks / ((double) (System.currentTimeMillis() - startTime) / 60000);
		String bpmStr = StringUtils.center(String.format("%.2f blocks/minute", blocksPerMinute), 25);
		System.out.format("%s|%s|%s|%s\n", recentSpeedStr, avgSpeedStr, blocksStr, bpmStr);
	}
	
	public Controller(MinerOptions options) throws MinerInitException {
		this.options = options;
		state = new NodeState(options.getStateRefreshRate(), options.getAPI());
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
				if (options.getKristAddress().submitBlock(sol.getNonce())) {
					System.out.println("Success!");
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
			} catch (SyncnodeDownException | InvalidNonceException e) {
				System.out.println("Error!");
				e.printStackTrace();
				miners.start(state.getBlock(), (int) state.getWork());
			}
		}
	}

}
