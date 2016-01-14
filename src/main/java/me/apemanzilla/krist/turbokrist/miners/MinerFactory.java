package me.apemanzilla.krist.turbokrist.miners;

import java.util.HashMap;
import java.util.Map;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

public class MinerFactory {

	private static final HashMap<Integer, CLDevice> deviceIDs = new HashMap<Integer, CLDevice>();
	
	static {
		// build device ID map
		CLPlatform[] platforms = JavaCL.listPlatforms();
		for (CLPlatform plat : platforms) {
			CLDevice[] devices = plat.listAllDevices(true);
			for (CLDevice dev : devices) {
				if (isCompatible(dev)) {
					deviceIDs.put(deviceIDs.size() + 1, dev);
				}
			}
		}
	}
	
	public static Map<Integer, CLDevice> getDeviceIDs() {
		return deviceIDs;
	}
	
	public static boolean isCompatible(CLDevice dev) {
		return dev.getType().contains(CLDevice.Type.GPU);
	}
	
	public static int getSignature(CLDevice dev) {
		return dev.createSignature().hashCode();
	}
	
}
