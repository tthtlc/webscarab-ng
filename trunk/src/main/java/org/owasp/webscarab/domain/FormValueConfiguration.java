/**
 * 
 */
package org.owasp.webscarab.domain;

/**
 * Class for form values used in spider plugin
 * @author Lpz
 */
public final class FormValueConfiguration extends BaseEntity {

    public static final String PROPERTY_NAME = "formName";
    public static final String PROPERTY_VALUE = "formValue";
    public static final String PROPERTY_OVERWRITE = "overwrite";
    private String formName;
    private String formValue;
    private boolean overwrite = true;

    public String getFormName() {
        return formName;
    }

    public void setFormName(String formName) {
        this.formName = formName;
    }

    public String getFormValue() {
        return formValue;
    }

    public void setFormValue(String formValue) {
        this.formValue = formValue;
    }

    public boolean getOverwrite() {
        return overwrite;
    }

    public void setOverwrite(boolean overwrite) {
        this.overwrite = overwrite;
    }
    public FormValueConfiguration() {
        this.setFormName("new regex name");
        this.setFormValue("new value");
        this.setOverwrite(false);
    }
    public FormValueConfiguration(String name, String value, boolean overwrite) {
        this.setFormName(name);
        this.setFormValue(value);
        this.setOverwrite(overwrite);
    }
}
