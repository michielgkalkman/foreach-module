package org.taHjaj.wo;

import org.apache.maven.plugin.logging.Log;
import org.codehaus.plexus.logging.Logger;

public class LogLogger implements Logger {
    private final Log log;

    public LogLogger(Log log) {
        this.log = log;
    }

    @Override
    public void debug(String message) {
        log.debug(message);
    }

    @Override
    public void debug(String message, Throwable throwable) {
        log.debug(message,throwable);
    }

    @Override
    public boolean isDebugEnabled() {
        return log.isDebugEnabled();
    }

    @Override
    public void info(String message) {
        log.info(message);
    }

    @Override
    public void info(String message, Throwable throwable) {
        log.info(message,throwable);
    }

    @Override
    public boolean isInfoEnabled() {
        return log.isInfoEnabled();
    }

    @Override
    public void warn(String message) {
        log.warn(message);
    }

    @Override
    public void warn(String message, Throwable throwable) {
        log.warn(message,throwable);
    }

    @Override
    public boolean isWarnEnabled() {
        return log.isWarnEnabled();
    }

    @Override
    public void error(String message) {
        log.error(message);
    }

    @Override
    public void error(String message, Throwable throwable) {
        log.error(message,throwable);
    }

    @Override
    public boolean isErrorEnabled() {
        return log.isErrorEnabled();
    }

    @Override
    public void fatalError(String message) {
        log.error("FATAL: " + message);
    }

    @Override
    public void fatalError(String message, Throwable throwable) {
        log.error("FATAL: " + message, throwable);
    }

    @Override
    public boolean isFatalErrorEnabled() {
        return log.isErrorEnabled();
    }

    private int threshold;

    @Override
    public int getThreshold() {
        return threshold;
    }

    @Override
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    @Override
    public Logger getChildLogger(String name) {
        return null;
    }

    @Override
    public String getName() {
        return getClass().getName();
    }
}
