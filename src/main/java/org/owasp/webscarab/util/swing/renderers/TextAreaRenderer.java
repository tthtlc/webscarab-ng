/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.owasp.webscarab.util.swing.renderers;

import java.awt.Component;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.TableCellRenderer;

/**
 *
 * @author Lpz
 */
public class TextAreaRenderer extends JTextArea
        implements TableCellRenderer {

    public TextAreaRenderer() {
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    public Component getTableCellRendererComponent(JTable jTable,
            Object obj, boolean isSelected, boolean hasFocus, int row,
            int column) {
        setText((String) obj);
        return this;
    }
}
