/**
 *
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.net.URI;
import java.util.Date;

import org.bushe.swing.event.EventServiceEvent;

/**
 * @author lpz
 *
 */
public class SpiderStartEvent implements EventServiceEvent {

    private Object source;
    private URI[] uri;
    private Date start;
    private String[] methods;
    private String[] parameters;

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
    }
    
    public String[] getMethods() {
        return methods;
    }

    public Date getStart() {
        return start;
    }

    public SpiderStartEvent(Object source, URI[] uri, String[] methods, Date date) {
        this.source = source;
        this.uri = uri;
        this.start = date;
        this.methods = methods;
        this.parameters = new String[uri.length];
    }
    /* (non-Javadoc)
     * @see org.bushe.swing.event.EventServiceEvent#getSource()
     */

    public Object getSource() {
        return source;
    }

    public URI[] getURIs() {
        return this.uri;
    }
}
