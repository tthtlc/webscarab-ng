/**
 * 
 */
package org.owasp.webscarab.domain;

/**
 * Class for header values used in spider plugin
 * @author Lpz
 */
public final class HeaderConfiguration extends BaseEntity {

    public static final String PROPERTY_NAME = "headerName";
    public static final String PROPERTY_VALUE = "headerValue";
    private String headerName;
    private String headerValue;

    public HeaderConfiguration(String name, String value) {
        this.setHeaderName(name);
        this.setHeaderValue(value);
    }

    public String getHeaderName() {
        return headerName;
    }

    public void setHeaderName(String headerName) {
        this.headerName = headerName;
    }

    public String getHeaderValue() {
        return headerValue;
    }

    public void setHeaderValue(String headerValue) {
        this.headerValue = headerValue;
    }

    public HeaderConfiguration() {
    }

    public HeaderConfiguration(HeaderConfiguration config) {
        this.setHeaderName(config.getHeaderName());
        this.setHeaderValue(config.getHeaderValue());
    }
}
