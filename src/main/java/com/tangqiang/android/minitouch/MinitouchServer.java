package com.tangqiang.android.minitouch;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.receiver.LogOutputReceiver;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * minitouch 服务处理
 *
 * @author tqiang
 * @date 2019-09-21 20:49
 */
public class MinitouchServer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String REMOTE_PATH = "/data/local/tmp/minitouch";
    private String ABI_COMMAND = "ro.product.cpu.abi";
    private String SDK_COMMAND = "ro.build.version.sdk";
    private String MINITOUCH_START_COMMAND = REMOTE_PATH + "/minitouch ";
    private IDevice iDevice;
    private MinitouchService minitouchService;
    private int port = 5558;

    public MinitouchServer(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    public MinitouchServer(IDevice iDevice, int port) {
        this.iDevice = iDevice;
        this.port = port;
    }

    public MinitouchServer(IDevice iDevice, int port, String extStartCmd) {
        this.iDevice = iDevice;
        this.port = port;
        this.MINITOUCH_START_COMMAND += extStartCmd;
    }

    public void start() {
        try {
            logger.info("Begin init MinitouchServer ......");
            String abi = iDevice.getProperty(ABI_COMMAND);
            String sdk = iDevice.getProperty(SDK_COMMAND);
            logger.info("Android abi:" + abi + "   sdk:" + sdk);

            // 复制文件到临时目录
            ClassPathResource resourceBin = new ClassPathResource("minitouch" + File.separator + abi + File.separator + "minitouch");
            File minitouchBinFile = File.createTempFile("minitouch", "bin");
            try (InputStream inputStream = resourceBin.getInputStream()) {
                FileUtils.copyInputStreamToFile(inputStream, minitouchBinFile);
            }

            LogOutputReceiver ignorReceiver = new LogOutputReceiver();
            iDevice.executeShellCommand("mkdir " + REMOTE_PATH, ignorReceiver, 1, TimeUnit.SECONDS);
            iDevice.executeShellCommand("chmod -R 770 " + REMOTE_PATH, ignorReceiver, 1, TimeUnit.SECONDS);
            // 将minitouch  push到设备中
            iDevice.pushFile(minitouchBinFile.getAbsolutePath(), REMOTE_PATH + "/minitouch");

            // 删除临时文件
            minitouchBinFile.delete();

            iDevice.executeShellCommand(String.format("chmod 770 %s/%s", REMOTE_PATH, "minitouch"), ignorReceiver, 1, TimeUnit.SECONDS);


            // 启动 minitouch 服务
            String startCommand = MINITOUCH_START_COMMAND;
            minitouchService = new MinitouchService(startCommand);
            minitouchService.start();

            Thread.sleep(1000);
            // 端口转发
            iDevice.createForward(port, "minitouch", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            logger.info("Minitouch createForward  PORT:" + port);
        } catch (Exception e) {
            logger.error("Minitouch Server error ! ", e);
        }
    }


    public void stop() {
        try {
            this.minitouchService.interrupt();
        } catch (Exception e) {
            logger.error("Server close error !", e);
        }
    }

    /**
     * 启动 minitouch 服务
     */
    private class MinitouchService extends Thread {
        private String startCommand;

        private MinitouchService(String startCommand) {
            this.startCommand = startCommand;
        }

        @Override
        public void run() {
            try {
                LogOutputReceiver logOutputReceiver = new LogOutputReceiver();
                logger.info("Start minitouch service : " + startCommand);
                iDevice.executeShellCommand(startCommand, logOutputReceiver, 0, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Minitouch service error!" + e.getMessage());
            }
            logger.info("Minitouch service end ----------");
        }
    }
}