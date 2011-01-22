/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.owasp.webscarab.domain;

import java.net.URI;
import org.bushe.swing.event.EventServiceEvent;

/**
 *
 * @author Lpz
 */
public class SetCookieEvent implements EventServiceEvent {
    private Object source;
    private URI uri;
    private String cookieValue;
   

    public SetCookieEvent(Object source, URI uri, String cookieValue) {
        this.source = source;
        this.uri = uri;
        this.cookieValue = cookieValue;
    }
    public Object getSource() {
        return source;
    }
    public URI getUri() {
        return uri;
    }
    public String getCookieValue() {
        return cookieValue;
    }

}
