package com.tangqiang.adblib.core;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class provides useful functions and fields for ADB protocol details.
 *
 * @author Cameron Gutman
 */
public class AdbProtocol {

    /**
     * The length of the ADB message header
     */
    public static final int ADB_HEADER_LENGTH = 24;

    /**
     * The current version of the ADB protocol
     */
    public static final int CONNECT_VERSION = 0x01000000;

    /**
     * The maximum data payload supported by the ADB implementation
     */
    public static final int CONNECT_MAXDATA = 4096;

    /**
     * The payload sent with the connect message
     */
    public static byte[] CONNECT_PAYLOAD;

    /**
     * This authentication type represents a SHA1 hash to sign
     */
    public static final int AUTH_TYPE_TOKEN = 1;

    /**
     * This authentication type represents the signed SHA1 hash
     */
    public static final int AUTH_TYPE_SIGNATURE = 2;

    /**
     * This authentication type represents a RSA public key
     */
    public static final int AUTH_TYPE_RSA_PUBLIC = 3;

    static {
        try {
            CONNECT_PAYLOAD = "host::\0".getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
        }
    }

    /**
     * This function performs a checksum on the ADB payload data.
     *
     * @param payload Payload to checksum
     * @return The checksum of the payload
     */
    private static int getPayloadChecksum(byte[] payload) {
        int checksum = 0;

        for (byte b : payload) {
            /* We have to manually "unsign" these bytes because Java sucks */
            if (b >= 0)
                checksum += b;
            else
                checksum += b + 256;
        }

        return checksum;
    }

    /**
     * This function validate the ADB message by checking
     * its command, magic, and payload checksum.
     *
     * @param msg ADB message to validate
     * @return True if the message was valid, false otherwise
     */
    public static boolean validateMessage(AdbMessage msg) {
        /* Magic is cmd ^ 0xFFFFFFFF */
        if (msg.command != (msg.magic ^ 0xFFFFFFFF))
            return false;

        if (msg.payloadLength != 0) {
            if (getPayloadChecksum(msg.payload) != msg.checksum)
                return false;
        }

        return true;
    }

    /**
     * This function generates an ADB message given the fields.
     *
     * @param cmd     Command identifier
     * @param arg0    First argument
     * @param arg1    Second argument
     * @param payload Data payload
     * @return Byte array containing the message
     */
    public static byte[] generateMessage(int cmd, int arg0, int arg1, byte[] payload) {
        /* struct message {
         *     unsigned command;       // command identifier constant
         *     unsigned arg0;          // first argument
         *     unsigned arg1;          // second argument
         *     unsigned data_length;   // length of payload (0 is allowed)
         *     unsigned data_check;    // checksum of data payload
         *     unsigned magic;         // command ^ 0xffffffff
         * };
         */

        ByteBuffer message;

        if (payload != null) {
            message = ByteBuffer.allocate(ADB_HEADER_LENGTH + payload.length).order(ByteOrder.LITTLE_ENDIAN);
        } else {
            message = ByteBuffer.allocate(ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        }

        message.putInt(cmd);
        message.putInt(arg0);
        message.putInt(arg1);

        if (payload != null) {
            message.putInt(payload.length);
            message.putInt(getPayloadChecksum(payload));
        } else {
            message.putInt(0);
            message.putInt(0);
        }

        message.putInt(cmd ^ 0xFFFFFFFF);

        if (payload != null) {
            message.put(payload);
        }

        return message.array();
    }

    /**
     * Generates a connect message with default parameters.
     *
     * @return Byte array containing the message
     */
    public static byte[] generateConnect() {
        return generateMessage(AdbCommand.CNXN.command, CONNECT_VERSION, CONNECT_MAXDATA, CONNECT_PAYLOAD);
    }

    /**
     * Generates an auth message with the specified type and payload.
     *
     * @param type Authentication type (see AUTH_TYPE_* constants)
     * @param data The payload for the message
     * @return Byte array containing the message
     */
    public static byte[] generateAuth(int type, byte[] data) {
        return generateMessage(AdbCommand.AUTH.command, type, 0, data);
    }

    /**
     * Generates an open stream message with the specified local ID and destination.
     *
     * @param localId A unique local ID identifying the stream
     * @param dest    The destination of the stream on the target
     * @return Byte array containing the message
     * @throws UnsupportedEncodingException If the destination cannot be encoded to UTF-8
     */
    public static byte[] generateOpen(int localId, String dest) throws UnsupportedEncodingException {
        ByteBuffer bbuf = ByteBuffer.allocate(dest.length() + 1);
        bbuf.put(dest.getBytes("UTF-8"));
        bbuf.put((byte) 0);
        return generateMessage(AdbCommand.OPEN.command, localId, 0, bbuf.array());
    }

    /**
     * Generates a write stream message with the specified IDs and payload.
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @param data     The data to provide as the write payload
     * @return Byte array containing the message
     */
    public static byte[] generateWrite(int localId, int remoteId, byte[] data) {
        return generateMessage(AdbCommand.WRTE.command, localId, remoteId, data);
    }

    /**
     * Generates a write stream message with the specified IDs and payload.
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @param command     The data to provide as the write command
     * @return Byte array containing the message
     */
    public static byte[] generateWrite(int localId, int remoteId,String command) throws UnsupportedEncodingException  {
        ByteBuffer bbuf = ByteBuffer.allocate(command.length() + 1);
        bbuf.put(command.getBytes("UTF-8"));
        bbuf.put((byte) 0);
        return generateMessage(AdbCommand.WRTE.command, localId, remoteId, bbuf.array());
    }

    /**
     * Generates a close stream message with the specified IDs.
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @return Byte array containing the message
     */
    public static byte[] generateClose(int localId, int remoteId) {
        return generateMessage(AdbCommand.CLSE.command, localId, remoteId, null);
    }

    /**
     * Generates an okay message with the specified IDs.
     *
     * @param localId  The unique local ID of the stream
     * @param remoteId The unique remote ID of the stream
     * @return Byte array containing the message
     */
    public static byte[] generateReady(int localId, int remoteId) {
        return generateMessage(AdbCommand.OKAY.command, localId, remoteId, null);
    }

}
