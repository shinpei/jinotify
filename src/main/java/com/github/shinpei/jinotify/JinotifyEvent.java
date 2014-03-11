package com.github.shinpei.jinotify;

public enum JinotifyEvent {

    ACCESS(Clib.InotifyConstants.ACCESS),
    MODIFY(Clib.InotifyConstants.MODIFY),
    CREATE(Clib.InotifyConstants.CREATE),
    DELETE(Clib.InotifyConstants.DELETE),
    MOVE(Clib.InotifyConstants.MOVE),
    CLOSE(Clib.InotifyConstants.CLOSE);

    private final Clib.InotifyConstants value;

    JinotifyEvent(final Clib.InotifyConstants value) {

        this.value = value;
    }

    public final int value() {

        return value.value();
    }
}
