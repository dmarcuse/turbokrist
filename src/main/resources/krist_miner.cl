long hashToLong(byte* hash) {
	return hash[5] + (hash[4] << 8) + (hash[3] << 16) + ((long)hash[2] << 24) + ((long) hash[1] << 32) + ((long) hash[0] << 40);
}

// converts one long into 12 hex characters
void longToHex(long in, byte* hex, int offset) {
#pragma unroll
	for (int i = offset; i < 34; i++) {
		hex[i] = (in >> ((i - offset) * 5) & 31) + 48;
	}
}

__kernel void krist_miner_basic(
		__global const byte* address,	// 10 chars
		__global const byte* block,	// 12 chars
		__global const byte* prefix,	// 2 chars
		const long base,				// convert to 10 chars
		const long work,
		__global byte* output) {
	int id = get_global_id(0);
	long nonce = id + base;
	byte input[64];
	byte hashed[32];
#pragma unroll
	for (int i = 0; i < 10; i++) {
		input[i] = address[i];
	}
#pragma unroll
	for (int i = 10; i < 22; i++) {
		input[i] = block[i - 10];
	}
#pragma unroll
	for (int i = 22; i < 24; i++) {
		input[i] = prefix[i-22];
	}
#pragma unroll
	for (int i = 24; i < 34; i++) {
		input[i] = ((nonce >> ((i - 24) * 5)) & 31) + 48;
	}
	digest(input, 34, hashed);
	long score = hashToLong(hashed);
	if (score < work) {
#pragma unroll
		for (int i = 0; i < 10; i++) {
			output[i] = address[i];
		}
#pragma unroll
		for (int i = 10; i < 22; i++) {
			output[i] = block[i - 10];
		}
#pragma unroll
		for (int i = 22; i < 24; i++) {
			output[i] = prefix[i-22];
		}
#pragma unroll
		for (int i = 24; i < 34; i++) {
			output[i] = ((nonce >> ((i - 24) * 5)) & 31) + 48;
		}
	}
}
