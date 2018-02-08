package com.tangqiang.adb;

import com.android.ddmlib.IDevice;
import com.tangqiang.adb.image.AdbImage;
import com.tangqiang.adb.types.AdbShellButton;
import com.tangqiang.core.CommandOutputReceiver;
import com.tangqiang.core.IAdbDevice;
import com.tangqiang.core.LogOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Adb device
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public class AdbDevice implements IAdbDevice {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private IDevice device;


    public AdbDevice(IDevice device) {
        this.device = device;
    }

    @Override
    public IDevice device() {
        return device;
    }


    @Override
    public boolean press(AdbShellButton keycode) {
        return press(keycode, false);
    }

    @Override
    public boolean press(AdbShellButton keycode, boolean longpress) {
        String result = shell("input", "keyevent", keycode.getValue(), longpress ? " --longpress " : "");
        return shellResult(result);
    }


    @Override
    public void pressAsync(AdbShellButton keycode) {
        pressAsync(keycode, false);
    }

    @Override
    public void pressAsync(AdbShellButton keycode, boolean longpress) {
        shellAsync("input", "keyevent", keycode.getValue(), longpress ? " --longpress " : "");
    }


    @Override
    public boolean type(String message) {
        String result = shell("input", "text", message);
        return shellResult(result);
    }

    @Override
    public void typeAsync(String message) {
        shellAsync("input", "text", message);
    }

    @Override
    public boolean tap(int x, int y) {
        String result = shell("input", "tap", x, y);
        return shellResult(result);
    }

    @Override
    public void tapAsync(int x, int y) {
        shellAsync("input", "tap", x, y);
    }


    @Override
    public boolean touch(int x, int y, long ms) {
        String result = shell("input", "swipe", x, y, x, y, ms);
        return shellResult(result);
    }

    @Override
    public void touchAsync(int x, int y, long ms) {
        shellAsync("input", "swipe", x, y, x, y, ms);
    }

    @Override
    public boolean drag(int startx, int starty, int endx, int endy, long ms) {
        String result = shell("input", "swipe", startx, starty, endx, endy, ms);
        return shellResult(result);
    }

    @Override
    public void dragAsync(int startx, int starty, int endx, int endy, long ms) {
        shellAsync("input", "swipe", startx, starty, endx, endy, ms);
    }

    @Override
    public void reboot(String into) {
        try {
            logger.debug("reboot.......... ");
            device.reboot(into);
        } catch (Exception e) {
            logger.error("Unable to reboot device", e);
        }
    }

    @Override
    public boolean wake() {
        return press(AdbShellButton.NOTIFICATION);
    }

    /**
     * 截屏  Frame传输
     */
    public AdbImage takeSnapshotFrame() {
        try {
            logger.debug("takeSnapshotFrame ");
            return new AdbImage(device.getScreenshot());
        } catch (Exception e) {
            logger.error("Unable to take snapshot", e);
            return null;
        }
    }

    /**
     * 截屏 文件传输
     */
    public boolean takeSnapshot(String localFile) {
        try {
            File local = new File(localFile);
            String phoneFile = "/sdcard/tmp/screenshot.png";
            logger.debug("takeSnapshot [" + phoneFile + "]  to:" + local.getAbsolutePath());
            shell("screencap", "-p", phoneFile);
            device.pullFile(phoneFile, local.getAbsolutePath());
            return true;
        } catch (Exception e) {
            logger.error("Unable to take snapshot", e);
            return false;
        }
    }


    @Override
    public String shell(String cmd) {
        return shell(cmd, 5000);
    }

    @Override
    public String shell(Object... args) {
        String cmd = shellBuild(args);
        return shell(cmd, 5000);
    }

    @Override
    public String shell(String cmd, long timeout) {
        CommandOutputReceiver capture = new CommandOutputReceiver();
        try {
            device.executeShellCommand(cmd, capture, (int) timeout);
        } catch (Exception e) {
            logger.error("Error executing command: " + cmd, e);
            return "Error:" + e.getMessage();
        }
        String result = capture.toString();
        logger.debug("shell execute[" + cmd + "]  timeout:" + timeout + "  Result:" + result);
        return result;
    }

    @Override
    public void shellAsync(Object... args) {
        String cmd = shellBuild(args);
        shellAsync(cmd);
    }


    @Override
    public void shellAsync(String cmd) {
        shellAsync(cmd, Integer.MAX_VALUE);
    }

    @Override
    public void shellAsync(String cmd, long timeout) {
        executor.submit(new Runnable() {
            public void run() {
                try {
                    logger.debug("shell Async execute[" + cmd + "]  timeout:" + timeout);
                    LogOutputReceiver outputReceiver = new LogOutputReceiver();
                    device.executeShellCommand(cmd, outputReceiver, (int) timeout);
                } catch (Exception e) {
                    logger.warn("Execute command[" + cmd + "]" + " Error:" + e.getMessage());
                }
            }
        });
    }

    private String shellBuild(Object... args) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < args.length; i++) {
            String arg = String.valueOf(args[i]);
            stringBuilder.append(arg).append(" ");
        }
        return stringBuilder.toString().trim();
    }

    private boolean shellResult(String result) {
        return !result.startsWith("Error");
    }


    @Override
    public void close() {
        executor.shutdown();
    }

}
