package com.tangqiang.adb.event;

import java.util.List;

/**
 * 执行 adb shell getevent -t 接收到的一个事件的数据的解析
 *
 * @author Tom
 * @version 1.0 2018-02-11 0011 Tom create
 * @date 2018-02-11 0011
 * @copyright Copyright © 2017 Grgbanking All rights reserved.
 */
public class RawInputEvent {

    /**
     * 事件发生时的 设备的微秒数
     **/
    public long when;
    /**
     * 事件接收时的系统的纳秒数
     */
    public long time;
    /**
     * 事件类型码
     */
    public Type type;
    /**
     * 键值
     */
    public int keyCode;
    /**
     * 值
     */
    public int value;
    /**
     * X坐标值
     */
    public int x;
    /**
     * Y坐标值
     */
    public int y;

    private RawInputEvent(long when, Type type, int keyCode, int x, int y) {
        this.when = when;
        this.type = type;
        this.keyCode = keyCode;
        // this.value = value;
        this.x = x;
        this.y = y;
    }


    public static RawInputEvent from(List<EventItem> items) {
        long when = -1;
        Type type = Type.SYNREPORT;
        int keyCode = -1;
        // int value = -1;
        int x = -1;
        int y = -1;
        for (EventItem item : items) {
            when = when == -1 ? (item.secTime * 1000000 + item.msTime) : when;
            switch (item.type) {
                case KEY:
                    keyCode = item.code;
                    if (keyCode == 330) {
                        type = item.value == 1 ? Type.TouchDown : Type.TouchUp;
                    } else {
                        type = item.value == 1 ? Type.KeyDown : Type.KeyUp;
                    }
                    break;
                case ABS:
                    if (item.code == 53) {
                        x = item.value;
                    } else if (item.code == 54) {
                        y = item.value;
                    }
                    break;
            }
        }
        type = type == Type.SYNREPORT && x != -1 ? Type.TouchMove : type;
        return new RawInputEvent(when, type, keyCode, x, y);
    }


    @Override
    public String toString() {
        return "RawInputEvent{" + "when=" + when + ", type=" + type + ", keyCode=" + keyCode + ", x=" + x + ", y=" + y + '}';
    }


    public enum Type {
        KeyDown,
        KeyUp,
        TouchDown,
        TouchUp,
        TouchMove,
        SYNREPORT
    }

}
