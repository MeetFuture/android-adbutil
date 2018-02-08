package com.tangqiang.adblib.core.image;

import java.nio.ByteBuffer;

/**
 * TODO
 *
 * @author Tom
 * @version 1.0 2018-01-03 0003 Tom create
 * @date 2018-01-03 0003
 *
 */
public class FrameBufferImage {
    public int version;
    public int bpp;
    public int size;
    public int width;
    public int height;
    public int redOffset;
    public int redLength;
    public int blueOffset;
    public int blueLength;
    public int greenOffset;
    public int greenLength;
    public int alphaOffset;
    public int alphaLength;
    public byte[] data;
    private int dataOffset = 0;

    public FrameBufferImage() {
    }

    public static int getHeaderSize(int version) {
        switch (version) {
            case 1:
                return 13;
            case 16:
                return 4;
            default:
                return 0;
        }
    }

    private static int getMask(int var0) {
        return (1 << var0) - 1;
    }

    public boolean readHeader(ByteBuffer buffer) {
        int version = buffer.getInt();
        this.version = version;
        if (version == 16) {
            this.bpp = 16;
            this.size = buffer.getInt();
            this.width = buffer.getInt();
            this.height = buffer.getInt();
            this.redOffset = 11;
            this.redLength = 5;
            this.greenOffset = 5;
            this.greenLength = 6;
            this.blueOffset = 0;
            this.blueLength = 5;
            this.alphaOffset = 0;
            this.alphaLength = 0;
            this.data = new byte[this.size];
        } else {
            if (version != 1) {
                return false;
            }
            this.bpp = buffer.getInt();
            this.size = buffer.getInt();
            this.width = buffer.getInt();
            this.height = buffer.getInt();
            this.redOffset = buffer.getInt();
            this.redLength = buffer.getInt();
            this.blueOffset = buffer.getInt();
            this.blueLength = buffer.getInt();
            this.greenOffset = buffer.getInt();
            this.greenLength = buffer.getInt();
            this.alphaOffset = buffer.getInt();
            this.alphaLength = buffer.getInt();
            this.data = new byte[this.size];
        }
        return true;
    }


    public boolean readData(ByteBuffer buffer, int len) {
        buffer.get(data, dataOffset, len);
        dataOffset += len;
        return true;
    }

    public int getDataNeed() {
        return size - dataOffset;
    }

    public int getRedMask() {
        return this.getMask(this.redLength, this.redOffset);
    }

    public int getGreenMask() {
        return this.getMask(this.greenLength, this.greenOffset);
    }

    public int getBlueMask() {
        return this.getMask(this.blueLength, this.blueOffset);
    }

    public FrameBufferImage getRotated() {
        FrameBufferImage bufferImage = new FrameBufferImage();
        bufferImage.version = this.version;
        bufferImage.bpp = this.bpp;
        bufferImage.size = this.size;
        bufferImage.redOffset = this.redOffset;
        bufferImage.redLength = this.redLength;
        bufferImage.blueOffset = this.blueOffset;
        bufferImage.blueLength = this.blueLength;
        bufferImage.greenOffset = this.greenOffset;
        bufferImage.greenLength = this.greenLength;
        bufferImage.alphaOffset = this.alphaOffset;
        bufferImage.alphaLength = this.alphaLength;
        bufferImage.width = this.height;
        bufferImage.height = this.width;
        int var2 = this.data.length;
        bufferImage.data = new byte[var2];
        int var3 = this.bpp >> 3;
        int var4 = this.width;
        int var5 = this.height;

        for (int var6 = 0; var6 < var5; ++var6) {
            for (int var7 = 0; var7 < var4; ++var7) {
                System.arraycopy(this.data, (var6 * var4 + var7) * var3, bufferImage.data, ((var4 - var7 - 1) * var5 + var6) * var3, var3);
            }
        }

        return bufferImage;
    }

    public int getARGB(int var1) {
        int var2;
        if (this.bpp == 16) {
            var2 = this.data[var1] & 255;
            var2 |= this.data[var1 + 1] << 8 & '\uff00';
        } else {
            if (this.bpp != 32) {
                throw new UnsupportedOperationException("FrameBufferImage.getARGB(int) only works in 16 and 32 bit mode.");
            }

            var2 = this.data[var1] & 255;
            var2 |= (this.data[var1 + 1] & 255) << 8;
            var2 |= (this.data[var1 + 2] & 255) << 16;
            var2 |= (this.data[var1 + 3] & 255) << 24;
        }

        int var3 = (var2 >>> this.redOffset & getMask(this.redLength)) << 8 - this.redLength;
        int var4 = (var2 >>> this.greenOffset & getMask(this.greenLength)) << 8 - this.greenLength;
        int var5 = (var2 >>> this.blueOffset & getMask(this.blueLength)) << 8 - this.blueLength;
        int var6;
        if (this.alphaLength == 0) {
            var6 = 255;
        } else {
            var6 = (var2 >>> this.alphaOffset & getMask(this.alphaLength)) << 8 - this.alphaLength;
        }

        return var6 << 24 | var3 << 16 | var4 << 8 | var5;
    }

    private int getMask(int var1, int var2) {
        int var3 = getMask(var1) << var2;
        return this.bpp == 32 ? Integer.reverseBytes(var3) : var3;
    }
}
