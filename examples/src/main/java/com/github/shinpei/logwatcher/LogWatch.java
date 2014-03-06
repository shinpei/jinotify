package com.github.shinpei.logwatcher;

import com.github.shinpei.jinotify.Jinotify;
import com.github.shinpei.jinotify.JinotifyListener;

public class LogWatch {
    static public void main(String[] args) {

        Jinotify jinotify = new Jinotify();
        class MyListener extends JinotifyListener {
            @Override
            public void onCreate() {
                System.out.println("hi");
            }
        }

        try {
            jinotify.addWatch("/tmp", new MyListener());

        } catch (Exception e) {

        }
        try {
        while(true) {
            Thread.sleep(10000);
        }
        }catch (Exception e) {

        }
        jinotify.closeNotifier();
    }
}
