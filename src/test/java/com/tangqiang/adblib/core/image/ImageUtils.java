package com.tangqiang.adblib.core.image;


import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.PixelInterleavedSampleModel;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Hashtable;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-03 0003 Tom create
 * @date 2018-01-03 0003
 *
 */
public class ImageUtils {
    private static Hashtable<?, ?> EMPTY_HASH = new Hashtable();
    private static int[] BAND_OFFSETS_32 = new int[]{0, 1, 2, 3};
    private static int[] BAND_OFFSETS_16 = new int[]{0, 1};

    private ImageUtils() {
    }

    public static BufferedImage convertImage(FrameBufferImage rawImage, BufferedImage image) {
        switch(rawImage.bpp) {
            case 16:
                return rawImage16toARGB(image, rawImage);
            case 32:
                return rawImage32toARGB(rawImage);
            default:
                return null;
        }
    }

    public static BufferedImage convertImage(FrameBufferImage rawImage) {
        return convertImage(rawImage, (BufferedImage)null);
    }

    static int getMask(int length) {
        int res = 0;

        for(int i = 0; i < length; ++i) {
            res = (res << 1) + 1;
        }

        return res;
    }

    private static BufferedImage rawImage32toARGB(FrameBufferImage rawImage) {
        DataBufferByte dataBuffer = new DataBufferByte(rawImage.data, rawImage.size);
        PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(0, rawImage.width, rawImage.height, 4, rawImage.width * 4, BAND_OFFSETS_32);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        return new BufferedImage(new ThirtyTwoBitColorModel(rawImage), raster, false, EMPTY_HASH);
    }

    private static BufferedImage rawImage16toARGB(BufferedImage image, FrameBufferImage rawImage) {
        DataBufferByte dataBuffer = new DataBufferByte(rawImage.data, rawImage.size);
        PixelInterleavedSampleModel sampleModel = new PixelInterleavedSampleModel(0, rawImage.width, rawImage.height, 2, rawImage.width * 2, BAND_OFFSETS_16);
        WritableRaster raster = Raster.createWritableRaster(sampleModel, dataBuffer, new Point(0, 0));
        //return new BufferedImage(new SixteenBitColorModel(rawImage), raster, false, EMPTY_HASH);
        return null;
    }
}

