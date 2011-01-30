/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.console;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.LogRecord;

/**
 *
 * @author Lpz
 */
public class WebScarabLogRecord extends LogRecord implements Comparable<WebScarabLogRecord> {

    private final String logFormat = "%s %s - %s: %s %s %s \n";

    public WebScarabLogRecord(Level level, String msg) {
        super(level, msg);

    }

    public WebScarabLogRecord(LogRecord record) {
        super(record.getLevel(), record.getMessage());
        this.setLoggerName(record.getLoggerName());
        this.setSourceClassName(record.getSourceClassName());
        this.setSourceMethodName(record.getSourceMethodName());
        this.setThrown(record.getThrown());
        this.setParameters(record.getParameters());
    }
    public String getRealMessage() {
        if(this.getThrown()==null)
            return this.getMessage();
        return this.getThrown().getMessage();
    }
    public String toFullString() {
        Date d = new Date(this.getMillis());
        return String.format(logFormat, d.toString(), this.getLevel(),
                getSourceClassName(), getSourceMethodName(), getLoggerName(), getRealMessage());
    }

    public int compareTo(WebScarabLogRecord o) {
        if (this.getMillis() > o.getMillis()) {
            return 1;
        } else if (this.getMillis() < o.getMillis()) {
            return -1;
        }
        return 0;
    }
}
