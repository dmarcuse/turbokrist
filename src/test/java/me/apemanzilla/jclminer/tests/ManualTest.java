package me.apemanzilla.jclminer.tests;

import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.krist.state.NodeState;
import me.apemanzilla.krist.turbokrist.MinerOptions;
import me.apemanzilla.krist.turbokrist.MinerUtils;
import me.apemanzilla.krist.turbokrist.miners.Miner;
import me.apemanzilla.krist.turbokrist.miners.MinerFactory;
import me.apemanzilla.krist.turbokrist.miners.MinerGroup;
import me.apemanzilla.krist.turbokrist.miners.MinerInitException;
import me.apemanzilla.krist.turbokrist.miners.MinerListener;
import me.apemanzilla.krist.turbokrist.miners.Solution;
import me.apemanzilla.kristapi.KristAPI;
import me.apemanzilla.kristapi.exceptions.RemoteErrorException;
import me.apemanzilla.kristapi.exceptions.SyncnodeDownException;

public class ManualTest implements Runnable, MinerListener {

	private static Miner m;

	public static void main(String[] args)
			throws MinerInitException, SyncnodeDownException, RemoteErrorException, InterruptedException {
		MinerOptions mo = new MinerOptions("k5ztameslf");
		mo.setWorkSize(MinerFactory.generateSignature(JavaCL.getBestDevice()), (int) Math.pow(2, 22));
		Miner temp = MinerFactory.createOpenCLMiner(JavaCL.getBestDevice(), mo);
		Thread t = new Thread(new ManualTest());
		t.start();
		MinerGroup mg = new MinerGroup(temp);
		m = mg;
		m.addListener(new ManualTest());
		m.start(KristAPI.getBlock(), (int) KristAPI.getWork());
	}

	@Override
	public void blockSolved(Solution sol) {
		m.stop();
		System.out.println(sol.toString());
		System.out.println(sol.getNonce());
		System.exit(1);
	}

	@Override
	public void run() {
		while (true) {
			if (m.isMining()) {
				System.out.println(MinerUtils.formatSpeed((long) m.getRecentHashrate()));
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
			}
		}
	}

}
