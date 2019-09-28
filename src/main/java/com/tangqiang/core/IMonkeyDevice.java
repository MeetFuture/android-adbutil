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


    /**
     * 直线拖拽
     *
     * @param startx 开始
     * @param starty
     * @param endx   结束
     * @param endy
     * @param ms     耗时
     * @return
     */
    boolean drag(int startx, int starty, int endx, int endy, long ms);

    /**
     * 直线拖拽
     *
     * @param startx 开始
     * @param starty
     * @param endx   结束
     * @param endy
     * @param step   分步
     * @param ms     耗时
     * @return
     */
    boolean drag(int startx, int starty, int endx, int endy, int step, long ms);

    /**
     * 弧度拖拽
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    boolean dragAngle(int startx, int starty, int endx, int endy);

    /**
     * 弧度拖拽
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @param angle  角度 30 180
     * @return
     */
    boolean dragAngle(int startx, int starty, int endx, int endy, double angle);

    /**
     * 弧度拖拽
     *
     * @param startx
     * @param starty
     * @param endx
     * @param endy
     * @return
     */
    boolean dragAngle(int startx, int starty, int endx, int endy, double angle, long ms);

    /**
     * 拖拽
     */
    void dragAsync(int startx, int starty, int endx, int endy, long ms);

    void dragAsync(int startx, int starty, int endx, int endy, int step, long ms);


    boolean sendCommand(String command);

    void sendCommandAsync(String command);
}
