package com.godfather59.quransunnah.crypto

// Pure-Kotlin SHA-256 and SHA-1 (common code, no platform crypto needed).
// Verified against Dart crypto vectors in HashTest, plus a full-manifest
// cross-check (every bundled file) in IntegrityTest.

fun ByteArray.toHex(): String = joinToString("") { b ->
    val v = b.toInt() and 0xFF
    (if (v < 16) "0" else "") + v.toString(16)
}

private fun padMessage(input: ByteArray): ByteArray {
    val bitLen = input.size.toLong() * 8L
    // 0x80 byte, then zeros, then 64-bit big-endian length. Total ≡ 0 (mod 64).
    val withOne = input.size + 1
    val zeroCount = ((56 - (withOne % 64)) + 64) % 64
    val out = ByteArray(withOne + zeroCount + 8)
    input.copyInto(out)
    out[input.size] = 0x80.toByte()
    for (i in 0 until 8) {
        out[out.size - 1 - i] = (bitLen ushr (8 * i)).toByte()
    }
    return out
}

private fun Int.rotr(n: Int): Int = (this ushr n) or (this shl (32 - n))

object Sha256 {
    private val K = intArrayOf(
        0x428a2f98.toInt(), 0x71374491.toInt(), 0xb5c0fbcf.toInt(), 0xe9b5dba5.toInt(),
        0x3956c25b.toInt(), 0x59f111f1.toInt(), 0x923f82a4.toInt(), 0xab1c5ed5.toInt(),
        0xd807aa98.toInt(), 0x12835b01.toInt(), 0x243185be.toInt(), 0x550c7dc3.toInt(),
        0x72be5d74.toInt(), 0x80deb1fe.toInt(), 0x9bdc06a7.toInt(), 0xc19bf174.toInt(),
        0xe49b69c1.toInt(), 0xefbe4786.toInt(), 0x0fc19dc6.toInt(), 0x240ca1cc.toInt(),
        0x2de92c6f.toInt(), 0x4a7484aa.toInt(), 0x5cb0a9dc.toInt(), 0x76f988da.toInt(),
        0x983e5152.toInt(), 0xa831c66d.toInt(), 0xb00327c8.toInt(), 0xbf597fc7.toInt(),
        0xc6e00bf3.toInt(), 0xd5a79147.toInt(), 0x06ca6351.toInt(), 0x14292967.toInt(),
        0x27b70a85.toInt(), 0x2e1b2138.toInt(), 0x4d2c6dfc.toInt(), 0x53380d13.toInt(),
        0x650a7354.toInt(), 0x766a0abb.toInt(), 0x81c2c92e.toInt(), 0x92722c85.toInt(),
        0xa2bfe8a1.toInt(), 0xa81a664b.toInt(), 0xc24b8b70.toInt(), 0xc76c51a3.toInt(),
        0xd192e819.toInt(), 0xd6990624.toInt(), 0xf40e3585.toInt(), 0x106aa070.toInt(),
        0x19a4c116.toInt(), 0x1e376c08.toInt(), 0x2748774c.toInt(), 0x34b0bcb5.toInt(),
        0x391c0cb3.toInt(), 0x4ed8aa4a.toInt(), 0x5b9cca4f.toInt(), 0x682e6ff3.toInt(),
        0x748f82ee.toInt(), 0x78a5636f.toInt(), 0x84c87814.toInt(), 0x8cc70208.toInt(),
        0x90befffa.toInt(), 0xa4506ceb.toInt(), 0xbef9a3f7.toInt(), 0xc67178f2.toInt(),
    )

    fun digest(input: ByteArray): ByteArray {
        var h0 = 0x6a09e667.toInt()
        var h1 = 0xbb67ae85.toInt()
        var h2 = 0x3c6ef372.toInt()
        var h3 = 0xa54ff53a.toInt()
        var h4 = 0x510e527f.toInt()
        var h5 = 0x9b05688c.toInt()
        var h6 = 0x1f83d9ab.toInt()
        var h7 = 0x5be0cd19.toInt()

        val msg = padMessage(input)
        val w = IntArray(64)
        var off = 0
        while (off < msg.size) {
            for (i in 0 until 16) {
                w[i] = ((msg[off + i * 4].toInt() and 0xFF) shl 24) or
                    ((msg[off + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((msg[off + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (msg[off + i * 4 + 3].toInt() and 0xFF)
            }
            for (i in 16 until 64) {
                val s0 = w[i - 15].rotr(7) xor w[i - 15].rotr(18) xor (w[i - 15] ushr 3)
                val s1 = w[i - 2].rotr(17) xor w[i - 2].rotr(19) xor (w[i - 2] ushr 10)
                w[i] = w[i - 16] + s0 + w[i - 7] + s1
            }
            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            var f = h5
            var g = h6
            var h = h7
            for (i in 0 until 64) {
                val s1 = e.rotr(6) xor e.rotr(11) xor e.rotr(25)
                val ch = (e and f) xor ((e.inv()) and g)
                val t1 = h + s1 + ch + K[i] + w[i]
                val s0 = a.rotr(2) xor a.rotr(13) xor a.rotr(22)
                val maj = (a and b) xor (a and c) xor (b and c)
                val t2 = s0 + maj
                h = g
                g = f
                f = e
                e = d + t1
                d = c
                c = b
                b = a
                a = t1 + t2
            }
            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            h5 += f
            h6 += g
            h7 += h
            off += 64
        }

        val out = ByteArray(32)
        val hs = intArrayOf(h0, h1, h2, h3, h4, h5, h6, h7)
        for (i in hs.indices) {
            out[i * 4] = (hs[i] ushr 24).toByte()
            out[i * 4 + 1] = (hs[i] ushr 16).toByte()
            out[i * 4 + 2] = (hs[i] ushr 8).toByte()
            out[i * 4 + 3] = hs[i].toByte()
        }
        return out
    }

    fun hex(input: ByteArray): String = digest(input).toHex()
}

object Sha1 {
    fun digest(input: ByteArray): ByteArray {
        var h0 = 0x67452301.toInt()
        var h1 = 0xEFCDAB89.toInt()
        var h2 = 0x98BADCFE.toInt()
        var h3 = 0x10325476.toInt()
        var h4 = 0xC3D2E1F0.toInt()

        val msg = padMessage(input)
        val w = IntArray(80)
        var off = 0
        while (off < msg.size) {
            for (i in 0 until 16) {
                w[i] = ((msg[off + i * 4].toInt() and 0xFF) shl 24) or
                    ((msg[off + i * 4 + 1].toInt() and 0xFF) shl 16) or
                    ((msg[off + i * 4 + 2].toInt() and 0xFF) shl 8) or
                    (msg[off + i * 4 + 3].toInt() and 0xFF)
            }
            for (i in 16 until 80) {
                w[i] = (w[i - 3] xor w[i - 8] xor w[i - 14] xor w[i - 16]).rotateLeft(1)
            }
            var a = h0
            var b = h1
            var c = h2
            var d = h3
            var e = h4
            for (i in 0 until 80) {
                val (f, k) = when (i) {
                    in 0 until 20 -> Pair((b and c) or ((b.inv()) and d), 0x5A827999.toInt())
                    in 20 until 40 -> Pair(b xor c xor d, 0x6ED9EBA1.toInt())
                    in 40 until 60 -> Pair((b and c) or (b and d) or (c and d), 0x8F1BBCDC.toInt())
                    else -> Pair(b xor c xor d, 0xCA62C1D6.toInt())
                }
                val temp = a.rotateLeft(5) + f + e + k + w[i]
                e = d
                d = c
                c = b.rotateLeft(30)
                b = a
                a = temp
            }
            h0 += a
            h1 += b
            h2 += c
            h3 += d
            h4 += e
            off += 64
        }

        val out = ByteArray(20)
        val hs = intArrayOf(h0, h1, h2, h3, h4)
        for (i in hs.indices) {
            out[i * 4] = (hs[i] ushr 24).toByte()
            out[i * 4 + 1] = (hs[i] ushr 16).toByte()
            out[i * 4 + 2] = (hs[i] ushr 8).toByte()
            out[i * 4 + 3] = hs[i].toByte()
        }
        return out
    }

    fun hex(input: ByteArray): String = digest(input).toHex()
}

private fun Int.rotateLeft(n: Int): Int = (this shl n) or (this ushr (32 - n))

/** git blob hash: sha1("blob <len>\0" + content). Matches integrity_manifest.json. */
fun gitBlobSha1Hex(content: ByteArray): String {
    val header = ("blob " + content.size).encodeToByteArray() + byteArrayOf(0)
    return Sha1.hex(header + content)
}
