package io.treehouses.remote.SSH;

import net.i2p.crypto.eddsa.KeyFactory;
import net.i2p.crypto.eddsa.KeyPairGenerator;

import java.security.Provider;
import java.security.Security;

public class Ed25519Provider extends Provider {
    private static final String NAME = "Ed25519Provider";

    private static final Object sInitLock = new Object();
    private static boolean sInitialized = false;

    /**
     * Constructs a new instance of the Ed25519Provider.
     */
    public Ed25519Provider() {
        super(NAME, 1.0, "Provider wrapping eddsa classes");

        put("KeyPairGenerator.Ed25519", KeyPairGenerator.class.getName());
        put("KeyFactory.Ed25519", KeyFactory.class.getName());
    }

    public static void insertIfNeeded() {
        synchronized (sInitLock) {
            if (!sInitialized) {
                Security.addProvider(new Ed25519Provider());
                sInitialized = true;
            }
        }
    }
}