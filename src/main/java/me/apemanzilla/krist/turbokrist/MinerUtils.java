package me.apemanzilla.krist.turbokrist;

import java.text.DecimalFormat;

/**
 * Contains code to assist Krist mining Some of this code is taken from sci4me's
 * <a href="https://github.com/sci4me/skristminer">skristminer</a>
 * 
 * @author apemanzilla @author sci4me
 */
public final class MinerUtils {

	private static final DecimalFormat format = new DecimalFormat("0.00");
	public static final int[] K = { 0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4,
			0xab1c5ed5, 0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
			0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da, 0x983e5152,
			0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967, 0x27b70a85, 0x2e1b2138,
			0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85, 0xa2bfe8a1, 0xa81a664b, 0xc24b8b70,
			0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070, 0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5,
			0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3, 0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa,
			0xa4506ceb, 0xbef9a3f7, 0xc67178f2 };

	private MinerUtils() {
	}

	/**
	 * @author apemanzilla
	 */
	public static char[] getChars(final byte[] b) {
		final char[] chars = new char[b.length];
		for (int i = 0; i < chars.length; i++) {
			chars[i] = (char) b[i];
		}
		return chars;
	}

	/**
	 * @author sci4me
	 */
	public static byte[] getBytes(final String s) {
		final byte[] bytes = new byte[s.length()];
		for (int i = 0; i < bytes.length; i++)
			bytes[i] = (byte) s.charAt(i);
		return bytes;
	}

	/**
	 * @author sci4me
	 */
	public static long hashToLong(final byte[] hash) {
		long ret = 0;
		for (int i = 5; i >= 0; i--)
			ret += (hash[i] & 0xFF) * Math.pow(256, 5 - i);
		return ret;
	}

	/**
	 * @author sci4me
	 */
	public static String formatSpeed(final long rawSpeed) {
		String result;

		if (rawSpeed > 1000000000) {
			final double speed = (double) rawSpeed / 1000000000;
			result = format.format(speed) + " GH/s";
		} else if (rawSpeed > 1000000) {
			final double speed = (double) rawSpeed / 1000000;
			result = format.format(speed) + " MH/s";
		} else if (rawSpeed > 1000) {
			final double speed = (double) rawSpeed / 1000;
			result = format.format(speed) + " KH/s";
		} else {
			result = rawSpeed + " H/s";
		}

		return result;
	}

	/**
	 * @author sci4me
	 */
	public static byte[] digest(final byte[] message) {
		byte[] hashed = new byte[32], padded = MinerUtils.padMessage(message);

		int h0 = 0x6a09e667;
		int h1 = 0xbb67ae85;
		int h2 = 0x3c6ef372;
		int h3 = 0xa54ff53a;
		int h4 = 0x510e527f;
		int h5 = 0x9b05688c;
		int h6 = 0x1f83d9ab;
		int h7 = 0x5be0cd19;

		final int pl64 = padded.length / 64;
		int i, j, sa, sb, j4;
		int a, b, c, d, e, f, g, h, s0, s1, maj, t1, t2, ch, i64;
		int[] words = new int[64];

		for (i = 0; i < pl64; i++) {
			a = h0;
			b = h1;
			c = h2;
			d = h3;
			e = h4;
			f = h5;
			g = h6;
			h = h7;
			i64 = i * 64;

			for (j = 0; j < 16; j++) {
				j4 = j * 4 + i64;
				words[j] |= ((padded[j4] & 0x000000FF) << 24);
				words[j] |= ((padded[j4 + 1] & 0x000000FF) << 16);
				words[j] |= ((padded[j4 + 2] & 0x000000FF) << 8);
				words[j] |= (padded[j4 + 3] & 0x000000FF);
			}

			for (j = 16; j < 64; j++) {
				sa = words[j - 15];
				sb = words[j - 2];
				s0 = Integer.rotateRight(sa, 7) ^ Integer.rotateRight(sa, 18) ^ (sa >>> 3);
				s1 = Integer.rotateRight(sb, 17) ^ Integer.rotateRight(sb, 19) ^ (sb >>> 10);
				words[j] = words[j - 16] + s0 + words[j - 7] + s1;
			}

			for (j = 0; j < 64; j++) {
				s0 = Integer.rotateRight(a, 2) ^ Integer.rotateRight(a, 13) ^ Integer.rotateRight(a, 22);
				maj = (a & b) ^ (a & c) ^ (b & c);
				t2 = s0 + maj;
				s1 = Integer.rotateRight(e, 6) ^ Integer.rotateRight(e, 11) ^ Integer.rotateRight(e, 25);
				ch = (e & f) ^ (~e & g);
				t1 = h + s1 + ch + MinerUtils.K[j] + words[j];

				h = g;
				g = f;
				f = e;
				e = d + t1;
				d = c;
				c = b;
				b = a;
				a = t1 + t2;
			}

			h0 += a;
			h1 += b;
			h2 += c;
			h3 += d;
			h4 += e;
			h5 += f;
			h6 += g;
			h7 += h;
		}

		hashed[0] = (byte) ((h0 >>> 56) & 0xff);
		hashed[1] = (byte) ((h0 >>> 48) & 0xff);
		hashed[2] = (byte) ((h0 >>> 40) & 0xff);
		hashed[3] = (byte) ((h0 >>> 32) & 0xff);

		hashed[4] = (byte) ((h1 >>> 56) & 0xff);
		hashed[5] = (byte) ((h1 >>> 48) & 0xff);
		hashed[6] = (byte) ((h1 >>> 40) & 0xff);
		hashed[7] = (byte) ((h1 >>> 32) & 0xff);

		hashed[8] = (byte) ((h2 >>> 56) & 0xff);
		hashed[9] = (byte) ((h2 >>> 48) & 0xff);
		hashed[10] = (byte) ((h2 >>> 40) & 0xff);
		hashed[11] = (byte) ((h2 >>> 32) & 0xff);

		hashed[12] = (byte) ((h3 >>> 56) & 0xff);
		hashed[13] = (byte) ((h3 >>> 48) & 0xff);
		hashed[14] = (byte) ((h3 >>> 40) & 0xff);
		hashed[15] = (byte) ((h3 >>> 32) & 0xff);

		hashed[16] = (byte) ((h4 >>> 56) & 0xff);
		hashed[17] = (byte) ((h4 >>> 48) & 0xff);
		hashed[18] = (byte) ((h4 >>> 40) & 0xff);
		hashed[19] = (byte) ((h4 >>> 32) & 0xff);

		hashed[20] = (byte) ((h5 >>> 56) & 0xff);
		hashed[21] = (byte) ((h5 >>> 48) & 0xff);
		hashed[22] = (byte) ((h5 >>> 40) & 0xff);
		hashed[23] = (byte) ((h5 >>> 32) & 0xff);

		hashed[24] = (byte) ((h6 >>> 56) & 0xff);
		hashed[25] = (byte) ((h6 >>> 48) & 0xff);
		hashed[26] = (byte) ((h6 >>> 40) & 0xff);
		hashed[27] = (byte) ((h6 >>> 32) & 0xff);

		hashed[28] = (byte) ((h7 >>> 56) & 0xff);
		hashed[29] = (byte) ((h7 >>> 48) & 0xff);
		hashed[30] = (byte) ((h7 >>> 40) & 0xff);
		hashed[31] = (byte) ((h7 >>> 32) & 0xff);

		return hashed;
	}

	/**
	 * @author sci4me
	 */
	public static byte[] padMessage(final byte[] data) {
		final int origLength = data.length;
		final int tailLength = origLength % 64;

		final int padLength;
		if ((64 - tailLength >= 9))
			padLength = 64 - tailLength;
		else
			padLength = 128 - tailLength;

		final byte[] output = new byte[origLength + padLength];

		final long lengthInBits = origLength * 8;
		final int lm1 = output.length - 1;
		output[lm1] = (byte) (lengthInBits & 0xFF);
		output[lm1 - 1] = (byte) ((lengthInBits >>> 8) & 0xFF);
		output[lm1 - 2] = (byte) ((lengthInBits >>> 16) & 0xFF);
		output[lm1 - 3] = (byte) ((lengthInBits >>> 24) & 0xFF);
		output[lm1 - 4] = (byte) ((lengthInBits >>> 32) & 0xFF);
		output[lm1 - 5] = (byte) ((lengthInBits >>> 40) & 0xFF);
		output[lm1 - 6] = (byte) ((lengthInBits >>> 48) & 0xFF);
		output[lm1 - 7] = (byte) ((lengthInBits >>> 56) & 0xFF);
		output[origLength] = (byte) 0x80;

		System.arraycopy(data, 0, output, 0, origLength);

		return output;
	}
}