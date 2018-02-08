package com.tangqiang.monkey;

import com.android.ddmlib.IDevice;
import com.tangqiang.core.CommandOutputReceiver;
import com.tangqiang.core.IMonkeyDevice;
import com.tangqiang.core.LinearInterpolator;
import com.tangqiang.core.LogOutputReceiver;
import com.tangqiang.core.types.Point;
import com.tangqiang.monkey.types.MonkeyButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Monkey
 * 使用Monkey以获得比Adb更快的按键、触摸交互速度
 *
 * @author Tom
 * @version 1.0 2018-02-05 0005 Tom create
 * @date 2018-02-05 0005
 * @copyright Copyright © 2018 Grgbanking All rights reserved.
 */
public class MonkeyDevice implements IMonkeyDevice {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private IDevice device;
    private int localPort = 5559;
    private Socket monkeySocket;
    private BufferedWriter monkeyWriter;
    private BufferedReader monkeyReader;


    public MonkeyDevice(IDevice device) {
        this.device = device;
        init();
    }

    public MonkeyDevice(IDevice device, int localPort) {
        this.device = device;
        this.localPort = localPort;
        init();
    }

    private void init() {
        boolean success = false;
        long start = System.currentTimeMillis();
        while (!success) {
            try {
                long diff = System.currentTimeMillis() - start;
                if (diff > 10000L) {
                    logger.error("Unable to create MonkeyDevice");
                    return;
                }
                Thread.sleep(1000L);

                Socket monkeySocket = createForwardSocket(device, localPort);
                if (monkeySocket == null) {
                    continue;
                }

                this.monkeySocket = monkeySocket;
                this.monkeyWriter = new BufferedWriter(new OutputStreamWriter(monkeySocket.getOutputStream()));
                this.monkeyReader = new BufferedReader(new InputStreamReader(monkeySocket.getInputStream()));

                boolean wake = wake();
                if (!wake) {
                    continue;
                }
            } catch (Exception e) {
                logger.error("Create Manager error :" + e.getMessage());
                continue;
            }
            success = true;
        }
    }


    private Socket createForwardSocket(IDevice device, int port) {
        Socket monkeySocket = null;
        try {
            String monkeyShow = "ps | grep 'com.android.commands.monkey'";
            CommandOutputReceiver receiver = new CommandOutputReceiver();
            device.executeShellCommand(monkeyShow, receiver, 5000);
            String monkeyMsg = receiver.toString().trim();
            if (monkeyMsg.length() > 0) {
                logger.warn("Monkey already run:" + monkeyMsg);
            }

            String command = "monkey --port " + port;
            logger.info("Start monkey on device :" + command);
            shellAsync(command, 60000);

            Thread.sleep(2000);
            logger.info("create device port Forward to this computer");
            device().createForward(port, port);
            monkeySocket = new Socket("127.0.0.1", port);
        } catch (Exception e) {
            logger.error("Create device port Forward to this computer error!" + e.getMessage());
        }
        return monkeySocket;
    }


    private void shellAsync(String cmd, long timeout) {
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

    @Override
    public IDevice device() {
        return device;
    }


    @Override
    public boolean press(MonkeyButton button) {
        return sendCommand("press " + button.getKeyName());
    }

    @Override
    public boolean press(MonkeyButton button, long pressTime) {
        boolean result = sendCommand("key down " + button.getKeyName());
        try {
            Thread.sleep(pressTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result && sendCommand("key up " + button.getKeyName());
    }


    @Override
    public void pressAsync(MonkeyButton button) {
        sendCommandAsync("press " + button.getKeyName());
    }

    @Override
    public void pressAsync(MonkeyButton button, long pressTime) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                press(button, pressTime);
            }
        });
    }

    @Override
    public boolean type(String message) {
        StringTokenizer tok = new StringTokenizer(message, "\n", true);
        while (tok.hasMoreTokens()) {
            String line = tok.nextToken();
            boolean success;
            if ("\n".equals(line)) {
                success = press(MonkeyButton.ENTER);
                if (!success) {
                    return false;
                }
            } else {
                success = sendCommand("type " + line);
                if (!success) {
                    return false;
                }
            }
        }
        return true;
    }

    @Override
    public void typeAsync(String message) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                type(message);
            }
        });
    }

    @Override
    public boolean tap(int x, int y) {
        return sendCommand("tap " + x + " " + y);
    }

    @Override
    public void tapAsync(int x, int y) {
        sendCommandAsync("tap " + x + " " + y);
    }

    @Override
    public boolean touch(int x, int y, long ms) {
        boolean result = sendCommand("touch down " + x + " " + y);
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result && sendCommand("touch up " + x + " " + y);
    }

    @Override
    public void touchAsync(int x, int y, long ms) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                touch(x, y, ms);
            }
        });
    }

    @Override
    public boolean drag(int startx, int starty, int endx, int endy, long ms) {
        return drag(startx, starty, endx, endy, (int) (ms / 10), ms);
    }

    @Override
    public boolean drag(int startx, int starty, int endx, int endy, int steps, long ms) {
        final long iterationTime = ms / (long) steps;
        LinearInterpolator lerp = new LinearInterpolator(steps);
        Point start = new Point(startx, starty);
        Point end = new Point(endx, endy);
        lerp.interpolate(start, end, new LinearInterpolator.Callback() {
            public void start(Point point) {
                try {
                    sendCommand("touch down " + point.getX() + " " + point.getY());
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            public void step(Point point) {
                try {
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            public void end(Point point) {
                try {
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    sendCommand("touch up " + point.getX() + " " + point.getY());
                } catch (Exception e) {
                    logger.error("Error sending drag end event", e);
                }
            }
        });
        return true;
    }

    @Override
    public void dragAsync(int startx, int starty, int endx, int endy, long ms) {
        dragAsync(startx, starty, endx, endy, (int) (ms / 10), ms);
    }

    @Override
    public void dragAsync(int startx, int starty, int endx, int endy, int step, long ms) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                drag(startx, starty, endx, endy, step, ms);
            }
        });
    }

    @Override
    public void reboot(String into) {
        try {
            device.reboot(into);
        } catch (Exception e) {
            logger.error("Unable to reboot device", e);
        }
    }

    @Override
    public boolean wake() {
        return sendCommand("wake");
    }


    public String getVariable(String name) throws IOException {
        try {
            String response = sendCommand0("getvar " + name);
            if (this.parseResponse(response)) {
                int offset = response.indexOf(58);
                String extras = offset < 0 ? "" : response.substring(offset + 1);
                return extras;
            }
        } catch (Exception e) {
            logger.warn("Execute getVariable[" + name + "]" + " Error !", e);
        }
        return null;
    }

    public Collection<String> listVariable() {
        try {
            String response = sendCommand0("listvar");
            if (parseResponse(response)) {
                int offset = response.indexOf(58);
                String extras = offset < 0 ? "" : response.substring(offset + 1);
                List<String> list = Arrays.asList(extras.split(" "));
                return list;
            }
        } catch (Exception e) {
            logger.warn("Execute listVariable Error !", e);
        }
        return Collections.emptyList();
    }


    @Override
    public boolean sendCommand(String command) {
        String monkeyResponse = sendCommand0(command);
        return this.parseResponse(monkeyResponse);
    }

    @Override
    public void sendCommandAsync(String command) {
        executor.submit(new Runnable() {
            public void run() {
                sendCommand0(command);
            }
        });
    }

    private String sendCommand0(String command) {
        String result = null;
        try {
            command = command.trim();
            this.monkeyWriter.write(command + "\n");
            this.monkeyWriter.flush();
            result = monkeyReader.readLine();
            logger.debug("Monkey Command: " + command + "  Result:" + result);
        } catch (Exception e) {
            logger.error("send Event[" + command + "] error! " + e.getMessage());
        }
        return result;
    }

    private boolean parseResponse(String monkeyResponse) {
        if (monkeyResponse == null) {
            return false;
        } else {
            return monkeyResponse.startsWith("OK");
        }
    }


    @Override
    public void close() {
        try {
            executor.shutdown();
            this.sendCommand("quit");
            this.monkeyReader.close();
            this.monkeyWriter.close();
            this.monkeySocket.close();
        } catch (Exception e) {
            logger.error("Close monkey error !", e);
        }
    }


}
