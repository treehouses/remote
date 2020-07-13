package io.treehouses.remote.SSH

import net.i2p.crypto.eddsa.KeyFactory
import net.i2p.crypto.eddsa.KeyPairGenerator
import java.security.Provider
import java.security.Security

class Ed25519Provider : Provider(NAME, 1.0, "Provider wrapping eddsa classes") {
    companion object {
        private const val NAME = "Ed25519Provider"
        private val sInitLock = Any()
        private var sInitialized = false
        @JvmStatic
        fun insertIfNeeded() {
            synchronized(sInitLock) {
                if (!sInitialized) {
                    Security.addProvider(Ed25519Provider())
                    sInitialized = true
                }
            }
        }
    }

    /**
     * Constructs a new instance of the Ed25519Provider.
     */
    init {
        put("KeyPairGenerator.Ed25519", KeyPairGenerator::class.java.name)
        put("KeyFactory.Ed25519", KeyFactory::class.java.name)
    }
}