package com.tangqiang.android.common.receiver;

import com.android.ddmlib.IShellOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令日志输出
 */
public class LogOutputReceiver implements IShellOutputReceiver {
    private final Logger log = LoggerFactory.getLogger("Shell execute:");


    @Override
    public void addOutput(byte[] data, int offset, int length) {
        String message = new String(data, offset, length);
        String[] mes = message.split("\n");

        for (int i = 0; i < mes.length; ++i) {
            String line = mes[i];
            this.log.debug(line);
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
