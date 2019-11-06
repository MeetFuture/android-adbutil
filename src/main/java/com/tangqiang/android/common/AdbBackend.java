package com.tangqiang.android.common;

import com.android.ddmlib.AndroidDebugBridge;
import com.android.ddmlib.IDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        return this.getDevice(60000L);
    }

    public IDevice[] getDevice(long timeoutMs) {
        while (timeoutMs > 0L) {
            try {
                IDevice[] device = this.bridge.getDevices();
                if (device != null && device.length > 0) {
                    return device;
                }

                Thread.sleep(CONNECTION_ITERATION_TIMEOUT_MS);
                timeoutMs -= CONNECTION_ITERATION_TIMEOUT_MS;
            } catch (Exception e) {
                this.logger.error("Error getDevices", e);
            }
        }
        return null;
    }


    public void shutdown() {
        if (this.initAdb) {
            AndroidDebugBridge.terminate();
        }
    }
}
