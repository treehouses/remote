package io.treehouses.remote.SSH

import java.math.BigInteger
import java.security.spec.ECFieldFp
import java.security.spec.ECParameterSpec

/**
 * This class implements the basic EC operations such as point addition and
 * doubling and point multiplication. Only NSA Suite B / NIST curves are
 * supported.
 *
 * Todo:
 * - Add (more) comments - Performance optimizations - Cleanup ASN.1 code,
 * possibly replace with own impl - ...
 *
 * References:
 *
 * [1] Software Implementation of the NIST Elliptic Curves Over Prime Fields, M.
 * Brown et al. [2] Efficient elliptic curve exponentiation using mixed
 * coordinates, H. Cohen et al. [3] SEC 1: Elliptic Curve Cryptography. [4]
 * Guide to Elliptic Curve Cryptography, D. Hankerson et al., Springer.
 *
 * @author martclau@gmail.com
 */
object EcCore {
    private val THREE = BigInteger.valueOf(3)
    private fun doublePointA(P: Array<BigInteger?>,
                             params: ECParameterSpec): Array<BigInteger?> {
        val p = (params.curve.field as ECFieldFp).p
        val a = params.curve.a
        if (P[0] == null || P[1] == null) return P
        val d = P[0]!!.pow(2).multiply(THREE).add(a).multiply(P[1]!!
                .shiftLeft(1).modInverse(p))
        val R = arrayOfNulls<BigInteger>(2)
        R[0] = d.pow(2).subtract(P[0]!!.shiftLeft(1)).mod(p)
        R[1] = d.multiply(P[0]!!.subtract(R[0])).subtract(P[1]).mod(p)
        return R
    }

    private fun addPointsA(P1: Array<BigInteger?>, P2: Array<BigInteger?>,
                           params: ECParameterSpec): Array<BigInteger?> {
        val p = (params.curve.field as ECFieldFp).p
        if (P2[0] == null || P2[1] == null) return P1
        if (P1[0] == null || P1[1] == null) return P2
        val d = P2[1]!!.subtract(P1[1]).multiply(P2[0]!!.subtract(P1[0])
                .modInverse(p))
        val R = arrayOfNulls<BigInteger>(2)
        R[0] = d.pow(2).subtract(P1[0]).subtract(P2[0]).mod(p)
        R[1] = d.multiply(P1[0]!!.subtract(R[0])).subtract(P1[1]).mod(p)
        return R
    }

    fun multiplyPointA(P: Array<BigInteger?>, k: BigInteger,
                       params: ECParameterSpec): Array<BigInteger?> {
        var Q = arrayOf<BigInteger?>(null, null)
        for (i in k.bitLength() - 1 downTo 0) {
            Q = doublePointA(Q, params)
            if (k.testBit(i)) Q = addPointsA(Q, P, params)
        }
        return Q
    }
}