package com.tangqiang.android.minicap;

import java.awt.*;

/**
 * 图片生成订阅
 *
 * @author tqiang
 * @date 2019-10-14 23:11
 */
public interface ScreenObserver {
    public void update(Image image);
}