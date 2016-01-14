package me.apemanzilla.krist.turbokrist;

import me.apemanzilla.kristapi.types.KristAddress;

public class MinerOptions {
	
	private KristAddress address;
	
	public KristAddress getAddress() {
		return address;
	}
	
	public MinerOptions(String address) {
		this.address = KristAddress.auto(address);
	}
	
}
