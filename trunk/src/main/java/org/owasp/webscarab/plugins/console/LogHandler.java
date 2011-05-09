/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.console;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import org.bushe.swing.event.EventService;

/**
 *
 * @author Lpz
 */
public class LogHandler extends Handler {
    final EventService eventService;
    
    public LogHandler(EventService es) {
        eventService = es;
    }
    @Override
    public void publish(LogRecord record) {
        //skipping unecessary logs from RCP...
        if(record.getSourceClassName().toLowerCase().endsWith("defaultimagesource"))
            return;
        if(record.getSourceClassName().toLowerCase().endsWith("defaulticonsource"))
            return;
        if(record.getSourceClassName().toLowerCase().endsWith("defaultapplicationobjectconfigurer"))
            return;
        if(record.getSourceClassName().toLowerCase().endsWith("labeledobjectsupport"))
            return;
        synchronized(this) {
            //passing record further
            eventService.publish(new LogEvent(new WebScarabLogRecord(record), this));
        }
    }

    @Override
    public void flush() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws SecurityException {
        //throw new UnsupportedOperationException("Not supported yet.");
    }
}
