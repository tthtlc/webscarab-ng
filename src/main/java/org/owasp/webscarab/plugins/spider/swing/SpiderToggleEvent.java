/**
 *
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.net.URI;

import org.bushe.swing.event.EventServiceEvent;

/**
 * @author lpz
 *
 */
public class SpiderToggleEvent implements EventServiceEvent {

    private Object source;
    private URI uri;

    public SpiderToggleEvent(Object source, URI uri) {
        this.source = source;
        this.uri = uri;
    }
    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */
    public Object getSource() {
        return source;
    }

    public URI getURI() {
        return this.uri;
    }
}
