package com.tangqiang.core;

import com.android.ddmlib.IShellOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 执行shell时候输出
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public class LogOutputReceiver implements IShellOutputReceiver {
    private final Logger log = LoggerFactory.getLogger("Shell execute");

    public void addOutput(byte[] data, int offset, int length) {
        String message = new String(data, offset, length);
        String[] mes = message.split("\n");
        for (int i = 0; i < mes.length; ++i) {
            String line = mes[i];
            log.info(line);
        }
    }

    public void flush() {
    }

    public boolean isCancelled() {
        return false;
    }
}
