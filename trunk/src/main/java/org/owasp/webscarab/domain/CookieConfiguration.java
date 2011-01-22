/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.domain;

/**
 * Class used for configuration of cookies in webscarab cookie manager
 * @author Lpz
 */
public final class CookieConfiguration extends BaseEntity {

    public static final String PROPERTY_URI = "cookieUri";
    public static final String PROPERTY_VALUE = "cookieValue";
    public static final String PROPERTY_ID = "cookieId";
    
    String cookieUri;
    String cookieValue;

    public String getCookieUri() {
        return cookieUri;
    }

    public void setCookieUri(String cookieUri) {
        this.cookieUri = cookieUri;
    }

    public String getCookieValue() {
        return cookieValue;
    }

    public void setCookieValue(String cookieValue) {
        this.cookieValue = cookieValue;
    }

    public CookieConfiguration() {
        cookieUri = "http://";
        cookieValue = "";
    }

    public CookieConfiguration(String uri, String value) {
        setCookieUri(uri);
        setCookieValue(value);
    }
}
