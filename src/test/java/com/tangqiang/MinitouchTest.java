package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.AdbBackend;
import com.tangqiang.android.minitouch.MinitouchClient;
import com.tangqiang.android.minitouch.MinitouchServer;

/**
 * @author tqiang
 * @date 2019-10-13 23:47
 */
public class MinitouchTest {

    public static void main(String[] args) throws Exception {
        AdbBackend backend = new AdbBackend();
        IDevice[] devices = backend.getDevice();
        System.out.println("MinitouchTest.main  devices:" + devices.length + "  " + devices);
        if (devices.length != 1) {
            return;
        }
        IDevice device = devices[0];
        MinitouchServer minitouchServer = new MinitouchServer(device, 8888);
        minitouchServer.start();

        MinitouchClient client = new MinitouchClient(8888);
        client.start();

        Thread.sleep(1000);
        System.out.println("MinitouchTest begin touch ");
        client.tap(400, 400);
        System.out.println("MinitouchTest end touch ");
        Thread.sleep(1000);
        client.dragAngle(400, 2000, 800, 400);
        Thread.sleep(1000);
        client.stop();
        minitouchServer.stop();
        backend.shutdown();
    }


}
