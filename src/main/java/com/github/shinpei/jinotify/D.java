package com.github.shinpei.jinotify;

import org.slf4j.Logger;

public class D {
    private static Logger logger;

    D(Logger logger) {
        this.logger = logger;
    }

    final public void d(String msg) {
        logger.debug(msg);
    }

    final public void d(String fmt, Object... objs) {
        logger.debug(fmt, objs);
    }

    final public void i(String msg) {
        logger.info(msg);
    }

    final public void i(String fmt, Object... objs) {
        logger.info(fmt, objs);
    }

    final public void e(String msg) {
        logger.error(msg);
    }
    final public void e(String fmt, Object... objs) {
        logger.error(fmt, objs);
    }
}
