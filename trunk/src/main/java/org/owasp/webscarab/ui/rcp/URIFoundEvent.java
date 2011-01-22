/**
 *
 */
package org.owasp.webscarab.ui.rcp;

import java.net.URI;
import java.util.Date;

import org.bushe.swing.event.EventServiceEvent;

/**
 * @author lpz
 *
 */
public class URIFoundEvent implements EventServiceEvent {

    private Object source;
    private URI uri;
    private Date start;
    
    public URIFoundEvent(Object source, URI uri, Date start) {
        this.source = source;
        this.uri = uri;
        this.start = start;
    }
    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */
    public Object getSource() {
        return source;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getStart() {
        return this.start;
    }
    
    public URI getURI() {
        return this.uri;
    }
}
