package com.github.shinpei.logwatcher;

import com.github.shinpei.jinotify.Jinotify;
import com.github.shinpei.jinotify.JinotifyListener;

public class LogWatch {
    static public void main(String[] args) {

        Jinotify jinotify = new Jinotify();
        class MyListener extends JinotifyListener {
            @Override
            public void onModify() {
                System.out.println("hi");
            }
        }
        try {
            jinotify.addWatch("/var/log/system.log", Jinotify.MODIFY, new MyListener());

        } catch (Exception e) {

        }
        jinotify.closeNotifier();
    }
}
