// This file contains code for hashing and mining on OpenCL hardware

typedef uchar byte;

// union to convert 4 bytes to an int or vice versa
union byte_int_converter {
	uint val;
	byte bytes[4];
};

// macro so i can change it later
#define mult_add(a,b,c) (a * b + c)

// right rotate macro
#define RR(X, Y) rotate((uint)X, -((uint)Y))

// optimized padding macro
// takes a character array and integer
// character array is used as both input and output
// character array should be 64 items long regardless of content
// actual input present in character array should not exceed 55 items
// second argument should be the length of the input content
// example usage:
//	char data[64];
//	data[0] = 'h';
//	data[1] = 'e';
//	data[2] = 'l';
//	data[3] = 'l';
//	data[4] = 'o';
//	PAD(data, 5);
//	// data array now contains 'hello' padded
#define PAD(X, Y) X[63] = Y * 8; X[62] =  Y >> 5; X[Y] = 0x80;

// SHA256 macros
#define CH(x,y,z) bitselect(z,y,x)
#define MAJ(x,y,z) bitselect(x,y,z^x)
#define EP0(x) (RR(x,2) ^ RR(x,13) ^ RR(x,22))
#define EP1(x) (RR(x,6) ^ RR(x,11) ^ RR(x,25))
#define SIG0(x) (RR(x,7) ^ RR(x,18) ^ ((x) >> 3))
#define SIG1(x) (RR(x,17) ^ RR(x,19) ^ ((x) >> 10))

__constant uint K[64] = {
	0x428a2f98, 0x71374491, 0xb5c0fbcf, 0xe9b5dba5, 0x3956c25b, 0x59f111f1, 0x923f82a4, 0xab1c5ed5,
	0xd807aa98, 0x12835b01, 0x243185be, 0x550c7dc3, 0x72be5d74, 0x80deb1fe, 0x9bdc06a7, 0xc19bf174,
	0xe49b69c1, 0xefbe4786, 0x0fc19dc6, 0x240ca1cc, 0x2de92c6f, 0x4a7484aa, 0x5cb0a9dc, 0x76f988da,
	0x983e5152, 0xa831c66d, 0xb00327c8, 0xbf597fc7, 0xc6e00bf3, 0xd5a79147, 0x06ca6351, 0x14292967,
	0x27b70a85, 0x2e1b2138, 0x4d2c6dfc, 0x53380d13, 0x650a7354, 0x766a0abb, 0x81c2c92e, 0x92722c85,
	0xa2bfe8a1, 0xa81a664b, 0xc24b8b70, 0xc76c51a3, 0xd192e819, 0xd6990624, 0xf40e3585, 0x106aa070,
	0x19a4c116, 0x1e376c08, 0x2748774c, 0x34b0bcb5, 0x391c0cb3, 0x4ed8aa4a, 0x5b9cca4f, 0x682e6ff3,
	0x748f82ee, 0x78a5636f, 0x84c87814, 0x8cc70208, 0x90befffa, 0xa4506ceb, 0xbef9a3f7, 0xc67178f2};

// SHA256 digest function - optimization pending
// takes a byte array of size 64 with 55 or fewer items, and writes
// hash to 32 item byte array.
// make sure to pass the input size as the second argument.
// example usage:
//	char data[64];
//  char hash[32];
//	data[0] = 'h';
//	data[1] = 'e';
//	data[2] = 'l';
//	data[3] = 'l';
//	data[4] = 'o';
//	digest(data, 5, hash);
//	// hash array now contains hash of 'hello'

static void digest(byte* data, uint inputLen, byte* hash) {
	/* init vars */
	union byte_int_converter h0, h1, h2, h3, h4, h5, h6, h7, temp;
	uint a, b, c, d, e, f, g, h, i, t1, t2, m[64] = {0};
	PAD(data, inputLen);
	/* init hash state */
	h0.val = 0x6a09e667;
	h1.val = 0xbb67ae85;
	h2.val = 0x3c6ef372;
	h3.val = 0xa54ff53a;
	h4.val = 0x510e527f;
	h5.val = 0x9b05688c;
	h6.val = 0x1f83d9ab;
	h7.val = 0x5be0cd19;
	/* transform */
#pragma unroll
	for (i = 0; i < 16; i++) {
		//m[i] = (data[mult_add(i,4,0)] << 24) | (data[mult_add(i,4,1)] << 16) | (data[mult_add(i,4,2)] << 8) | (data[mult_add(i,4,3)]);
		temp.bytes[3] = data[i*4];
		temp.bytes[2] = data[i*4+1];
		temp.bytes[1] = data[i*4+2];
		temp.bytes[0] = data[i*4+3];
		m[i] = temp.val;
	}
#pragma unroll
	for (i = 16; i < 64; ++i)
		m[i] = SIG1(m[i - 2]) + m[i - 7] + SIG0(m[i - 15]) + m[i - 16];
	a = h0.val;
	b = h1.val;
	c = h2.val;
	d = h3.val;
	e = h4.val;
	f = h5.val;
	g = h6.val;
	h = h7.val;
#pragma unroll
	for (i = 0; i < 64; ++i) {
		t1 = h + EP1(e) + CH(e,f,g) + K[i] + m[i];
		t2 = EP0(a) + MAJ(a,b,c);
		h = g;
		g = f;
		f = e;
		e = d + t1;
		d = c;
		c = b;
		b = a;
		a = t1 + t2;
	}
	h0.val += a;
	h1.val += b;
	h2.val += c;
	h3.val += d;
	h4.val += e;
	h5.val += f;
	h6.val += g;
	h7.val += h;
	/* finish */
	hash[0] = h0.bytes[3];
	hash[1] = h0.bytes[2];
	hash[2] = h0.bytes[1];
	hash[3] = h0.bytes[0];

	hash[4] = h1.bytes[3];
	hash[5] = h1.bytes[2];
	hash[6] = h1.bytes[1];
	hash[7] = h1.bytes[0];

	hash[8] = h2.bytes[3];
	hash[9] = h2.bytes[2];
	hash[10] = h2.bytes[1];
	hash[11] = h2.bytes[0];

	hash[12] = h3.bytes[3];
	hash[13] = h3.bytes[2];
	hash[14] = h3.bytes[1];
	hash[15] = h3.bytes[0];

	hash[16] = h4.bytes[3];
	hash[17] = h4.bytes[2];
	hash[18] = h4.bytes[1];
	hash[19] = h4.bytes[0];

	hash[20] = h5.bytes[3];
	hash[21] = h5.bytes[2];
	hash[22] = h5.bytes[1];
	hash[23] = h5.bytes[0];

	hash[24] = h6.bytes[3];
	hash[25] = h6.bytes[2];
	hash[26] = h6.bytes[1];
	hash[27] = h6.bytes[0];

	hash[28] = h7.bytes[3];
	hash[29] = h7.bytes[2];
	hash[30] = h7.bytes[1];
	hash[31] = h7.bytes[0];
}
