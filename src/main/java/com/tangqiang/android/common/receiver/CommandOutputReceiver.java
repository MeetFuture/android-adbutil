package com.tangqiang.android.common.receiver;


import com.android.ddmlib.IShellOutputReceiver;

/**
 * 命令输出处理
 *
 * @author tqiang
 * @date 2019-10-13 23:13
 */
public class CommandOutputReceiver implements IShellOutputReceiver {
    private final StringBuilder builder = new StringBuilder();

    @Override
    public void flush() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }

    @Override
    public void addOutput(byte[] data, int offset, int length) {
        String message = new String(data, offset, length);
        this.builder.append(message);
    }

    @Override
    public String toString() {
        return this.builder.toString();
    }
}
