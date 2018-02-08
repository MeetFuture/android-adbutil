package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.adb.AdbDevice;
import com.tangqiang.monkey.MonkeyDevice;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;

/**
 * 测试
 *
 * @author Tom
 * @version 1.0 2018-02-03 0003 Tom create
 * @date 2018-02-03 0003
 */
public class Test {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Test th = new Test();
        th.execute();
    }

    private void execute() {
        try {
            AdbBackend backend = new AdbBackend();
            IDevice device = backend.getDevice();

            AdbDevice adbDevice = new AdbDevice(device);
            adbDevice.tap(500, 600);
            adbDevice.takeSnapshot("/screenshot.png");
            adbDevice.close();

            MonkeyDevice monkeyDevice = new MonkeyDevice(device);
            Collection<String> collections = monkeyDevice.listVariable();
            for (String s : collections) {
                logger.info("Variable :" + s);
            }
            Thread.sleep(1000);

            monkeyDevice.drag(500, 500, 900, 500, 100);
            Thread.sleep(2000);
            monkeyDevice.close();


            backend.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }




}
