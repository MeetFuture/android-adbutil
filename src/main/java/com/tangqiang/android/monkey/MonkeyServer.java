package com.tangqiang.android.monkey;


import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.receiver.ContainsOutputReceiver;
import com.tangqiang.android.common.receiver.LogOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Monkey 服务处理
 *
 * @author tqiang
 * @date 2019-09-21 20:49
 */
public class MonkeyServer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String MONKEY_START_COMMAND = "monkey --port ";
    private String MONKEY_STOP_COMMAND = "pkill -f com.android.commands.monkey";
    private IDevice iDevice;
    private int port = 5559;
    private String extStartCmd;
    private MonkeyService monkeyService;

    public MonkeyServer(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    public MonkeyServer(IDevice iDevice, int port) {
        this.iDevice = iDevice;
        this.port = port;
    }

    public MonkeyServer(IDevice iDevice, int port, String extStartCmd) {
        this.iDevice = iDevice;
        this.port = port;
        this.extStartCmd = extStartCmd;
    }

    public void start() {
        try {
            // 启动 minitouch 服务
            String startCommand = MONKEY_START_COMMAND + port + " " + (extStartCmd != null ? extStartCmd : "");
            logger.info("Begin init MonkeyServer , startCommand:" + startCommand);
            ContainsOutputReceiver outputReceiver = new ContainsOutputReceiver("mCurArgData");
            monkeyService = new MonkeyService(startCommand, outputReceiver);
            monkeyService.start();

            long beginTime = System.currentTimeMillis();
            while (!outputReceiver.contains() && (System.currentTimeMillis() - 10000 < beginTime)) {
                Thread.sleep(10);
            }

            // 端口转发
            logger.info("Monkey createForward  PORT:" + port);
            iDevice.createForward(port, port);
        } catch (Exception e) {
            logger.error("Monkey Server error ! ", e);
        }
    }


    public void stop() {
        try {
            LogOutputReceiver receiver = new LogOutputReceiver();
            logger.info("Stop monkey service : " + MONKEY_STOP_COMMAND);
            iDevice.executeShellCommand(MONKEY_STOP_COMMAND, receiver, 1, TimeUnit.SECONDS);
            this.monkeyService.interrupt();
        } catch (Exception e) {
            logger.error("Server close error !", e);
        }
    }

    /**
     * 启动 minitouch 服务
     */
    private class MonkeyService extends Thread {
        private String startCommand;
        private ContainsOutputReceiver outputReceiver;

        private MonkeyService(String startCommand, ContainsOutputReceiver outputReceiver) {
            this.startCommand = startCommand;
            this.outputReceiver = outputReceiver;
        }

        @Override
        public void run() {
            try {
                //"ps -ef | grep 'com.android.commands.monkey'";

                logger.info("Stop monkey service : " + MONKEY_STOP_COMMAND);
                iDevice.executeShellCommand(MONKEY_STOP_COMMAND, outputReceiver, 1, TimeUnit.SECONDS);

                logger.info("Start monkey service : " + startCommand);
                iDevice.executeShellCommand(startCommand, outputReceiver, 0, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Monkey service error! " + e.getMessage());
            }
            logger.info("Monkey service end ----------");
        }
    }
}