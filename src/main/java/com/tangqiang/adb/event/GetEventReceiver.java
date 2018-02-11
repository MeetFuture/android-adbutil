package com.tangqiang.adb.event;

import com.android.ddmlib.IShellOutputReceiver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * getevent 命令输出接收
 *
 * @author Tom
 * @version 1.0 2018-01-04 0004 Tom create
 * @date 2018-01-04 0004
 */
public abstract class GetEventReceiver implements IShellOutputReceiver {
    private final Logger log = LoggerFactory.getLogger("GetEvent");
    private boolean cancelled = false;
    private List<EventItem> list = new ArrayList<>(6);


    public void addOutput(byte[] data, int offset, int length) {
        long time = System.nanoTime();
        String message = new String(data, offset, length);
        String[] mes = message.split("\n");
        for (int i = 0; i < mes.length; ++i) {
            EventItem item = EventItem.from(mes[i]);
            if (item != null) {
                list.add(item);
                if (item.type == EventItem.Type.SYN && item.code == 0 && item.value == 0) {
                    RawInputEvent event = RawInputEvent.from(list);
                    event.time = time;
                    log.debug("GetEventReceiver " + event);
                    processNewEvent(event);
                    list = new ArrayList<>(6);
                }
            }
        }
    }


    @Override
    public void flush() {
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public void cancel() {
        this.cancelled = true;
    }

    public abstract void processNewEvent(RawInputEvent event);
}
