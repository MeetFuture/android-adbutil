package com.tangqiang.android.minicap;

import com.android.ddmlib.IDevice;
import com.tangqiang.android.common.receiver.CommandOutputReceiver;
import com.tangqiang.android.common.receiver.ContainsOutputReceiver;
import com.tangqiang.android.common.receiver.LogOutputReceiver;
import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

/**
 * minicap服务处理
 *
 * @author tqiang
 * @date 2019-09-21 20:49
 */
public class MinicapServer {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private String REMOTE_PATH = "/data/local/tmp/minicap";
    private String ABI_COMMAND = "ro.product.cpu.abi";
    private String SDK_COMMAND = "ro.build.version.sdk";
    private String MINICAP_START_COMMAND = "LD_LIBRARY_PATH=" + REMOTE_PATH + " " + REMOTE_PATH + "/minicap -P %s@%s/0 ";
    private IDevice iDevice;
    private MinicapService minicapService;
    private int port = 5557;

    public MinicapServer(IDevice iDevice) {
        this.iDevice = iDevice;
    }

    public MinicapServer(IDevice iDevice, int port) {
        this.iDevice = iDevice;
        this.port = port;
    }

    public MinicapServer(IDevice iDevice, int port, String extStartCmd) {
        this.iDevice = iDevice;
        this.port = port;
        this.MINICAP_START_COMMAND += extStartCmd;
    }

    public boolean start() {
        try {
            logger.info("Begin init MinicapServer ......");
            String abi = iDevice.getProperty(ABI_COMMAND);
            String sdk = iDevice.getProperty(SDK_COMMAND);
            logger.info("Android abi:" + abi + "   sdk:" + sdk);

            // 复制文件到临时目录
            String binPath = "/minicap/bin/" + abi + "/minicap";
            logger.info("extract bin :" + binPath);
            InputStream resourceBin = MinicapServer.class.getResourceAsStream(binPath);
            File minicapBinFile = File.createTempFile("minicap", "bin");
            FileUtils.copyInputStreamToFile(resourceBin, minicapBinFile);

            String soPath = "/minicap/shared/android-" + sdk + "/" + abi + "/minicap.so";
            logger.info("extract so :" + soPath);
            InputStream resourceSo = MinicapServer.class.getResourceAsStream(soPath);
            File minicapSoFile = File.createTempFile("minicap", "so");
            FileUtils.copyInputStreamToFile(resourceSo, minicapSoFile);

            LogOutputReceiver ignorReceiver = new LogOutputReceiver();
            iDevice.executeShellCommand("mkdir " + REMOTE_PATH, ignorReceiver, 1, TimeUnit.SECONDS);
            iDevice.executeShellCommand("chmod -R 770 " + REMOTE_PATH, ignorReceiver, 1, TimeUnit.SECONDS);
            // 将minicap的可执行文件和.so文件一起push到设备中
            iDevice.pushFile(minicapBinFile.getAbsolutePath(), REMOTE_PATH + "/minicap");
            iDevice.pushFile(minicapSoFile.getAbsolutePath(), REMOTE_PATH + "/minicap.so");

            // 删除临时文件
            minicapBinFile.delete();
            minicapSoFile.delete();

            iDevice.executeShellCommand(String.format("chmod 770 %s/%s", REMOTE_PATH, "minicap"), ignorReceiver, 1, TimeUnit.SECONDS);

            // 获取设备屏幕的尺寸
            CommandOutputReceiver commandOutputReceiver = new CommandOutputReceiver();
            iDevice.executeShellCommand("wm size", commandOutputReceiver);
            String output = commandOutputReceiver.toString();
            logger.info("android wm size:" + output);
            String size = output.split(":")[1].trim();

            // 启动minicap服务
            String startCommand = String.format(MINICAP_START_COMMAND, size, size);
            ContainsOutputReceiver outputReceiver = new ContainsOutputReceiver("JpgEncoder");
            minicapService = new MinicapService(startCommand, outputReceiver);
            minicapService.start();

            long beginTime = System.currentTimeMillis();
            while (!outputReceiver.contains() && (System.currentTimeMillis() - 10000 < beginTime)) {
                Thread.sleep(10);
            }

            // 端口转发
            logger.info("Minicap createForward port:" + port);
            iDevice.createForward(port, "minicap", IDevice.DeviceUnixSocketNamespace.ABSTRACT);
            return true;
        } catch (Exception e) {
            logger.error("Minicap Server error ! ", e);
        }
        return false;
    }


    public boolean stop() {
        try {
            this.minicapService.interrupt();
            return true;
        } catch (Exception e) {
            logger.error("Server close error !", e);
            return false;
        }
    }

    /**
     * 启动minicap服务
     */
    private class MinicapService extends Thread {
        private String startCommand;
        private ContainsOutputReceiver outputReceiver;

        private MinicapService(String startCommand, ContainsOutputReceiver outputReceiver) {
            this.startCommand = startCommand;
            this.outputReceiver = outputReceiver;
        }

        @Override
        public void run() {
            try {
                logger.info("Start minicap service : " + startCommand);
                iDevice.executeShellCommand(startCommand, outputReceiver, 0, TimeUnit.SECONDS);
            } catch (Exception e) {
                logger.error("Minicap service error! " + e.getMessage());
            }
            logger.info("Minicap service end ----------");
        }
    }
}
