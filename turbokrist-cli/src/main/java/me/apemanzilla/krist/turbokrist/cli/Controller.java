package me.apemanzilla.krist.turbokrist.cli;

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
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;

public class Controller implements MinerListener, NodeStateListener {

	private final MinerOptions options;
	private NodeState state;
	private MinerGroup miners;
	private Thread status;
	private long blocks = 0; // one can dream
	private long startTime = System.currentTimeMillis();

	public Controller(MinerOptions options) throws MinerInitException {
		this.options = options;
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
						System.out.format("Speed - %s\n Blocks - %d",
								MinerUtils.formatSpeed((long) miners.getRecentHashrate()), blocks);
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
		System.out.println("Block changed");
		synchronized (miners) {
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
				} else {
					System.out.println("Rejected.");
					miners.start(state.getBlock(), (int) state.getWork());
				}
			} catch (SyncnodeDownException e) {
				System.out.println("Error!");
				e.printStackTrace();
			}
		}
	}

}
