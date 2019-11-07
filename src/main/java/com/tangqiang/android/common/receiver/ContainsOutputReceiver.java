/*
 * Copyright (c) 2019. grgbanking all rights reserved.
 */

package com.tangqiang.android.common.receiver;


import com.android.ddmlib.IShellOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 命令包含
 *
 * @author tqiang
 * @date 2019-10-13 23:13
 */
public class ContainsOutputReceiver implements IShellOutputReceiver {
    private final Logger log = LoggerFactory.getLogger("Shell execute:");
    private String key = "-";
    private boolean contains = false;

    public ContainsOutputReceiver(String key) {
        this.key = key;
    }

    @Override
    public void addOutput(byte[] data, int offset, int length) {
        String message = new String(data, offset, length);
        String[] mes = message.split("\n");

        for (int i = 0; i < mes.length; ++i) {
            String line = mes[i];
            contains = contains || line.contains(key);
            this.log.debug(line);
        }
    }

    public boolean contains() {
        return contains;
    }

    @Override
    public void flush() {
    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}
