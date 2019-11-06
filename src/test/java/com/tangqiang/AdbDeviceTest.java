package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.adb.AdbDevice;
import com.tangqiang.android.common.AdbBackend;

/**
 * @author tqiang
 * @date 2019-10-13 23:47
 */
public class AdbDeviceTest {

    public static void main(String[] args) throws Exception {
        System.out.println("AdbDeviceTest.main  getDevices");
        AdbBackend backend = new AdbBackend();
        IDevice[] devices = backend.getDevice();
        System.out.println("AdbDeviceTest.main  devices:" + devices.length + "  " + devices);
        if (devices.length != 1) {
            return;
        }
        IDevice device = devices[0];
        AdbDevice adbDevice = new AdbDevice(device);
        adbDevice.drag(400, 1000, 800, 300, 1000);

        backend.shutdown();
    }

}
