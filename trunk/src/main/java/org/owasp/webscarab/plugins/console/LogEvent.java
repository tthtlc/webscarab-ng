/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.owasp.webscarab.plugins.console;

import org.bushe.swing.event.EventServiceEvent;

/**
 *
 * @author Lpz
 */
public class LogEvent implements EventServiceEvent {
    private WebScarabLogRecord logRecord;
    private Object source;

    public LogEvent(WebScarabLogRecord lr, Object s) {
        logRecord = lr;
        source = s;
    }
   
    public Object getSource() {
        return source;
    }
    
    public WebScarabLogRecord getLogRecord() {
        return logRecord;
    }

}
