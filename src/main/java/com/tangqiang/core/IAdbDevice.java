package com.tangqiang.core;

import com.tangqiang.adb.types.AdbShellButton;

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
    boolean drag(int startx, int starty, int endx, int endy, long ms);

    /**
     * 拖拽
     */
    void dragAsync(int startx, int starty, int endx, int endy, long ms);


    /**
     * shell 执行
     */
    String shell(Object... args);

    String shell(String cmd);

    /**
     * shell 执行
     */
    String shell(String cmd, long timeout);

    /**
     * shell 执行 异步
     */
    void shellAsync(Object... args);

    void shellAsync(String cmd);

    void shellAsync(String cmd, long timeout);

}
