package io.treehouses.remote.ssh.beans

import android.content.Context
import io.treehouses.remote.R
import io.treehouses.remote.ssh.PubKeyUtils
import java.security.NoSuchAlgorithmException
import java.security.spec.InvalidKeySpecException

/**
 * @author Kenny Root
 */
class PubKeyBean(var nickname: String = "",
                 var type: String = "RSA", var privateKey: ByteArray? = null, var publicKey: ByteArray? = null) {
    /* Database fields */
//    var id: Long = 0
//    var nickname: String = ""
//    var type: String = "RSA"
//    private var privateKey: ByteArray? = null
//    private var publicKey: ByteArray? = null
    var isEncrypted = false
//    var isStartup = false
    var isConfirmUse = false
    var lifetime = 10000

    /* Transient values */
//    @Transient
//    var isUnlocked = false
//
//    @Transient
//    var unlockedPrivate: Any? = null

    @Transient
    private var bits: Int? = null

//    fun setPrivateKey(privateKey: ByteArray?) {
//        if (privateKey == null) this.privateKey = null else this.privateKey = privateKey.clone()
//    }
//
//    fun getPrivateKey(): ByteArray? {
//        return if (privateKey == null) null else privateKey!!.clone()
//    }
//
//    fun setPublicKey(encoded: ByteArray?) {
//        publicKey = encoded?.clone()
//    }
//
//    fun getPublicKey(): ByteArray? {
//        return if (publicKey == null) null else publicKey!!.clone()
//    }

    fun getDescription(context: Context): String {
        if (bits == null) {
            try {
                bits = PubKeyUtils.getBitStrength(publicKey!!, type)
            } catch (ignored: NoSuchAlgorithmException) {
            } catch (ignored: InvalidKeySpecException) {
            }
        }
        val res = context.resources
        val sb = StringBuilder()
        when (type) {
            KEY_TYPE_RSA -> sb.append(res.getString(R.string.key_type_rsa_bits, bits))
            KEY_TYPE_DSA -> sb.append(res.getString(R.string.key_type_dsa_bits, 1024))
            KEY_TYPE_EC -> sb.append(res.getString(R.string.key_type_ec_bits, bits))
            KEY_TYPE_ED25519 -> sb.append(res.getString(R.string.key_type_ed25519))
            else -> sb.append(res.getString(R.string.key_type_unknown))
        }
//        if (isEncrypted) {
//            sb.append(' ')
//            sb.append(res.getString(R.string.key_attribute_encrypted))
//        }
        return sb.toString()
    } //

    //    public boolean changePassword(String oldPassword, String newPassword) throws Exception {
    //        PrivateKey priv;
    //
    //        try {
    //            priv = PubkeyUtils.decodePrivate(getPrivateKey(), getType(), oldPassword);
    //        } catch (Exception e) {
    //            return false;
    //        }
    //
    //        setPrivateKey(PubkeyUtils.getEncodedPrivate(priv, newPassword));
    //        setEncrypted(newPassword.length() > 0);
    //
    //        return true;
    //    }
    companion object {
        const val beanName = "pubkey"
        const val KEY_TYPE_RSA = "RSA"
        const val KEY_TYPE_DSA = "DSA"
        const val KEY_TYPE_EC = "EC"
        const val KEY_TYPE_ED25519 = "ED25519"
    }
}