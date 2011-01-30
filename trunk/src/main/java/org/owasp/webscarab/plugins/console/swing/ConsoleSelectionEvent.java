/**
 *
 */
package org.owasp.webscarab.plugins.console.swing;

import java.util.logging.LogRecord;
import org.bushe.swing.event.EventServiceEvent;

/**
 * @author lpz
 *
 */
public class ConsoleSelectionEvent implements EventServiceEvent {

    private Object source;

    private LogRecord[] selection;

    public ConsoleSelectionEvent(Object source, LogRecord[] selection) {
        this.source = source;
        this.selection = selection;
    }

    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */
    public Object getSource() {
        return this.source;
    }

    public LogRecord[] getSelection() {
        return this.selection;
    }
}
