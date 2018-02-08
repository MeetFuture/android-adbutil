package com.tangqiang.adblib.core.image;


import java.awt.color.ColorSpace;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-03 0003 Tom create
 * @date 2018-01-03 0003
 *
 */
public class ThirtyTwoBitColorModel extends ColorModel {
    private static final int[] BITS = new int[]{8, 8, 8, 8};
    private final int alphaLength;
    private final int alphaMask;
    private final int alphaOffset;
    private final int blueMask;
    private final int blueLength;
    private final int blueOffset;
    private final int greenMask;
    private final int greenLength;
    private final int greenOffset;
    private final int redMask;
    private final int redLength;
    private final int redOffset;

    public ThirtyTwoBitColorModel(FrameBufferImage rawImage) {
        super(32, BITS, ColorSpace.getInstance(1000), true, false, 3, 0);
        this.redOffset = rawImage.redOffset;
        this.redLength = rawImage.redLength;
        this.redMask =  ImageUtils.getMask(this.redLength);
        this.greenOffset = rawImage.greenOffset;
        this.greenLength = rawImage.greenLength;
        this.greenMask =  ImageUtils.getMask(this.greenLength);
        this.blueOffset = rawImage.blueOffset;
        this.blueLength = rawImage.blueLength;
        this.blueMask =  ImageUtils.getMask(this.blueLength);
        this.alphaLength = rawImage.alphaLength;
        this.alphaOffset = rawImage.alphaOffset;
        this.alphaMask = ImageUtils.getMask(this.alphaLength);
    }

    public boolean isCompatibleRaster(Raster raster) {
        return true;
    }

    private int getPixel(Object inData) {
        byte[] data = (byte[])((byte[])inData);
        int value = data[0] & 255;
        value |= (data[1] & 255) << 8;
        value |= (data[2] & 255) << 16;
        value |= (data[3] & 255) << 24;
        return value;
    }

    public int getAlpha(Object inData) {
        int pixel = this.getPixel(inData);
        return this.alphaLength == 0 ? 255 : (pixel >>> this.alphaOffset & this.alphaMask) << 8 - this.alphaLength;
    }

    public int getBlue(Object inData) {
        int pixel = this.getPixel(inData);
        return (pixel >>> this.blueOffset & this.blueMask) << 8 - this.blueLength;
    }

    public int getGreen(Object inData) {
        int pixel = this.getPixel(inData);
        return (pixel >>> this.greenOffset & this.greenMask) << 8 - this.greenLength;
    }

    public int getRed(Object inData) {
        int pixel = this.getPixel(inData);
        return (pixel >>> this.redOffset & this.redMask) << 8 - this.redLength;
    }

    public int getAlpha(int pixel) {
        throw new UnsupportedOperationException();
    }

    public int getBlue(int pixel) {
        throw new UnsupportedOperationException();
    }

    public int getGreen(int pixel) {
        throw new UnsupportedOperationException();
    }

    public int getRed(int pixel) {
        throw new UnsupportedOperationException();
    }
}

