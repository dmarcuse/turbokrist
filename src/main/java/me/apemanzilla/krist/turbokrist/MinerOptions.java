package me.apemanzilla.krist.turbokrist;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nativelibs4java.opencl.CLDevice;

import me.apemanzilla.krist.api.KristAPI;
import me.apemanzilla.krist.api.exceptions.MalformedAddressException;
import me.apemanzilla.krist.api.types.KristAddress;
import me.apemanzilla.krist.turbokrist.miners.MinerFactory;

public class MinerOptions {

	private final KristAPI api;
	
	private KristAddress address;

	private Map<Integer, Integer> workSizes = new HashMap<Integer, Integer>();

	private Set<Integer> devices = new HashSet<Integer>();

	private int stateRefreshRate = 2000;

	public KristAddress getKristAddress() {
		return address;
	}

	public MinerOptions(String address, KristAPI api) throws MalformedAddressException {
		this.api = api;
		this.address = api.getAddress(address);
	}

	public KristAPI getAPI() {
		return api;
	}
	
	public void setWorkSize(int signature, int size) {
		workSizes.put(signature, size);
	}

	public int getWorkSize(int signature) {
		return workSizes.containsKey(signature) ? workSizes.get(signature) : 1024;
	}

	/**
	 * Selects the best available device to be used for mining (determined by
	 * the maximum compute units for each device).
	 */
	public void selectBestDevice() {
		int best = -1;
		Map<Integer, CLDevice> devices = MinerFactory.getDeviceIDs();
		for (Entry<Integer, CLDevice> es : devices.entrySet()) {
			if (best == -1) {
				best = es.getKey();
			} else {
				if (es.getValue().getMaxComputeUnits() > devices.get(best).getMaxComputeUnits()) {
					best = es.getKey();
				}
			}
		}
		this.devices.clear();
		this.devices.add(best);
	}

	/**
	 * Selects all devices to be used for mining.
	 */
	public void selectAllDevices() {
		Map<Integer, CLDevice> devices = MinerFactory.getDeviceIDs();
		this.devices.clear();
		for (Entry<Integer, CLDevice> es : devices.entrySet()) {
			this.devices.add(es.getKey());
		}
	}

	/**
	 * Selects a given set of devices for mining. @param ids The IDs of the
	 * devices to use for mining. @see
	 * me.apemanzilla.krist.turbokrist.miners.MinerFactory#getDeviceIDs()
	 * MinerFactory.getDeviceIDs
	 */
	public void selectDevices(int... ids) {
		this.devices.clear();
		for (int i : ids) {
			this.devices.add(i);
		}
	}

	/**
	 * Gets a list of CLDevices based on the currently selected devices. @return
	 * A {@code List<CLDevice>} of devices to use for mining.
	 */
	public List<CLDevice> getMiningDevices() {
		List<CLDevice> out = new ArrayList<CLDevice>();
		Map<Integer, CLDevice> devices = MinerFactory.getDeviceIDs();
		for (int i : this.devices) {
			out.add(devices.get(i));
		}
		return out;
	}

	public int getStateRefreshRate() {
		return stateRefreshRate;
	}

	public void setStateRefreshRate(int stateRefreshRate) {
		this.stateRefreshRate = stateRefreshRate;
	}

}
