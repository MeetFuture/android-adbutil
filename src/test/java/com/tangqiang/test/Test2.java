package com.tangqiang.test;

import com.android.ddmlib.IDevice;
import com.tangqiang.AdbBackend;
import com.tangqiang.adb.AdbDevice;
import com.tangqiang.adb.event.GetEventReceiver;
import com.tangqiang.adb.event.RawInputEvent;
import com.tangqiang.core.types.Rect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 */
public class Test2 {
    private Logger logger = LoggerFactory.getLogger(getClass());

    public static void main(String[] args) {
        Test2 th = new Test2();
        th.execute();
    }

    private void execute() {
        try {
            AdbBackend backend = new AdbBackend();
            IDevice device = backend.getDevice();

            AdbDevice adbDevice = new AdbDevice(device);

            Rect screenPhysical = adbDevice.getScreenPhysical();
            Rect screenVirtual = adbDevice.getScreenVirtual();
            logger.info("ScreenSize:" + screenPhysical + "   Max:" + screenVirtual);
            adbDevice.getEvent(new GetEventR());
//            adbDevice.shell("getevent -p | grep -e '0035' -e '0036'");

//            adbDevice.shell("getevent -c 20| grep -e '0035' -e '0036'", 100000);


            adbDevice.close();


            backend.shutdown();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private class GetEventR extends GetEventReceiver {

        @Override
        public void processNewEvent(RawInputEvent event) {
            logger.info(event.toString());
        }
    }

}
