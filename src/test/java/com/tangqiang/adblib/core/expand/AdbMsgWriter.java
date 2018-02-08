package com.tangqiang.adblib.core.expand;

import com.tangqiang.adblib.core.AdbStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Scanner;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public class AdbMsgWriter extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AdbStream stream;

    public AdbMsgWriter(AdbStream stream) {
        this.stream = stream;
    }

    @Override
    public void run() {
        logger.info("Scanner cmd ....");
        Scanner in = new Scanner(System.in);
        while (true) {
            try {
                String cmd = in.nextLine();
                logger.info("Write cmd:" + cmd);
                stream.write(cmd + '\n');

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
