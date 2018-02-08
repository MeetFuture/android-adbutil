package com.tangqiang;

import com.android.ddmlib.IDevice;
import com.tangqiang.adb.AdbDevice;
import com.tangqiang.monkey.MonkeyDevice;

import java.util.Collection;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-02-03 0003 Tom create
 * @date 2018-02-03 0003
 */
public class Test {
    public static void main(String[] args) {
        Test th = new Test();
        th.execute();
    }

    private void execute() {
        try {
            AdbBackend backend = new AdbBackend();
            IDevice a = backend.getDevice();

            //AdbDevice adbDevice = new AdbDevice(a);
            //String tap = adbDevice.tap(500, 600);
            //System.out.println("Test.Tap :" + tap);
            // executeFish(adbDevice);

            MonkeyDevice monkeyDevice = new MonkeyDevice(a);
            Collection<String> collections = monkeyDevice.listVariable();
            for (String s : collections) {
                System.out.println("Variable :" + s);
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

    private void executeFish(AdbDevice adbDevice) {
        while (true) {
            try {
                Thread.sleep(1000);
                adbDevice.tap(1600, 700);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


}
