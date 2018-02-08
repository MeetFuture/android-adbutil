package com.tangqiang.adblib.core;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * This class provides an abstraction for the ADB message format.
 *
 * @author Cameron Gutman
 */
public class AdbMessage {
    /**
     * The command field of the message
     */
    public int command;
    /**
     * The arg0 field of the message
     */
    public int arg0;
    /**
     * The arg1 field of the message
     */
    public int arg1;
    /**
     * The payload length field of the message
     */
    public int payloadLength;
    /**
     * The checksum field of the message
     */
    public int checksum;
    /**
     * The magic field of the message
     */
    public int magic;
    /**
     * The payload of the message
     */
    public byte[] payload;

    /**
     * Read and parse an ADB message from the supplied input stream.
     * This message is NOT validated.
     *
     * @param in InputStream object to read data from
     * @return An AdbMessage object represented the message read
     * @throws IOException If the stream fails while reading
     */
    public static AdbMessage parseAdbMessage(InputStream in) throws IOException {
        AdbMessage msg = new AdbMessage();
        ByteBuffer packet = ByteBuffer.allocate(AdbProtocol.ADB_HEADER_LENGTH).order(ByteOrder.LITTLE_ENDIAN);

        /* Read the header first */
        int dataRead = 0;
        do {
            int bytesRead = in.read(packet.array(), dataRead, 24 - dataRead);

            if (bytesRead < 0)
                throw new IOException("Stream closed");
            else
                dataRead += bytesRead;
        }
        while (dataRead < AdbProtocol.ADB_HEADER_LENGTH);

        /* Pull out header fields */
        msg.command = packet.getInt();
        msg.arg0 = packet.getInt();
        msg.arg1 = packet.getInt();
        msg.payloadLength = packet.getInt();
        msg.checksum = packet.getInt();
        msg.magic = packet.getInt();

        /* If there's a payload supplied, read that too */
        if (msg.payloadLength != 0) {
            msg.payload = new byte[msg.payloadLength];

            dataRead = 0;
            do {
                int bytesRead = in.read(msg.payload, dataRead, msg.payloadLength - dataRead);

                if (bytesRead < 0)
                    throw new IOException("Stream closed");
                else
                    dataRead += bytesRead;
            }
            while (dataRead < msg.payloadLength);
        }

        return msg;
    }

    @Override
    public String toString() {
        String  msg = "arg0:" + arg0 +"  arg1:" + arg1 + "  payloadLength:" + payloadLength+ "  checksum:" + checksum +"  magic:" + magic ;
        return msg;
    }
}