package com.tangqiang.adblib.core.expand;

import com.tangqiang.adblib.core.AdbBase64;
import org.apache.commons.codec.binary.Base64;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public class AdbBase64Impl implements AdbBase64 {

    @Override
    public String encodeToString(byte[] arg0) {
        return Base64.encodeBase64String(arg0);
    }
}
