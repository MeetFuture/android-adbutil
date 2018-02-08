package com.tangqiang.core;

import com.android.ddmlib.IDevice;

/**
 * android 设备接口
 *
 * @author Tom
 * @version 1.0 2018-02-08 0008 Tom create
 * @date 2018-02-08 0008
 * @copyright Copyright © 2018 Grgbanking All rights reserved.
 */
public interface IMyDevice {
    /**
     * ddmlib device
     */
    IDevice device();


    /**
     * 文字输入
     */
    boolean type(String message);

    void typeAsync(String message);

    /**
     * 触摸
     */
    boolean tap(int x, int y);

    /**
     * 异步触摸
     */
    void tapAsync(int x, int y);

    /**
     * 触摸
     */
    boolean touch(int x, int y, long ms);

    /**
     * 异步触摸
     */
    void touchAsync(int x, int y, long ms);


    /**
     * 重启
     */
    void reboot(String into);

    /**
     * 唤醒
     */
    boolean wake();

    /**
     * 关闭
     */
    void close();
}
