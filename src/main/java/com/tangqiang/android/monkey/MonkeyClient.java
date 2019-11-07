package com.tangqiang.android.monkey;

import com.tangqiang.android.common.polator.AngleInterpolator;
import com.tangqiang.android.common.polator.LinearInterpolator;
import com.tangqiang.monkey.types.MonkeyButton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
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
public class MonkeyClient {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ExecutorService executor = Executors.newFixedThreadPool(10);
    private int port = 5559;
    private String host;
    private Socket monkeySocket;
    private BufferedWriter monkeyWriter;
    private BufferedReader monkeyReader;

    public MonkeyClient() {
    }

    public MonkeyClient(int port) {
        this.port = port;
    }

    public MonkeyClient(String host, int port) {
        this.host = host;
        this.port = port;
    }


    public void start() {
        try {
            logger.info("Begin client ,connect to host " + host + ":" + port);
            this.monkeySocket = new Socket(host, port);
            this.monkeyWriter = new BufferedWriter(new OutputStreamWriter(monkeySocket.getOutputStream()));
            this.monkeyReader = new BufferedReader(new InputStreamReader(monkeySocket.getInputStream()));
        } catch (Exception e) {
            logger.error("client error !", e);
        }
    }


    public boolean press(MonkeyButton button) {
        return sendCommand("press " + button.getKeyName());
    }

    public boolean press(MonkeyButton button, long pressTime) {
        boolean result = sendCommand("key down " + button.getKeyName());
        try {
            Thread.sleep(pressTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result && sendCommand("key up " + button.getKeyName());
    }


    public void pressAsync(MonkeyButton button) {
        sendCommandAsync("press " + button.getKeyName());
    }

    public void pressAsync(MonkeyButton button, long pressTime) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                press(button, pressTime);
            }
        });
    }

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

    public void typeAsync(String message) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                type(message);
            }
        });
    }

    public boolean tap(int x, int y) {
        return sendCommand("tap " + x + " " + y);
    }

    public void tapAsync(int x, int y) {
        sendCommandAsync("tap " + x + " " + y);
    }

    public boolean touch(int x, int y, long ms) {
        boolean result = sendCommand("touch down " + x + " " + y);
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result && sendCommand("touch up " + x + " " + y);
    }

    public void touchAsync(int x, int y, long ms) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                touch(x, y, ms);
            }
        });
    }

    public boolean drag(int startx, int starty, int endx, int endy, long ms) {
        return drag(startx, starty, endx, endy, (int) (ms / 10), ms);
    }

    public boolean drag(int startx, int starty, int endx, int endy, int steps, long ms) {
        final long iterationTime = ms / (long) steps;
        LinearInterpolator lerp = new LinearInterpolator(steps);
        Point start = new Point(startx, starty);
        Point end = new Point(endx, endy);
        lerp.interpolate(start, end, new LinearInterpolator.Callback() {
            @Override
            public void start(Point point) {
                try {
                    sendCommand("touch down " + point.getX() + " " + point.getY());
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            @Override
            public void step(Point point) {
                try {
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag step event", e);
                }
            }

            @Override
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


    public boolean dragAngle(int startx, int starty, int endx, int endy) {
        return dragAngle(startx, starty, endx, endy, 30.0, (long) (endx - startx) * 10);
    }


    public boolean dragAngle(int startx, int starty, int endx, int endy, double angle) {
        return dragAngle(startx, starty, endx, endy, angle, (long) (endx - startx) * 10);
    }

    public boolean dragAngle(int startx, int starty, int endx, int endy, double angle, long ms) {
        final long iterationTime = ms / (long) (endx - startx);
        AngleInterpolator interpolator = new AngleInterpolator(angle);
        Point start = new Point(startx, starty);
        Point end = new Point(endx, endy);
        interpolator.interpolate(start, end, new AngleInterpolator.Callback() {
            @Override
            public void start(Point point) {
                try {
                    sendCommand("touch down " + point.getX() + " " + point.getY());
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            @Override
            public void step(Point point) {
                try {
                    sendCommand("touch move " + point.getX() + " " + point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            @Override
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

    public void dragAsync(int startx, int starty, int endx, int endy, long ms) {
        dragAsync(startx, starty, endx, endy, (int) (ms / 10), ms);
    }

    public void dragAsync(int startx, int starty, int endx, int endy, int step, long ms) {
        executor.submit(new Runnable() {
            @Override
            public void run() {
                drag(startx, starty, endx, endy, step, ms);
            }
        });
    }


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


    public boolean sendCommand(String command) {
        String monkeyResponse = sendCommand0(command);
        return this.parseResponse(monkeyResponse);
    }

    public void sendCommandAsync(String command) {
        executor.submit(new Runnable() {
            @Override
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
            logger.error("send Event[" + command + "] error! ", e);
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

    public void stop() {
        try {
            executor.shutdown();
        } catch (Exception e) {
            logger.error("Close monkey error !" + e.getMessage());
        }
        try {
            this.sendCommand("quit");
            this.monkeyWriter.close();
            this.monkeySocket.shutdownOutput();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
        try {
            this.monkeyReader.close();
            this.monkeySocket.shutdownInput();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
        try {
            this.monkeySocket.close();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
    }

}
