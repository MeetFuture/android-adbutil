package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.AdbBackend;
import com.tangqiang.android.minicap.MinicapClient;
import com.tangqiang.android.minicap.MinicapServer;
import com.tangqiang.android.minicap.ScreenObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;

/**
 * @author tqiang
 * @date 2019-10-13 23:47
 */
public class MinicapTest {

    public static void main(String[] args) throws Exception {
        System.out.println("MinicapServerTest.main  getDevices");
        AdbBackend backend = new AdbBackend();
        IDevice[] devices = backend.getDevice();
        System.out.println("MinicapServerTest.main  devices:" + devices.length + "  " + devices);
        if (devices.length != 1) {
            return;
        }
        MinicapServer minicapServer = new MinicapServer(devices[0], 8888);
        minicapServer.start();

        MinicapClient client = new MinicapClient(8888, new TestScreenObserver());
        client.start();

        Thread.sleep(3000);
        client.stop();
        minicapServer.stop();
        backend.shutdown();
    }


    private static class TestScreenObserver implements ScreenObserver {
        private Logger logger = LoggerFactory.getLogger(getClass());

        @Override
        public void update(Image image) {
            logger.info("Image:" + image);
        }
    }
}
