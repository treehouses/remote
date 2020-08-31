/*
 * ConnectBot: simple, powerful, open-source SSH client for Android
 * Copyright 2007 Kenny Root, Jeffrey Sharkey
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *	 http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.treehouses.remote.SSH

import android.util.Log
import com.trilead.ssh2.crypto.Base64
import com.trilead.ssh2.signature.DSASHA1Verify
import com.trilead.ssh2.signature.ECDSASHA2Verify
import com.trilead.ssh2.signature.Ed25519Verify
import com.trilead.ssh2.signature.RSASHA1Verify
import io.treehouses.remote.SSH.Ed25519Provider.Companion.insertIfNeeded
import io.treehouses.remote.SSH.Encryptor.decrypt
import io.treehouses.remote.SSH.beans.PubKeyBean
import net.i2p.crypto.eddsa.EdDSAPublicKey
import java.io.IOException
import java.security.*
import java.security.interfaces.DSAPublicKey
import java.security.interfaces.ECPublicKey
import java.security.interfaces.RSAPublicKey
import java.security.spec.InvalidKeySpecException
import java.security.spec.PKCS8EncodedKeySpec
import java.security.spec.X509EncodedKeySpec
import java.util.*

object PubKeyUtils {
    private const val TAG = "CB.PubkeyUtils"
    const val PKCS8_START = "-----BEGIN PRIVATE KEY-----"
    const val PKCS8_END = "-----END PRIVATE KEY-----"

    // Size in bytes of salt to use.
    private const val SALT_SIZE = 8

    // Number of iterations for password hashing. PKCS#5 recommends 1000
    private const val ITERATIONS = 1000
    fun formatKey(key: Key): String {
        val algo = key.algorithm
        val fmt = key.format
        val encoded = key.encoded
        return "Key[algorithm=" + algo + ", format=" + fmt +
                ", bytes=" + encoded.size + "]"
    }

    @Throws(java.lang.Exception::class)
    private fun encrypt(cleartext: ByteArray, secret: String): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        val ciphertext: ByteArray = Encryptor.encrypt(salt, ITERATIONS, secret, cleartext)
        val complete = ByteArray(salt.size + ciphertext.size)
        System.arraycopy(salt, 0, complete, 0, salt.size)
        System.arraycopy(ciphertext, 0, complete, salt.size, ciphertext.size)
        Arrays.fill(salt, 0x00.toByte())
        Arrays.fill(ciphertext, 0x00.toByte())
        return complete
    }

    @Throws(Exception::class)
    private fun decrypt(saltAndCiphertext: ByteArray, secret: String): ByteArray {
        val salt = ByteArray(SALT_SIZE)
        val ciphertext = ByteArray(saltAndCiphertext.size - salt.size)
        System.arraycopy(saltAndCiphertext, 0, salt, 0, salt.size)
        System.arraycopy(saltAndCiphertext, salt.size, ciphertext, 0, ciphertext.size)
        return decrypt(salt, ITERATIONS, secret, ciphertext)
    }

    @Throws(java.lang.Exception::class)
    fun getEncodedPrivate(pk: PrivateKey, secret: String?): ByteArray {
        val encoded = pk.encoded
        return if (secret.isNullOrEmpty()) encoded else encrypt(pk.encoded, secret)
    }

    @Throws(NoSuchAlgorithmException::class, InvalidKeySpecException::class)
    fun decodeKey(encoded: ByteArray?, keyType: String?, type: String): Key {
        val kf = KeyFactory.getInstance(keyType)
        return when(type) {
            "private" -> kf.generatePrivate(PKCS8EncodedKeySpec(encoded))
            else -> kf.generatePublic(X509EncodedKeySpec(encoded))
        }
    }

    @Throws(Exception::class)
    fun decodePrivate(encoded: ByteArray, keyType: String?, secret: String?): PrivateKey {
        return if (!secret.isNullOrEmpty()) decodeKey(decrypt(encoded, secret), keyType, "private") as PrivateKey else decodeKey(encoded, keyType, "private") as PrivateKey
    }

    fun getBitStrength(encoded : ByteArray, keyType: String) : Int {
        val pubKey = decodeKey(encoded, keyType, "public");
        return when (keyType) {
            PubKeyBean.KEY_TYPE_RSA -> (pubKey as RSAPublicKey).modulus.bitLength();
            PubKeyBean.KEY_TYPE_DSA -> 1024;
            PubKeyBean.KEY_TYPE_EC -> (pubKey as ECPublicKey).params.curve.field.fieldSize;
            PubKeyBean.KEY_TYPE_ED25519 -> 256;
            else -> { 0; }
        }
    }

    //    static String getAlgorithmForOid(String oid) throws NoSuchAlgorithmException {
    //        if ("1.2.840.10045.2.1".equals(oid)) {
    //            return "EC";
    //        } else if ("1.2.840.113549.1.1.1".equals(oid)) {
    //            return "RSA";
    //        } else if ("1.2.840.10040.4.1".equals(oid)) {
    //            return "DSA";
    //        } else {
    //            throw new NoSuchAlgorithmException("Unknown algorithm OID " + oid);
    //        }
    //    }
    //
    //    static String getOidFromPkcs8Encoded(byte[] encoded) throws NoSuchAlgorithmException {
    //        if (encoded == null) {
    //            throw new NoSuchAlgorithmException("encoding is null");
    //        }
    //
    //        try {
    //            SimpleDERReader reader = new SimpleDERReader(encoded);
    //            reader.resetInput(reader.readSequenceAsByteArray());
    //            reader.readInt();
    //            reader.resetInput(reader.readSequenceAsByteArray());
    //            return reader.readOid();
    //        } catch (IOException e) {
    //            Log.w(TAG, "Could not read OID", e);
    //            throw new NoSuchAlgorithmException("Could not read key", e);
    //        }
    //    }
    //    static BigInteger getRSAPublicExponentFromPkcs8Encoded(byte[] encoded) throws InvalidKeySpecException {
    //        if (encoded == null) {
    //            throw new InvalidKeySpecException("encoded key is null");
    //        }
    //
    //        try {
    //            SimpleDERReader reader = new SimpleDERReader(encoded);
    //            reader.resetInput(reader.readSequenceAsByteArray());
    //            if (!reader.readInt().equals(BigInteger.ZERO)) {
    //                throw new InvalidKeySpecException("PKCS#8 is not version 0");
    //            }
    //
    //            reader.readSequenceAsByteArray();  // OID sequence
    //            reader.resetInput(reader.readOctetString());  // RSA key bytes
    //            reader.resetInput(reader.readSequenceAsByteArray());  // RSA key sequence
    //
    //            if (!reader.readInt().equals(BigInteger.ZERO)) {
    //                throw new InvalidKeySpecException("RSA key is not version 0");
    //            }
    //
    //            reader.readInt();  // modulus
    //            return reader.readInt();  // public exponent
    //        } catch (IOException e) {
    //            Log.w(TAG, "Could not read public exponent", e);
    //            throw new InvalidKeySpecException("Could not read key", e);
    //        }
    //    }
    //    public static KeyPair convertToKeyPair(PubKeyBean keybean, String password) throws BadPasswordException {
    ////        if (PubkeyDatabase.KEY_TYPE_IMPORTED.equals(keybean.getType())) {
    ////            // load specific key using pem format
    ////            try {
    ////                return PEMDecoder.decode(new String(keybean.getPrivateKey(), "UTF-8").toCharArray(), password);
    ////            } catch (Exception e) {
    ////                Log.e(TAG, "Cannot decode imported key", e);
    ////                throw new BadPasswordException();
    ////            }
    ////        } else {
    //            // load using internal generated format
    //            try {
    //                PrivateKey privKey = PubKeyUtils.decodePrivate(keybean.getPrivateKey(), keybean.getType(), password);
    //                PublicKey pubKey = PubKeyUtils.decodePublic(keybean.getPublicKey(), keybean.getType());
    //                Log.d(TAG, "Unlocked key " + PubKeyUtils.formatKey(pubKey));
    //
    //                return new KeyPair(pubKey, privKey);
    //            } catch (Exception e) {
    //                Log.e(TAG, "Cannot decode pubkey from database", e);
    //                throw new BadPasswordException();
    //            }
    ////        }
    //    }
    //    public static KeyPair recoverKeyPair(byte[] encoded) throws NoSuchAlgorithmException,
    //            InvalidKeySpecException {
    //        final String algo = getAlgorithmForOid(getOidFromPkcs8Encoded(encoded));
    //
    //        final KeySpec privKeySpec = new PKCS8EncodedKeySpec(encoded);
    //
    //        final KeyFactory kf = KeyFactory.getInstance(algo);
    //        final PrivateKey priv = kf.generatePrivate(privKeySpec);
    //
    //        return new KeyPair(recoverPublicKey(kf, priv), priv);
    //    }
    //
    //    static PublicKey recoverPublicKey(KeyFactory kf, PrivateKey priv)
    //            throws NoSuchAlgorithmException, InvalidKeySpecException {
    //        if (priv instanceof RSAPrivateCrtKey) {
    //            RSAPrivateCrtKey rsaPriv = (RSAPrivateCrtKey) priv;
    //            return kf.generatePublic(new RSAPublicKeySpec(rsaPriv.getModulus(), rsaPriv
    //                    .getPublicExponent()));
    //        } else if (priv instanceof RSAPrivateKey) {
    //            BigInteger publicExponent = getRSAPublicExponentFromPkcs8Encoded(priv.getEncoded());
    //            RSAPrivateKey rsaPriv = (RSAPrivateKey) priv;
    //            return kf.generatePublic(new RSAPublicKeySpec(rsaPriv.getModulus(), publicExponent));
    //        } else if (priv instanceof DSAPrivateKey) {
    //            DSAPrivateKey dsaPriv = (DSAPrivateKey) priv;
    //            DSAParams params = dsaPriv.getParams();
    //
    //            // Calculate public key Y
    //            BigInteger y = params.getG().modPow(dsaPriv.getX(), params.getP());
    //
    //            return kf.generatePublic(new DSAPublicKeySpec(y, params.getP(), params.getQ(), params
    //                    .getG()));
    //        } else if (priv instanceof ECPrivateKey) {
    //            ECPrivateKey ecPriv = (ECPrivateKey) priv;
    //            ECParameterSpec params = ecPriv.getParams();
    //
    //            // Calculate public key Y
    //            ECPoint generator = params.getGenerator();
    //            BigInteger[] wCoords = EcCore.multiplyPointA(new BigInteger[] { generator.getAffineX(),
    //                    generator.getAffineY() }, ecPriv.getS(), params);
    //            ECPoint w = new ECPoint(wCoords[0], wCoords[1]);
    //
    //            return kf.generatePublic(new ECPublicKeySpec(w, params));
    //        } else {
    //            throw new NoSuchAlgorithmException("Key type must be RSA, DSA, or EC");
    //        }
    //    }
    /*
     * OpenSSH compatibility methods
     */
    fun convertToOpenSSHFormat(pk: PublicKey, nickName: String) : String {
        Log.e("PUBKEYORIG", String(Base64.encode(pk.encoded)))
        return when (pk) {
            is RSAPublicKey -> "ssh-rsa ${String(Base64.encode(RSASHA1Verify.encodeSSHRSAPublicKey(pk)))}$nickName"
            is DSAPublicKey -> "ssh-dss ${String(Base64.encode(DSASHA1Verify.encodeSSHDSAPublicKey(pk)))}$nickName"
            is ECPublicKey -> {
                val keyType = ECDSASHA2Verify.getCurveName(pk.params.curve.field.fieldSize)
                val data = String(Base64.encode(ECDSASHA2Verify.encodeSSHECDSAPublicKey(pk)))
                "${ECDSASHA2Verify.ECDSA_SHA2_PREFIX} $keyType $data $nickName"
            }
            is EdDSAPublicKey -> "${Ed25519Verify.ED25519_ID} ${String(Base64.encode(Ed25519Verify.encodeSSHEd25519PublicKey(pk)))} $nickName"
            else -> throw InvalidKeyException("Unknown Key Type")
        }
    }
    /*
     * OpenSSH compatibility methods
     */
    /**
     * @param pair KeyPair to convert to an OpenSSH public key
     * @return OpenSSH-encoded pubkey
     */
    fun extractOpenSSHPublic(pair: KeyPair?): ByteArray? {
        return try {
            when (val pubKey = pair?.public) {
                is RSAPublicKey -> RSASHA1Verify.encodeSSHRSAPublicKey(pubKey)
                is DSAPublicKey -> DSASHA1Verify.encodeSSHDSAPublicKey(pubKey)
                is ECPublicKey -> ECDSASHA2Verify.encodeSSHECDSAPublicKey(pubKey)
                is EdDSAPublicKey -> Ed25519Verify.encodeSSHEd25519PublicKey(pubKey)
                else -> null
            }
        } catch (e: IOException) {
            null
        }
    } //    public static String exportPEM(PrivateKey key, String secret) throws NoSuchAlgorithmException, InvalidParameterSpecException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, IOException {

    //        StringBuilder sb = new StringBuilder();
    //
    //        byte[] data = key.getEncoded();
    //
    //        sb.append(PKCS8_START);
    //        sb.append('\n');
    //
    //        if (secret != null) {
    //            byte[] salt = new byte[8];
    //            SecureRandom random = new SecureRandom();
    //            random.nextBytes(salt);
    //
    //            PBEParameterSpec defParams = new PBEParameterSpec(salt, 1);
    //            AlgorithmParameters params = AlgorithmParameters.getInstance(key.getAlgorithm());
    //
    //            params.init(defParams);
    //
    //            PBEKeySpec pbeSpec = new PBEKeySpec(secret.toCharArray());
    //
    //            SecretKeyFactory keyFact = SecretKeyFactory.getInstance(key.getAlgorithm());
    //            Cipher cipher = Cipher.getInstance(key.getAlgorithm());
    //            cipher.init(Cipher.WRAP_MODE, keyFact.generateSecret(pbeSpec), params);
    //
    //            byte[] wrappedKey = cipher.wrap(key);
    //
    //            EncryptedPrivateKeyInfo pinfo = new EncryptedPrivateKeyInfo(params, wrappedKey);
    //
    //            data = pinfo.getEncoded();
    //
    //            sb.append("Proc-Type: 4,ENCRYPTED\n");
    //            sb.append("DEK-Info: DES-EDE3-CBC,");
    //            sb.append(encodeHex(salt));
    //            sb.append("\n\n");
    //        }
    //
    //        int i = sb.length();
    //        sb.append(Base64.encode(data));
    //        for (i += 63; i < sb.length(); i += 64) {
    //            sb.insert(i, "\n");
    //        }
    //
    //        sb.append('\n');
    //        sb.append(PKCS8_END);
    //        sb.append('\n');
    //
    //        return sb.toString();
    //    }
    //    private static final char[] HEX_DIGITS = { '0', '1', '2', '3', '4', '5', '6',
    //            '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
    //    static String encodeHex(byte[] bytes) {
    //        final char[] hex = new char[bytes.length * 2];
    //
    //        int i = 0;
    //        for (byte b : bytes) {
    //            hex[i++] = HEX_DIGITS[(b >> 4) & 0x0f];
    //            hex[i++] = HEX_DIGITS[b & 0x0f];
    //        }
    //
    //        return String.valueOf(hex);
    //    }
    //
    //    public static class BadPasswordException extends Exception {
    //    }
    init {
        insertIfNeeded()
    }
}