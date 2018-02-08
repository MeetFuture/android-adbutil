package com.tangqiang.adblib.core.expand;

import com.tangqiang.adblib.core.AdbCrypto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

/**
 * This function loads a keypair from the specified files if one exists, and if not, it creates a new keypair and saves it in the specified files
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public class AdbCryptoBuilder {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AdbCrypto build(String pubKeyFile, String privKeyFile) throws Exception {
        File pub = new File(pubKeyFile);
        File priv = new File(privKeyFile);
        AdbCrypto c = null;

        if (pub.exists() && priv.exists()) {
            try {
                c = AdbCrypto.loadAdbKeyPair(new AdbBase64Impl(), priv, pub);
                logger.info("Loaded existing keypair");
            } catch (Exception e) {
                logger.error("loadAdbKeyPair error !", e);
            }
        }

        if (c == null) {
            c = AdbCrypto.generateAdbKeyPair(new AdbBase64Impl());
            c.saveAdbKeyPair(priv, pub);
            logger.info("Generated new keypair");
        }
        return c;
    }

}
