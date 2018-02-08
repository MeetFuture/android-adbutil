package com.tangqiang.adblib.core.expand;

import com.tangqiang.adblib.core.AdbStream;
import com.tangqiang.adblib.core.image.FrameBufferImage;
import com.tangqiang.adblib.core.image.ImageUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;


/**
 * adb.open("framebuffer:")
 *
 * @author Tom
 * @version 1.0 2018-01-02 0002 Tom create
 * @date 2018-01-02 0002
 *
 */
public class AdbFrameBufferReader extends Thread {
    private Logger logger = LoggerFactory.getLogger(getClass());
    private AdbStream stream;


    public AdbFrameBufferReader(AdbStream stream) {
        this.stream = stream;
    }


    @Override
    public void run() {
        int count = 0;
        FrameBufferImage image = null;
        while (true) {
            try {
                count++;
                byte[] frameBuffer = stream.read();

                if (count == 1) {
                    ByteBuffer packet = ByteBuffer.wrap(frameBuffer).order(ByteOrder.LITTLE_ENDIAN);

                    image = new FrameBufferImage();
                    image.readHeader(packet);

                    logger.info("frameBuffer version:" + image.version + "  bpp:" + image.bpp + "  size:"
                            + image.size + "  width:" + image.width + "  height:" + image.height
                            + "  redOffset:" + image.redOffset + "  redLength:" + image.redLength
                            + "  blueOffset:" + image.blueOffset + "  blueLength:" + image.blueLength
                            + "  greenOffset:" + image.greenOffset + "  greenLength:" + image.greenLength
                            + "  alphaOffset:" + image.alphaOffset + "  alphaLength:" + image.alphaLength
                    );
                    image.readData(packet, packet.remaining());
                } else {
                    ByteBuffer packet = ByteBuffer.wrap(frameBuffer).order(ByteOrder.LITTLE_ENDIAN);
                    image.readData(packet, packet.remaining());
                }

                int dataNeed = image.getDataNeed();
                logger.info("frameBuffer len:" + frameBuffer.length + "  count:" + count + "  dataNeed:" + dataNeed);

                if (dataNeed <= 0) {
                    BufferedImage bufferedImage = ImageUtils.convertImage(image);
                    File jpg = new File("target/tmp/ss.jpg");
                    ImageIO.write(bufferedImage, "jpg", jpg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }


}
