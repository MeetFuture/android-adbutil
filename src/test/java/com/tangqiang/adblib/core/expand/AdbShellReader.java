package com.tangqiang.adblib.core.expand;

import com.tangqiang.adblib.core.AdbStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *  adb.open("shell:")
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public class AdbShellReader extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AdbStream stream;

    public AdbShellReader(AdbStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        while (!stream.isClosed()) {
            try {
                String msg = new String(stream.read(), "US-ASCII");

                logger.info(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
