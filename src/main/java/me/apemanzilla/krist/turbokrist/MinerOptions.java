package me.apemanzilla.krist.turbokrist;

import java.util.HashMap;
import java.util.Map;

import me.apemanzilla.kristapi.types.KristAddress;

public class MinerOptions {

	private KristAddress address;
	
	private Map<Integer, Integer> workSizes = new HashMap<Integer, Integer>();
	
	public KristAddress getKristAddress() {
		return address;
	}

	public MinerOptions(String address) {
		this.address = KristAddress.auto(address);
	}

	public void setWorkSize(int signature, int size) {
		workSizes.put(signature, size);
	}
	
	public int getWorkSize(int signature) {
		return workSizes.containsKey(signature) ? workSizes.get(signature) : 1024;
	}
	
}
