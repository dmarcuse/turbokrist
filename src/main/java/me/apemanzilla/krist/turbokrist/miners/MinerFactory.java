package me.apemanzilla.krist.turbokrist.miners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.nativelibs4java.opencl.CLDevice;
import com.nativelibs4java.opencl.CLDevice.Type;
import com.nativelibs4java.opencl.CLPlatform;
import com.nativelibs4java.opencl.JavaCL;

import me.apemanzilla.krist.turbokrist.MinerOptions;

/**
 * Used to create {@link me.apemanzilla.krist.turbokrist.miners.Miner Miner}
 * objects that can be used to mine Krist
 * 
 * @author apemanzilla
 *
 */
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

	private MinerFactory() {
	}

	/**
	 * Creates a {@link me.apemanzilla.krist.turbokrist.miners.Miner Miner}
	 * object which can be used for mining Krist with OpenCL acceleration.
	 * 
	 * @param dev The {@link com.nativelibs4java.opencl.CLDevice CLDevice} to be
	 * used for mining.
	 * 
	 * @param options The {@link me.apemanzilla.krist.turbokrist.MinerOptions
	 * MinerOptions} object to use to configure the device.
	 * 
	 * @return A {@link me.apemanzilla.krist.turbokrist.miners.Miner Miner}
	 * object.
	 * 
	 * @throws MinerInitException When there is a problem creating the miner.
	 */
	public static Miner createOpenCLMiner(CLDevice dev, MinerOptions options) throws MinerInitException {
		if (!isCompatible(dev))
			throw new MinerInitException(String.format("Device %s is incompatible.", dev.getName().trim()));
		if (dev.getType().contains(Type.GPU)) {
			return new GPUMiner(dev, options);
		} else {
			throw new MinerInitException(String.format("Cannot create miner for device %s.", dev.getName().trim()));
		}
	}

	/**
	 * Creates all possible {@link me.apemanzilla.krist.turbokrist.miners.Miner
	 * Miner} objects from the given options.
	 * 
	 * @param options The {@link me.apemanzilla.krist.turbokrist.MinerOptions
	 * MinerOptions} object to use to configure the miners.
	 * 
	 * @return A {@code List} of miners.
	 */
	public static List<Miner> createAll(MinerOptions options) throws MinerInitException {
		List<Miner> miners = new ArrayList<Miner>();
		for (CLDevice dev : options.getMiningDevices()) {
			Miner m = createOpenCLMiner(dev, options);
			miners.add(m);
		}
		return miners;
	}

	/**
	 * @return A map linking integer IDs to CLDevices
	 */
	public static Map<Integer, CLDevice> getDeviceIDs() {
		return deviceIDs;
	}

	/**
	 * @param dev A CLDevice
	 * 
	 * @return Whether the given CLDevice can be used to mine Krist
	 */
	public static boolean isCompatible(CLDevice dev) {
		return dev.getType().contains(CLDevice.Type.GPU);
	}

	/**
	 * Gets a signature for the CLDevice. The signature is based on the device
	 * model, vendor, driver, and OpenCL profile, so it may be the same for two
	 * of the same devices.
	 * 
	 * @param dev The CLDevice
	 * 
	 * @return The hashcode of the CLDevice's signature
	 */
	public static int getSignature(CLDevice dev) {
		return dev.createSignature().hashCode();
	}

	public static String generatePrefix() {
		return String.format("%2x", new Random().nextInt(256));
	}

	public static int generateSignature(CLDevice dev) {
		return dev.createSignature().hashCode();
	}

}
