package com.github.shinpei.jinotify;

public class ClibException extends Exception {
    ClibException(String msg) {
        super(msg);
    }

    ClibException(Throwable e) {
        super(e);
    }

    ClibException(String msg, Throwable e) {
        super(msg, e);
    }
}
