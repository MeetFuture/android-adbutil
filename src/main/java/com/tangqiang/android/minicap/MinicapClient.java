package com.tangqiang.android.minicap;

import com.tangqiang.android.common.mini.Banner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.Socket;
import java.util.Arrays;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * minicap客户端
 *
 * @author tqiang
 * @date 2019-10-13 21:40
 */
public class MinicapClient {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private LinkedBlockingQueue<byte[]> dataQueue = new LinkedBlockingQueue<>();
    private ScreenObserver observer;
    private Socket socket;
    private String host = "127.0.0.1";
    private int port = 5557;

    public MinicapClient(ScreenObserver observer) {
        this.observer = observer;
    }

    public MinicapClient(int port, ScreenObserver observer) {
        this.port = port;
        this.observer = observer;
    }

    public MinicapClient(String host, int port, ScreenObserver observer) {
        this.host = host;
        this.port = port;
        this.observer = observer;
    }

    public void start() {
        try {
            logger.info("Begin client ,connect to host " + host + ":" + port);
            socket = new Socket(host, port);
            new ImageCollector().start();
            // 启动图片转换线程
            new ImageConverter().start();
        } catch (Exception e) {
            logger.error("client error !", e);
        }
    }

    public void stop() {
        try {
            this.socket.shutdownInput();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
        try {
            this.socket.shutdownOutput();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
        try {
            this.socket.close();
        } catch (Exception e) {
            logger.error("client close error !" + e.getMessage());
        }
    }

    private byte[] byteMerger(byte[] bytes1, byte[] bytes2) {
        byte[] bytes = new byte[bytes1.length + bytes2.length];
        System.arraycopy(bytes1, 0, bytes, 0, bytes1.length);
        System.arraycopy(bytes2, 0, bytes, bytes1.length, bytes2.length);
        return bytes;
    }

    /**
     * 数据接收
     */
    private class ImageCollector extends Thread {

        @Override
        public void run() {
            logger.debug("Begin collector data ......");
            try (InputStream stream = socket.getInputStream()) {
                while (!socket.isInputShutdown()) {
                    byte[] buffer = new byte[10240];
                    int realLen = stream.read(buffer);
                    if (realLen != -1) {
                        // 将数据塞到队列中
                        dataQueue.offer(Arrays.copyOfRange(buffer, 0, realLen));
                    }
                }
            } catch (Exception e) {
                logger.error("Collector data error !" + e.getMessage(), e);
            }
            logger.debug("Collector data Stop -------------------------");
        }
    }


    /**
     * 数据转换为图片
     */
    private class ImageConverter extends Thread {
        private Banner banner = new Banner();
        private int readBannerBytes = 0;
        private int bannerLength = 2;
        private int readFrameBytes = 0;
        private int frameBodyLength = 0;
        private byte[] frameBody = new byte[0];


        @Override
        public void run() {
            logger.info("Begin converter image ......");
            while (!socket.isInputShutdown()) {
                try {
                    // 从队列获取数据解析
                    byte[] buffer = dataQueue.poll(1, TimeUnit.SECONDS);
                    if (buffer == null) {
                        continue;
                    }
                    if (dataQueue.size() == 200) {
                        logger.warn("Screen frame size : " + dataQueue.size());
                    }

                    long start = System.currentTimeMillis();
                    int len = buffer.length;
                    for (int cursor = 0; cursor < len; ) {
                        int byte10 = buffer[cursor] & 0xff;
                        if (readBannerBytes < bannerLength) {
                            cursor = parserBanner(cursor, byte10);
                        } else if (readFrameBytes < 4) {
                            // 第二次的缓冲区中前4位数字和为frame的缓冲区大小
                            frameBodyLength += (byte10 << (readFrameBytes * 8)) >>> 0;
                            cursor += 1;
                            readFrameBytes += 1;
                        } else {
                            if (len - cursor >= frameBodyLength) {
                                byte[] subByte = Arrays.copyOfRange(buffer, cursor, cursor + frameBodyLength);
                                frameBody = byteMerger(frameBody, subByte);
                                if ((frameBody[0] != -1) || frameBody[1] != -40) {
                                    logger.error("Frame body does not start with JPG header");
                                    return;
                                }
                                byte[] finalBytes = Arrays.copyOfRange(frameBody, 0, frameBody.length);
                                BufferedImage image = createImageFromByte(finalBytes);
                                observer.update(image);
                                long current = System.currentTimeMillis();
                                //logger.debug("图片已生成,耗时: " + (current - start) + "/ms");
                                start = current;
                                cursor += frameBodyLength;
                                restore();
                            } else {
                                //logger.debug("所需数据大小 : " + frameBodyLength);
                                byte[] subByte = Arrays.copyOfRange(buffer, cursor, len);
                                frameBody = byteMerger(frameBody, subByte);
                                frameBodyLength -= (len - cursor);
                                readFrameBytes += (len - cursor);
                                cursor = len;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("转换图片异常!" + e.getMessage(), e);
                }
            }
            logger.info("Converter image end ------------------");
        }

        private void restore() {
            frameBodyLength = 0;
            readFrameBytes = 0;
            frameBody = new byte[0];
        }

        private BufferedImage createImageFromByte(byte[] binaryData) {
            BufferedImage bufferedImage = null;
            try (InputStream in = new ByteArrayInputStream(binaryData)) {
                bufferedImage = ImageIO.read(in);
                if (bufferedImage == null) {
                    logger.debug("bufferimage为空");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bufferedImage;
        }

        private int parserBanner(int cursor, int byte10) {
            switch (readBannerBytes) {
                case 0:
                    // version
                    banner.setVersion(byte10);
                    break;
                case 1:
                    // length
                    bannerLength = byte10;
                    banner.setLength(byte10);
                    break;
                case 2:
                case 3:
                case 4:
                case 5:
                    // pid
                    int pid = banner.getPid();
                    pid += (byte10 << ((readBannerBytes - 2) * 8)) >>> 0;
                    banner.setPid(pid);
                    break;
                case 6:
                case 7:
                case 8:
                case 9:
                    // real width
                    int realWidth = banner.getReadWidth();
                    realWidth += (byte10 << ((readBannerBytes - 6) * 8)) >>> 0;
                    banner.setReadWidth(realWidth);
                    break;
                case 10:
                case 11:
                case 12:
                case 13:
                    // real height
                    int realHeight = banner.getReadHeight();
                    realHeight += (byte10 << ((readBannerBytes - 10) * 8)) >>> 0;
                    banner.setReadHeight(realHeight);
                    break;
                case 14:
                case 15:
                case 16:
                case 17:
                    // virtual width
                    int virtualWidth = banner.getVirtualWidth();
                    virtualWidth += (byte10 << ((readBannerBytes - 14) * 8)) >>> 0;
                    banner.setVirtualWidth(virtualWidth);

                    break;
                case 18:
                case 19:
                case 20:
                case 21:
                    // virtual height
                    int virtualHeight = banner.getVirtualHeight();
                    virtualHeight += (byte10 << ((readBannerBytes - 18) * 8)) >>> 0;
                    banner.setVirtualHeight(virtualHeight);
                    break;
                case 22:
                    // orientation
                    banner.setOrientation(byte10 * 90);
                    break;
                case 23:
                    // quirks
                    banner.setQuirks(byte10);
                    break;
                default:
                    break;
            }

            cursor += 1;
            readBannerBytes += 1;

            if (readBannerBytes == bannerLength) {
                logger.debug(banner.toString());
            }
            return cursor;
        }

    }

}
