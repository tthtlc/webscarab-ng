/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.plugins.request.swing;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import org.owasp.webscarab.domain.FuzzingVariable;
import org.owasp.webscarab.plugins.request.FuzzingModel;
import org.springframework.binding.form.HierarchicalFormModel;
import org.springframework.binding.value.ValueModel;
import org.springframework.binding.value.support.ObservableList;
import org.springframework.richclient.form.AbstractDetailForm;
import org.springframework.richclient.form.AbstractTableMasterForm;
import org.springframework.richclient.form.builder.TableFormBuilder;
import org.springframework.richclient.util.GuiStandardUtils;

/**
 * @author Lpz
 */
public final class FuzzingVariablesForm extends AbstractTableMasterForm implements ActionListener {

    private static final String FORM_ID = "fuzzingVariablesForm";
    private static final String DETAIL_FORM_ID = "fuzzingVariablesForm";
    private HierarchicalFormModel model;
    
    JButton jbtCommit;
    JButton jbtCancel;
    JButton jbtBrowse;
    
    JComboBox combo;

    public FuzzingVariablesForm(HierarchicalFormModel model)  {
        super(model, "fuzzingVariables", FORM_ID, FuzzingVariable.class);
        this.model = model;
        setConfirmDelete(false);
    }
    public FuzzingModel GetFuzzingModel() {
        return (FuzzingModel) getApplicationContext().getBean("fuzzingModel");
    }
    @Override
    protected AbstractDetailForm createDetailForm(HierarchicalFormModel parentFormModel,
            ValueModel valueModel, ObservableList observableList) {
        return new AbstractDetailForm(parentFormModel, DETAIL_FORM_ID, valueModel, observableList) {

            protected JComponent createFormControl() {
                JPanel panel = getComponentFactory().createPanel(new BorderLayout());
                TableFormBuilder builder = new TableFormBuilder(getBindingFactory());
                builder.add(FuzzingVariable.PROPERTY_NAME);
                builder.row();
                builder.add(FuzzingVariable.PROPERTY_PATH);
                builder.row();
                updateControlsForState();
                panel.add(this.createFuzzingTypeForm(), BorderLayout.SOUTH);
                panel.add(builder.getForm(), BorderLayout.NORTH);
                panel.add(this.createButtonBar(), BorderLayout.CENTER);
                return panel;

            }

            private JPanel createFuzzingTypeForm() {
                JPanel panel = getComponentFactory().createPanel(new BorderLayout());
                panel.add(new JLabel("Paramater match method"), BorderLayout.WEST); //TODO: to messages
                panel.add(new JSeparator(), BorderLayout.NORTH); //TODO: to messages
                combo = new JComboBox(FuzzingModel.GetFuzzingModes().toArray());
                combo.setSelectedItem(GetFuzzingModel().getFuzzingMode().toString());
                combo.addActionListener(FuzzingVariablesForm.this);
                panel.add(combo, BorderLayout.EAST);
                return panel;
            }

            @Override
            public void creatingNewObject() {
                jbtCancel.setVisible(true);
                jbtCancel.setEnabled(true);
                jbtCommit.setVisible(true);
                jbtCommit.setEnabled(true);
                jbtBrowse.setVisible(true);
                jbtBrowse.setEnabled(true);
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
                        FuzzingVariablesForm.this.model.commit();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                        jbtBrowse.setEnabled(false);
                    }
                };
                Action actionBrowse = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        JFileChooser chooser = new JFileChooser();
                        int returnVal = chooser.showOpenDialog(FuzzingVariablesForm.this.jbtBrowse);
                        if (returnVal == JFileChooser.APPROVE_OPTION) {
                            updateFileTextField(FuzzingVariablesForm.this.getDetailForm(), chooser.getSelectedFile());
                        }
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                        jbtBrowse.setEnabled(false);

                    }
                };
                Action actionCancel = new AbstractAction() {

                    public void actionPerformed(ActionEvent e) {
                        FuzzingVariablesForm.this.model.revert();
                        jbtCancel.setEnabled(false);
                        jbtCommit.setEnabled(false);
                        jbtBrowse.setEnabled(false);

                    }
                };
                jbtCommit = new JButton(actionCommit);
                jbtCancel = new JButton(actionCancel);
                jbtBrowse = new JButton(actionBrowse);
                jbtCommit.setText("Save");
                jbtCancel.setText("Cancel");
                jbtBrowse.setText("Browse");
                jbtCancel.setEnabled(false);
                jbtBrowse.setEnabled(false);
                jbtCommit.setEnabled(false);
                buttonBar.add(jbtCommit);
                buttonBar.add(jbtBrowse);
                buttonBar.add(jbtCancel);
                return buttonBar;
            }
        };
    }
    
    protected void updateFileTextField(AbstractDetailForm adf, File file) {
        int count = adf.getControl().getComponentCount();
        for (int i = 0; i < count; ++i) {
            Component jcom = adf.getControl().getComponent(i);
            if (jcom instanceof JPanel) {
                JPanel jp = (JPanel) jcom;
                int jcount = jp.getComponentCount();
                for (int j = 0; j < jcount; ++j) {
                    Component jjcom = jp.getComponent(j);
                    if ((jjcom instanceof JTextField) && jjcom.getName().equals(FuzzingVariable.PROPERTY_PATH)) {
                        JTextField jta = (JTextField) jjcom;
                        jta.setText(file.getAbsolutePath());
                    }
                }
            }
        }
    }

    @Override
    protected String[] getColumnPropertyNames() {
        return new String[]{FuzzingVariable.PROPERTY_NAME, FuzzingVariable.PROPERTY_PATH};
    }

    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==combo) {
            Object o = combo.getSelectedItem();
            GetFuzzingModel().setFuzzingMode(FuzzingModel.FuzzingModes.valueOf(o.toString()));
        }
    }
}
