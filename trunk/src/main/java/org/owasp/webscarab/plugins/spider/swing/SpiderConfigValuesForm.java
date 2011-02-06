/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.spider.swing;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.owasp.webscarab.domain.FormValueConfiguration;
import org.owasp.webscarab.util.SpiderConfig;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.AbstractTableMasterForm;
import org.springframework.richclient.form.builder.TableFormBuilder;
import org.springframework.richclient.util.GuiStandardUtils;

/**
 * 
 * @author Lpz
 */
public class SpiderConfigValuesForm extends AbstractTableMasterForm {

    private static final String FORM_ID = "spiderConfigValuesForm";
    private static final String DETAIL_FORM_ID = "spiderConfigValuesForm";


    HierarchicalFormModel model;
    JButton jbtCommit;
    JButton jbtCancel;

    SpiderConfigValuesForm(HierarchicalFormModel model) {
        super(model, SpiderConfig.PROPERTY_FORM_VALUES_CONFIGURATIONS, FORM_ID,
				FormValueConfiguration.class);
		setConfirmDelete(false);
                this.model = model;
    }
     @Override
    protected String[] getColumnPropertyNames() {
        return new String[] { FormValueConfiguration.PROPERTY_NAME, FormValueConfiguration.PROPERTY_VALUE, FormValueConfiguration.PROPERTY_OVERWRITE};
    }

    @Override
    protected AbstractDetailForm createDetailForm(HierarchicalFormModel parentFormModel, ValueModel valueModel, ObservableList observableList) {
        return new AbstractDetailForm(parentFormModel, DETAIL_FORM_ID, valueModel, observableList) {

            protected JComponent createFormControl() {
                JPanel panel = getComponentFactory().createPanel(new BorderLayout());
                TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
                builder.add( FormValueConfiguration.PROPERTY_NAME);
                builder.row();
                builder.add( FormValueConfiguration.PROPERTY_VALUE);
                builder.row();
                builder.add( FormValueConfiguration.PROPERTY_OVERWRITE);
                builder.row();
                updateControlsForState();
                panel.add(builder.getForm(), BorderLayout.CENTER);
                panel.add(this.createButtonBar(), BorderLayout.SOUTH);
                return panel;
            }

            @Override
            public void creatingNewObject() {
                jbtCancel.setVisible(true);
                jbtCancel.setEnabled(true);
                jbtCommit.setVisible(true);
                jbtCommit.setEnabled(true);
            }

            @Override
            protected void setEditState(int editState) {
                if (editState == this.STATE_EDIT) {
                    jbtCancel.setEnabled(true);
                    jbtCommit.setEnabled(true);
                } else if (editState == this.STATE_CLEAR) {
                    jbtCommit.setEnabled(false);
                    jbtCancel.setEnabled(false);
                }
            }

            @Override
            protected JComponent createButtonBar() {
                JComponent buttonBar = getComponentFactory().createPanel();
                GuiStandardUtils.attachDialogBorder(buttonBar);
                AbstractAction actionCommit = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        SpiderConfigValuesForm.this.model.commit();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                    }
                };
                AbstractAction actionCancel = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        SpiderConfigValuesForm.this.model.revert();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);

                    }
                };
                jbtCommit = new JButton(actionCommit);
                jbtCancel = new JButton(actionCancel);
                jbtCommit.setEnabled(false);
                jbtCommit.setText(getMessage("spider.configHeader.save.label"));
                jbtCancel.setText(getMessage("spider.configHeader.cancel.label"));
                jbtCancel.setEnabled(false);
                buttonBar.add(jbtCommit);
                buttonBar.add(jbtCancel);
                return buttonBar;
            }
        };
    }

}
