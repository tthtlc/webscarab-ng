/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.spider.swing;

import javax.swing.JComponent;
import org.owasp.webscarab.util.SpiderConfig;
import org.springframework.binding.form.FormModel;
import org.springframework.richclient.form.AbstractForm;
import org.springframework.richclient.form.builder.TableFormBuilder;

/**
 *
 * @author Lpz
 */
public class SpiderConfigMainForm extends AbstractForm {

    private static final String FORM_ID = "spiderConfigMainForm";
    private SpiderConfig config;
    private JComponent fetchPatternField;
    private JComponent allowedPatternField;
    private JComponent disallowedPatternField;
    private JComponent autoEnabledField;
    private JComponent fetchFromTheSameHostField;
    private JComponent followFormsField;
    private JComponent maxSecondsField;
    private JComponent maxThreadsField;

    public SpiderConfigMainForm(FormModel model) {
        super(model, FORM_ID);
        config = (SpiderConfig) model.getFormObject();

    }

    protected JComponent createFormControl() {
        TableFormBuilder formBuilder = new TableFormBuilder(getBindingFactory());
        autoEnabledField = formBuilder.add(SpiderConfig.PROPERTY_AUTO_ENABLED)[1];
        formBuilder.row();
        followFormsField = formBuilder.add(SpiderConfig.PROPERTY_FOLLOW_FORMS)[1];
        formBuilder.row();
        maxThreadsField = formBuilder.add(SpiderConfig.PROPERTY_MAX_THREADS)[1];
        formBuilder.row();
        maxSecondsField = formBuilder.add(SpiderConfig.PROPERTY_MAX_SECONDS)[1];
        formBuilder.row();
        fetchPatternField = formBuilder.add(SpiderConfig.PROPERTY_FETCH_PATTERN)[1];
        formBuilder.row();
        fetchFromTheSameHostField = formBuilder.add(SpiderConfig.PROPERTY_FETCH_FROM_THE_SAME_HOST)[1];
        formBuilder.row();
        allowedPatternField = formBuilder.add(SpiderConfig.PROPERTY_ALLOWED_PATTERN)[1];
        formBuilder.row();
        disallowedPatternField = formBuilder.add(SpiderConfig.PROPERTY_DISALLOWED_PATTERN)[1];
        formBuilder.row();
        return formBuilder.getForm();
    }
}
