package com.tangqiang.adb;

import com.tangqiang.adblib.core.AdbBase64;
import com.tangqiang.adblib.core.AdbConnection;
import com.tangqiang.adblib.core.AdbCrypto;
import com.tangqiang.adblib.core.AdbStream;
import com.tangqiang.adblib.core.expand.AdbCryptoBuilder;
import com.tangqiang.adblib.core.expand.AdbFrameBufferReader;
import com.tangqiang.adblib.core.expand.AdbMsgWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.DatatypeConverter;
import java.net.Socket;

/**
 * Hello world!
 */
public class AppTe {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String ip = "10.1.141.107";
    private int port = 5555;


    private void start() throws Exception {
        logger.info("App Test begin .. ");
        AdbCrypto crypto = new AdbCryptoBuilder().build("target/tmp/pub.key", "target/tmp/priv.key");
        logger.info("Socket connecting...");
        Socket sock = new Socket(ip, port);
        logger.info("Socket connected");
        AdbConnection adb = AdbConnection.create(sock, crypto);
        logger.info("ADB connecting...");

        adb.connect();
        logger.info("ADB connected");


        AdbStream stream = adb.open("framebuffer:");
        new AdbFrameBufferReader(stream).start();

        //AdbStream stream = adb.open("shell:");
//        AdbStream stream = adb.open("track-jdwp");

        //new AdbShellReader(stream).start();

        new AdbMsgWriter(stream).start();
    }


    private void test() throws Exception {

        Socket socket = new Socket("192.168.1.42", 5555);

        AdbCrypto crypto = AdbCrypto.generateAdbKeyPair(new AdbBase64() {
            @Override
            public String encodeToString(byte[] data) {
                return DatatypeConverter.printBase64Binary(data);
            }
        });


        AdbConnection connection = AdbConnection.create(socket, crypto);
        connection.connect();

        AdbStream stream = connection.open("shell:logcat");
    }
}
