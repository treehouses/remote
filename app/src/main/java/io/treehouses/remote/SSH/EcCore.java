package io.treehouses.remote.SSH;


import java.math.BigInteger;
import java.security.spec.ECFieldFp;
import java.security.spec.ECParameterSpec;

/**
 * This class implements the basic EC operations such as point addition and
 * doubling and point multiplication. Only NSA Suite B / NIST curves are
 * supported.
 *
 * Todo:
 *  - Add (more) comments - Performance optimizations - Cleanup ASN.1 code,
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
 *
 */
public final class EcCore {

    private static final BigInteger THREE = BigInteger.valueOf(3);

    private static BigInteger[] doublePointA(BigInteger[] P,
                                             ECParameterSpec params) {
        final BigInteger p = ((ECFieldFp) params.getCurve().getField()).getP();
        final BigInteger a = params.getCurve().getA();

        if (P[0] == null || P[1] == null) return P;

        BigInteger d = (P[0].pow(2).multiply(THREE).add(a)).multiply(P[1]
                .shiftLeft(1).modInverse(p));
        BigInteger[] R = new BigInteger[2];
        R[0] = d.pow(2).subtract(P[0].shiftLeft(1)).mod(p);
        R[1] = d.multiply(P[0].subtract(R[0])).subtract(P[1]).mod(p);

        return R;
    }

    private static BigInteger[] addPointsA(BigInteger[] P1, BigInteger[] P2,
                                           ECParameterSpec params) {
        final BigInteger p = ((ECFieldFp) params.getCurve().getField()).getP();

        if (P2[0] == null || P2[1] == null) return P1;

        if (P1[0] == null || P1[1] == null) return P2;

        BigInteger d = (P2[1].subtract(P1[1])).multiply((P2[0].subtract(P1[0]))
                .modInverse(p));
        BigInteger[] R = new BigInteger[2];
        R[0] = d.pow(2).subtract(P1[0]).subtract(P2[0]).mod(p);
        R[1] = d.multiply(P1[0].subtract(R[0])).subtract(P1[1]).mod(p);

        return R;
    }

    public static BigInteger[] multiplyPointA(BigInteger[] P, BigInteger k,
                                              ECParameterSpec params) {
        BigInteger[] Q = new BigInteger[] {null, null};

        for (int i = k.bitLength() - 1; i >= 0; i--) {
            Q = doublePointA(Q, params);
            if (k.testBit(i)) Q = addPointsA(Q, P, params);
        }

        return Q;
    }

}
