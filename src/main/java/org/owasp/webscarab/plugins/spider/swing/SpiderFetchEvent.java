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
public class SpiderFetchEvent implements EventServiceEvent {

    private Object source;
    private URI[] uris;
    private String[] methods;
    private String[] parameters;
    private Date start;

    public SpiderFetchEvent(Object source, URI[] uris, String[] methods, Date start) {
        this.source = source;
        this.uris = uris;
        this.start = start;
        this.methods = methods;
        this.parameters = new String[uris.length];
    }

    public SpiderFetchEvent(Object source, URI[] uris, String[] methods, Date start, String[] parameters) {
        this.source = source;
        this.uris = uris;
        this.start = start;
        this.methods = methods;
        this.parameters = parameters;
    }

    public String[] getParameters() {
        return parameters;
    }

    public void setParameters(String[] parameters) {
        this.parameters = parameters;
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

    public URI[] getURIs() {
        return this.uris;
    }

    public String[] getMethods() {
        return this.methods;
    }
}
