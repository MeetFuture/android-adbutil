## my-android-adbutil

Android adb工具拓展

* adb常用方法整理
* monkey常用方法整理

使用android系统自带的monkey快速模拟用户点击、按键、触摸



### 代码示例


```java
    AdbBackend backend = new AdbBackend();
    IDevice device = backend.getDevice();
    
    AdbDevice adbDevice = new AdbDevice(device);
    adbDevice.tap(500, 600);
    adbDevice.takeSnapshot("/screenshot.png");
    adbDevice.close();
    
    MonkeyDevice monkeyDevice = new MonkeyDevice(device);
    Collection<String> collections = monkeyDevice.listVariable();
    for (String s : collections) {
        logger.info("Variable :" + s);
    }
    Thread.sleep(1000);
    
    monkeyDevice.drag(500, 500, 900, 500, 100);
    Thread.sleep(2000);
    monkeyDevice.close();
    
    
    backend.shutdown();
```