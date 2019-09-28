package com.tangqiang.core;

import com.tangqiang.monkey.types.MonkeyButton;

/**
 * monkey device 接口
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public interface IMonkeyDevice extends IMyDevice {

    /**
     * 按键
     */
    boolean press(MonkeyButton button);

    /**
     * 按键
     */
    boolean press(MonkeyButton button, long pressTime);

    void pressAsync(MonkeyButton button);

    void pressAsync(MonkeyButton button, long pressTime);


    boolean drag(int startx, int starty, int endx, int endy, long ms);

    /**
     * 拖拽
     */
    boolean drag(int startx, int starty, int endx, int endy, int step, long ms);

    boolean dragAngle60(int startx, int starty, int endx, int endy);

    boolean dragAngle60(int startx, int starty, int endx, int endy, long ms);

    /**
     * 拖拽
     */
    void dragAsync(int startx, int starty, int endx, int endy, long ms);

    void dragAsync(int startx, int starty, int endx, int endy, int step, long ms);


    boolean sendCommand(String command);

    void sendCommandAsync(String command);
}
