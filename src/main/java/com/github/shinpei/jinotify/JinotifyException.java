package com.github.shinpei.jinotify;

public class JinotifyException extends Exception {
    JinotifyException(String msg) {
        super(msg);
    }
    JinotifyException(String msg, Throwable e) {
        super(msg, e);
    }
    JinotifyException(Throwable e) {
        super(e);
    }
}
