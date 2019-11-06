package com.tangqiang.android.minitouch;

import com.tangqiang.android.common.polator.AngleInterpolator;
import com.tangqiang.android.common.polator.LinearInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Minitouch客户端处理
 *
 * @author tqiang
 * @date 2019-10-13 21:40
 */
public class MinitouchClient {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private Socket socket;
    private String host = "127.0.0.1";
    private int port = 5558;
    private BufferedWriter writer;
    private BufferedReader reader;

    public MinitouchClient() {
    }

    public MinitouchClient(int port) {
        this.port = port;
    }

    public MinitouchClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() {
        try {
            logger.info("Begin client ,connect to host " + host + ":" + port);
            socket = new Socket(host, port);
            this.writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
            this.reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            new DataCollector().start();
        } catch (Exception e) {
            logger.error("client error !", e);
        }
    }


    public void stop() {
        try {
            this.socket.shutdownInput();
        } catch (Exception e) {
            logger.error("client close error !", e);
        }
        try {
            this.socket.shutdownOutput();
        } catch (Exception e) {
            logger.error("client close error !", e);
        }
        try {
            this.socket.close();
        } catch (Exception e) {
            logger.error("client close error !", e);
        }
    }

    public void tap(int x, int y) {
        down(x, y);
        waitt(200);
        up();
    }

    public void waitt(long time) {
        String cmd = "w " + time + "\nc\n";
        sendCommand0(cmd);
    }

    public void up() {
        up(0);
    }

    public void up(int contactId) {
        String cmd = "u " + contactId + "\nc\n";
        sendCommand0(cmd);
    }

    public void down(int x, int y) {
        down(0, x, y, 100);
    }

    public void down(int x, int y, int pressure) {
        down(0, x, y, pressure);
    }

    public void down(int contactId, int x, int y, int pressure) {
        String cmd = "d " + contactId + " " + x + " " + y + " " + pressure + "\nc\n";
        sendCommand0(cmd);
    }

    public void move(int x, int y) {
        move(0, x, y, 100);
    }

    public void move(int x, int y, int pressure) {
        move(0, x, y, pressure);
    }

    public void move(int contactId, int x, int y, int pressure) {
        String cmd = "m " + contactId + " " + x + " " + y + " " + pressure + "\nc\n";
        sendCommand0(cmd);
    }

    public void touch(int x, int y, long ms) {
        down(x, y);
        try {
            Thread.sleep(ms);
        } catch (Exception e) {
            e.printStackTrace();
        }
        up();
    }


    public void drag(int startx, int starty, int endx, int endy, long ms) {
        drag(startx, starty, endx, endy, (int) (ms / 10), ms);
    }

    public void drag(int startx, int starty, int endx, int endy, int steps, long ms) {
        final long iterationTime = ms / (long) steps;
        LinearInterpolator lerp = new LinearInterpolator(steps);
        Point start = new Point(startx, starty);
        Point end = new Point(endx, endy);
        lerp.interpolate(start, end, new LinearInterpolator.Callback() {
            @Override
            public void start(Point point) {
                try {
                    down((int) point.getX(), (int) point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            @Override
            public void step(Point point) {
                try {
                    move((int) point.getX(), (int) point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag step event", e);
                }
            }

            @Override
            public void end(Point point) {
                try {
                    move((int) point.getX(), (int) point.getY());
                    up();
                } catch (Exception e) {
                    logger.error("Error sending drag end event", e);
                }
            }
        });
    }

    public void dragAngle(int startx, int starty, int endx, int endy) {
        dragAngle(startx, starty, endx, endy, 30.0, (long) (endx - startx) * 10);
    }


    public void dragAngle(int startx, int starty, int endx, int endy, double angle) {
        dragAngle(startx, starty, endx, endy, angle, (long) (endx - startx) * 10);
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
                    down((int) point.getX(), (int) point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag start event", e);
                }
            }

            @Override
            public void step(Point point) {
                try {
                    move((int) point.getX(), (int) point.getY());
                    Thread.sleep(iterationTime);
                } catch (Exception e) {
                    logger.error("Error sending drag step event", e);
                }
            }

            @Override
            public void end(Point point) {
                try {
                    move((int) point.getX(), (int) point.getY());
                    up();
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
        new Thread(() -> drag(startx, starty, endx, endy, step, ms)).start();
    }

    public void sendCommand0(String command) {
        String log = command.replaceAll("\n", "  --  ");
        try {
            logger.debug("Minitouch Command: " + log);
            writer.write(command);
            writer.flush();
        } catch (Exception e) {
            logger.error("send Event[" + log + "] error! ", e);
        }

    }

    /**
     * 数据接收
     */
    private class DataCollector extends Thread {

        @Override
        public void run() {
            logger.debug("Begin collector data ......");
            try {
                while (!socket.isInputShutdown()) {
                    String line = reader.readLine();
                    // 将数据塞到队列中
                    logger.info("DataCollector receive data:" + line);
                }
            } catch (Exception e) {
                logger.error("Collector data error !" + e.getMessage(), e);
            }
            logger.debug("Collector data Stop -------------------------");
        }
    }

}
