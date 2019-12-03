package com.tangqiang.android.common;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * 获取设备
 */
public class AdbBackend {
    private static final int CONNECTION_ITERATION_TIMEOUT_MS = 200;
    private final AndroidDebugBridge bridge;
    private final boolean initAdb;
    private Logger logger = LoggerFactory.getLogger(getClass());

    public AdbBackend() {
        this((String) null, true);
    }

    public AdbBackend(String adbLocation, boolean initAdb) {
        this.initAdb = initAdb;
        if (adbLocation == null) {
            adbLocation = "adb";
        }

        if (this.initAdb) {
            AndroidDebugBridge.init(false);
        }

        this.bridge = AndroidDebugBridge.createBridge(adbLocation, true);
        this.logger.info("adbLocation:" + adbLocation + "  initAdb:" + initAdb + "  bridge:" + this.bridge);
    }


    public IDevice[] getDevice() {
        return this.getDevice(60000L, null);
    }

    public IDevice[] getDevice(String deviceId) {
        return this.getDevice(60000L, deviceId);
    }

    public IDevice[] getDevice(long timeoutMs, String deviceId) {
        while (timeoutMs > 0L) {
            try {
                IDevice[] devicesTp = this.bridge.getDevices();
                IDevice[] devices = deviceId != null ? (IDevice[]) Arrays.stream(devicesTp).filter(device -> device.getSerialNumber().contains(deviceId)).toArray() : devicesTp;
                if (devices != null && devices.length > 0) {
                    return devices;
                }
            } catch (Exception e) {
                this.logger.error("Error getDevices", e);
            }

            try {
                Thread.sleep(CONNECTION_ITERATION_TIMEOUT_MS);
            } catch (Exception e) {
            }
            timeoutMs -= CONNECTION_ITERATION_TIMEOUT_MS;
        }
        return null;
    }


    public void shutdown() {
        if (this.initAdb) {
            AndroidDebugBridge.terminate();
        }
    }
}
