package io.treehouses.remote.ssh

import com.trilead.ssh2.crypto.Base64
import com.trilead.ssh2.signature.DSASHA1Verify
import com.trilead.ssh2.signature.ECDSASHA2Verify
import com.trilead.ssh2.signature.Ed25519Verify
import com.trilead.ssh2.signature.RSASHA1Verify
import io.treehouses.remote.ssh.Ed25519Provider.Companion.insertIfNeeded
import io.treehouses.remote.ssh.Encryptor.decrypt
import io.treehouses.remote.ssh.beans.PubKeyBean
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

    private const val SALT_SIZE = 8
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
        val pubKey = decodeKey(encoded, keyType, "public")
        return when (keyType) {
            PubKeyBean.KEY_TYPE_RSA -> (pubKey as RSAPublicKey).modulus.bitLength()
            PubKeyBean.KEY_TYPE_DSA -> 1024
            PubKeyBean.KEY_TYPE_EC -> (pubKey as ECPublicKey).params.curve.field.fieldSize
            PubKeyBean.KEY_TYPE_ED25519 -> 256
            else -> { 0; }
        }
    }

    fun convertToOpenSSHFormat(pk: PublicKey, nickName: String): String {
        return when (pk) {
            is RSAPublicKey -> {
                val rsaKey = String(Base64.encode(RSASHA1Verify.get().encodePublicKey(pk)))
                "ssh-rsa $rsaKey $nickName"
            }
            is DSAPublicKey -> {
                val dsaKey = String(Base64.encode(DSASHA1Verify.get().encodePublicKey(pk)))
                "ssh-dss $dsaKey $nickName"
            }
            is ECPublicKey -> {
                val verifier = ECDSASHA2Verify.getVerifierForKey(pk)
                val keyType = verifier.keyFormat
                val data = String(Base64.encode(verifier.encodePublicKey(pk)))
                "$keyType $data $nickName"
            }
            is EdDSAPublicKey -> {
                val edKey = String(Base64.encode(Ed25519Verify.get().encodePublicKey(pk)))
                "${Ed25519Verify.get().keyFormat} $edKey $nickName"
            }
            else -> throw InvalidKeyException("Unknown Key Type")
        }
    }

    fun extractOpenSSHPublic(pair: KeyPair?): ByteArray? {
        return try {
            when (val pubKey = pair?.public) {
                is RSAPublicKey -> RSASHA1Verify.get().encodePublicKey(pubKey)
                is DSAPublicKey -> DSASHA1Verify.get().encodePublicKey(pubKey)
                is ECPublicKey -> ECDSASHA2Verify.getVerifierForKey(pubKey).encodePublicKey(pubKey)
                is EdDSAPublicKey -> Ed25519Verify.get().encodePublicKey(pubKey)
                else -> null
            }
        } catch (e: IOException) {
            null
        }
    }
    init {
        insertIfNeeded()
    }
}
