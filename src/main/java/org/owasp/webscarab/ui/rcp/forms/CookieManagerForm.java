/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.ui.rcp.forms;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import org.owasp.webscarab.domain.CookieConfiguration;
import org.owasp.webscarab.domain.WebScarabCookieManager;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.command.ActionCommand;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.AbstractTableMasterForm;
import org.springframework.richclient.form.builder.TableFormBuilder;
import org.springframework.richclient.util.GuiStandardUtils;

/**
 *
 * @author Lpz
 */
public final class CookieManagerForm extends AbstractTableMasterForm {

    private static final String FORM_ID = "cookieManagerForm";
    private static final String DETAIL_FORM_ID = "cookieManagerForm";
    WebScarabCookieManager cookieManager;
    HierarchicalFormModel model;
    JButton jbtCommit;
    JButton jbtCancel;
    
    public CookieManagerForm(HierarchicalFormModel model, WebScarabCookieManager cm) {
        super(model, WebScarabCookieManager.PROPERTY_COOKIES, FORM_ID,
                CookieConfiguration.class);
        this.setCookieManager(cm);
        this.model = model;
        setConfirmDelete(false);
    }

    public void setCookieManager(WebScarabCookieManager cookieManager) {
        this.cookieManager = cookieManager;
    }

    @Override
    protected AbstractDetailForm createDetailForm(HierarchicalFormModel parentFormModel,
            ValueModel valueModel, ObservableList observableList) {
            return new AbstractDetailForm(parentFormModel, DETAIL_FORM_ID, valueModel, observableList) {

            protected JComponent createFormControl() {
                JPanel panel = getComponentFactory().createPanel(new BorderLayout());
                TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
                builder.add(CookieConfiguration.PROPERTY_URI);
                builder.row();
                builder.add(CookieConfiguration.PROPERTY_VALUE);
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
                Action actionCommit = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        CookieManagerForm.this.model.commit();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                    }
                };
                Action actionCancel = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        CookieManagerForm.this.model.revert();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                        
                    }
                };
                jbtCommit = new JButton(actionCommit);
                jbtCancel = new JButton(actionCancel);
                jbtCommit.setEnabled(false);
                jbtCommit.setText("Save");
                jbtCancel.setText("Cancel");
                jbtCancel.setEnabled(false);
                buttonBar.add(jbtCommit);
                buttonBar.add(jbtCancel);
                return buttonBar;
            }
        };
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[]{CookieConfiguration.PROPERTY_URI, CookieConfiguration.PROPERTY_VALUE};
    }
}
