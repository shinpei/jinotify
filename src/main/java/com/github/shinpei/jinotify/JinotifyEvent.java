package com.github.shinpei.jinotify;

public enum JinotifyEvent {

    ACCESS(Clib.InotifyConstants.ACCESS, "ACCESS"),
    MODIFY(Clib.InotifyConstants.MODIFY, "MODIFY"),
    CREATE(Clib.InotifyConstants.CREATE, "CREATE"),
    DELETE(Clib.InotifyConstants.DELETE, "DELETE"),
    MOVE(Clib.InotifyConstants.MOVE, "MOVE"),
    CLOSE(Clib.InotifyConstants.CLOSE, "CLOSE"),
    /* Not used as jinotify event */
    ATTRIB(Clib.InotifyConstants.ATTRIB),
    CLOSE_NOWRITE(Clib.InotifyConstants.CLOSE_NOWRITE),
    CLOSE_WRITE(Clib.InotifyConstants.CLOSE_WRITE),
    DELETE_SELF(Clib.InotifyConstants.DELETE_SELF),
    MOVE_SELF(Clib.InotifyConstants.MOVE_SELF),
    MOVED_FROM(Clib.InotifyConstants.MOVED_FROM),
    MOVED_TO(Clib.InotifyConstants.MOVED_TO),
    OPEN(Clib.InotifyConstants.OPEN);



    private final Clib.InotifyConstants value;
    private final String symbolRepresentation;

    JinotifyEvent(final Clib.InotifyConstants value, final String symbol) {
        this.value = value;
        this.symbolRepresentation = symbol;
    }

    JinotifyEvent(final Clib.InotifyConstants value) {
        this.value = value;
        this.symbolRepresentation = "";
    }

    public final int value() {

        return value.value();
    }

    @Override
    public String toString() {

        return symbolRepresentation;
    }

}
