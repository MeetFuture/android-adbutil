package com.tangqiang.adblib.core;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public enum AdbCommand {
    /**
     * OPEN is the open stream message. It is sent to open
     * a new stream on the target device.
     */
    OPEN(0x4e45504f),
    /**
     * OKAY is a success message. It is sent when a write is
     * processed successfully.
     */
    OKAY(0x59414b4f),
    /**
     * CLSE is the close stream message. It it sent to close an
     * existing stream on the target device.
     */
    CLSE(0x45534c43),
    /**
     * WRTE is the write stream message. It is sent with a payload
     * that is the data to write to the stream.
     */
    WRTE(0x45545257),
    SYNC(0x434e5953),
    /**
     * CNXN is the connect message. No messages (except AUTH)
     * are valid before this message is received.
     */
    CNXN(0x4e584e43),
    /**
     * AUTH is the authentication message. It is part of the
     * RSA public key authentication added in Android 4.2.2.
     */
    AUTH(0x48545541);

    int command;

    AdbCommand(int command) {
        this.command = command;
    }


    /**
     * 根据类型，返回类型的枚举实例。
     */
    public static AdbCommand fromValue(int command) {
        for (AdbCommand adbCommand : AdbCommand.values()) {
            if (adbCommand.command == command) {
                return adbCommand;
            }
        }
        return null;
    }

}
