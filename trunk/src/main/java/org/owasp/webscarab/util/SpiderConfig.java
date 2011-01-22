/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.util;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import org.owasp.webscarab.dao.FormValueConfigurationDao;
import org.owasp.webscarab.dao.HeaderConfigurationDao;
import org.owasp.webscarab.domain.FormValueConfiguration;
import org.owasp.webscarab.domain.HeaderConfiguration;
import org.owasp.webscarab.domain.NamedValue;
import org.owasp.webscarab.jdbc.NamedValueDao;
import org.springframework.core.closure.Constraint;
import org.springframework.rules.PropertyConstraintProvider;
import org.springframework.rules.Rules;
import org.springframework.rules.constraint.property.PropertyConstraint;

/**
 *
 * @author Lpz
 */
public final class SpiderConfig implements PropertyConstraintProvider {

    public final static String dbPrefix = "spider_conf_";
    public final static String PROPERTY_HEADER_CONFIGURATIONS = "headerConfigurations";
    public final static String PROPERTY_FORM_VALUES_CONFIGURATIONS = "formValuesConfigurations";
    public static final String PROPERTY_MAX_THREADS = "maxThreads";
    public static final String PROPERTY_MAX_SECONDS = "maxSeconds";
    public static final String PROPERTY_AUTO_ENABLED = "autoEnabled";
    public static final String PROPERTY_FETCH_FROM_THE_SAME_HOST = "fetchFromTheSameHost";
    public static final String PROPERTY_FOLLOW_FORMS = "followForms";
    public static final String PROPERTY_FETCH_PATTERN = "fetchPattern";
    public static final String PROPERTY_ALLOWED_PATTERN = "allowedPattern";
    public static final String PROPERTY_DISALLOWED_PATTERN = "disallowedPattern";
    private String fetchPattern = "^text.html.*$";
    private String disallowedPattern = ".*(logout|delete).*";
    private String allowedPattern = ".*";
    private boolean autoEnabled = true;
    private boolean fetchFromTheSameHost = true;
    private boolean followForms = true;
    private int maxSeconds = 600;
    private int maxThreads = 16;
    private Properties connectionProperties;
    private Rules validationRules;
    List<HeaderConfiguration> headerConfigurations = new ArrayList<HeaderConfiguration>();
    List<FormValueConfiguration> formValuesConfigurations = new ArrayList<FormValueConfiguration>();
    private FormValueConfigurationDao formValueConfigurationDao;
    private HeaderConfigurationDao headerConfigurationDao;
    private NamedValueDao namedValueDao;

    public SpiderConfig() {
        initRules();
    }

    public NamedValueDao getNamedValueDao() {
        return namedValueDao;
    }

    public void setNamedValueDao(NamedValueDao namedValueDao) {
        this.namedValueDao = namedValueDao;
    }

    public FormValueConfigurationDao getFormValueConfigurationDao() {
        return formValueConfigurationDao;
    }

    public void setFormValueConfigurationDao(FormValueConfigurationDao formValueConfigurationDao) {
        this.formValueConfigurationDao = formValueConfigurationDao;
    }

    public HeaderConfigurationDao getHeaderConfigurationDao() {
        return headerConfigurationDao;
    }

    public void setHeaderConfigurationDao(HeaderConfigurationDao headerConfigurationDao) {
        this.headerConfigurationDao = headerConfigurationDao;
    }

    public List<FormValueConfiguration> getFormValuesConfigurations() {
        formValuesConfigurations.clear();
        for (FormValueConfiguration fvc : formValueConfigurationDao.getAll()) {
            formValuesConfigurations.add(fvc);
        }
        return formValuesConfigurations;
    }

    public void setFormValuesConfigurations(List<FormValueConfiguration> formValuesConfigurations) {
        this.formValuesConfigurations = formValuesConfigurations;
    }

    public List<HeaderConfiguration> getHeaderConfigurations() {
        headerConfigurations.clear();
        for (HeaderConfiguration hc : headerConfigurationDao.getAll()) {
            headerConfigurations.add(hc);
        }
        return headerConfigurations;
    }

    public void setHeaderConfigurations(List<HeaderConfiguration> headerConfigurations) {
        this.headerConfigurations = headerConfigurations;
    }

    public NamedValue getNamedValue(String property) {
        String name = dbPrefix + property;
        NamedValue nv = namedValueDao.findNamedValue(name);

        if (nv == null) {
            try {
                Field f = this.getClass().getDeclaredField(property);
                nv = new NamedValue(name,  f.get(this).toString());
                namedValueDao.saveNamedValue(nv);
            } catch (Exception e) {
            }

        }
        return nv;
    }

    public void setNamedValue(String property, Object o) {
        String name = dbPrefix + property;
        namedValueDao.saveNamedValue(new NamedValue(name, o.toString()));
    }

    public String getAllowedPattern() {
        return getNamedValue(PROPERTY_ALLOWED_PATTERN).getValue();
    }

    public void setAllowedPattern(String allowedPattern) {
        this.allowedPattern = allowedPattern;
        setNamedValue(PROPERTY_ALLOWED_PATTERN, allowedPattern);
    }

    public boolean isAutoEnabled() {
        return Boolean.valueOf(getNamedValue(PROPERTY_AUTO_ENABLED).getValue());
    }

    public void setAutoEnabled(boolean autoEnabled) {
        this.autoEnabled = autoEnabled;
        setNamedValue(PROPERTY_AUTO_ENABLED, autoEnabled);
    }

    public Properties getConnectionProperties() {
        return connectionProperties;
    }

    public void setConnectionProperties(Properties connectionProperties) {
        this.connectionProperties = connectionProperties;
    }

    public String getDisallowedPattern() {
        return getNamedValue(PROPERTY_DISALLOWED_PATTERN).getValue();
    }

    public void setDisallowedPattern(String disallowedPattern) {
        this.disallowedPattern = disallowedPattern;
        setNamedValue(PROPERTY_DISALLOWED_PATTERN, disallowedPattern);
    }

    public boolean isFetchFromTheSameHost() {
        return Boolean.valueOf(getNamedValue(PROPERTY_FETCH_FROM_THE_SAME_HOST).getValue());

    }

    public void setFetchFromTheSameHost(boolean fetchFromTheSameHost) {
        this.fetchFromTheSameHost = fetchFromTheSameHost;
        setNamedValue(PROPERTY_FETCH_FROM_THE_SAME_HOST, fetchFromTheSameHost);
    }

    public String getFetchPattern() {
       return getNamedValue(PROPERTY_FETCH_PATTERN).getValue();
    }

    public void setFetchPattern(String fetchPattern) {
        this.fetchPattern = fetchPattern;
        setNamedValue(PROPERTY_FETCH_PATTERN, fetchPattern);
    }

    public boolean isFollowForms() {
        return Boolean.valueOf(getNamedValue(PROPERTY_FOLLOW_FORMS).getValue());
    }

    public void setFollowForms(boolean followForms) {
        this.followForms = followForms;
         setNamedValue(PROPERTY_FOLLOW_FORMS, followForms);
    }

    public int getMaxSeconds() {
        return Integer.valueOf(getNamedValue(PROPERTY_MAX_SECONDS).getValue());
    }

    public void setMaxSeconds(int maxSeconds) {
        this.maxSeconds = maxSeconds;
        setNamedValue(PROPERTY_MAX_SECONDS, maxSeconds);
    }

    public int getMaxThreads() {
        return Integer.valueOf(getNamedValue(PROPERTY_MAX_THREADS).getValue());

    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
        setNamedValue(PROPERTY_MAX_THREADS, maxThreads);
    }

    public Rules getValidationRules() {
        return validationRules;
    }

    public void setValidationRules(Rules validationRules) {
        this.validationRules = validationRules;
    }

    /**
     * Initialize the field constraints for our properties. Minimal constraints are
     * enforced here. If you need more control, you should override this in a subtype.
     */
    protected void initRules() {
        this.validationRules = new Rules(getClass()) {

            @Override
            protected void initRules() {
                add(PROPERTY_MAX_THREADS, all(new Constraint[]{required(), gt(1)}));
                add(PROPERTY_MAX_SECONDS, all(new Constraint[]{required(), gt(1)}));
                add(PROPERTY_ALLOWED_PATTERN, all(new Constraint[]{required()}));
                add(PROPERTY_FETCH_PATTERN, all(new Constraint[]{required()}));
                // add( PROPERTY_URL, all( new Constraint[] { required() } ) );
                //add( PROPERTY_USERNAME, all( new Constraint[] { required() } ) );
//                add( PROPERTY_PASSWORD, all( new Constraint[] { required() } ) );
            }
        };
    }

    public PropertyConstraint getPropertyConstraint(String propertyName) {
        return validationRules.getPropertyConstraint(propertyName);
    }
}
