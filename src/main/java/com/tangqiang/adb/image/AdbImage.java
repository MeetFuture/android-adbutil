package com.tangqiang.adb.image;

import com.android.ddmlib.RawImage;

import java.awt.image.BufferedImage;

/**
 * 图片
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 *
 */
public class AdbImage extends AdbImageBase {
    private final RawImage image;

    public AdbImage(RawImage image) {
        this.image = image;
    }

    @Override
    public BufferedImage createBufferedImage() {
        return ImageUtils.convertImage(this.image);
    }

    public RawImage getRawImage() {
        return this.image;
    }
}
