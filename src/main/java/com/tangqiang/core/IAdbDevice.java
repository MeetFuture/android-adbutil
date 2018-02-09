package com.tangqiang.core;

import com.tangqiang.adb.event.GetEventReceiver;
import com.tangqiang.adb.types.AdbShellButton;
import com.tangqiang.core.types.Rect;

/**
 * Adb device 接口
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public interface IAdbDevice extends IMyDevice {

    /**
     * 按键
     */
    boolean press(AdbShellButton keycode);

    /**
     * 按键
     */
    boolean press(AdbShellButton keycode, boolean longpress);

    void pressAsync(AdbShellButton keycode);

    void pressAsync(AdbShellButton keycode, boolean longpress);

    /**
     * 拖拽
     */
    boolean drag(int startX, int startY, int endX, int endY, long ms);

    /**
     * 拖拽
     */
    void dragAsync(int startx, int starty, int endx, int endy, long ms);

    /**
     * 获取设备屏幕分辨率 adb sehll wm size
     */
    Rect getScreenPhysical();

    /**
     * 获取设备虚拟的屏幕分辨率 adb shell getevent -p | grep -e '0035' -e '0036'
     */
    Rect getScreenVirtual();

    /**
     * 获取设备事件
     */
    void getEvent(GetEventReceiver receiver);

    /**
     * 获取设备事件
     *
     * @param timeout 超时时间
     */
    void getEvent(GetEventReceiver receiver, int timeout);

    /**
     * 执行shell
     */
    String shell(Object... args);

    String shell(String cmd);

    /**
     * 执行shell
     */
    String shell(String cmd, long timeout);

    /**
     * 执行shell  异步
     */
    void shellAsync(Object... args);

    void shellAsync(String cmd);

    void shellAsync(String cmd, long timeout);

}
