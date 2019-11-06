package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.AdbBackend;
import com.tangqiang.android.monkey.MonkeyClient;
import com.tangqiang.android.monkey.MonkeyServer;

/**
 * TODO
 *
 * @author tqiang
 * @date 2019-11-05 09:29
 */

public class MonkeyTest {
    public static void main(String[] args) throws Exception {
        AdbBackend backend = new AdbBackend();
        IDevice[] devices = backend.getDevice();
        System.out.println("MonkeyTest.main  devices:" + devices.length + "  " + devices);
        if (devices.length != 1) {
            return;
        }
        MonkeyServer monkeyServer = new MonkeyServer(devices[0]);
        monkeyServer.start();

        MonkeyClient client = new MonkeyClient();
        client.start();

        Thread.sleep(1000);
        client.tap(400, 400);
        Thread.sleep(1000);
        client.stop();
        monkeyServer.stop();

        backend.shutdown();
    }

}
