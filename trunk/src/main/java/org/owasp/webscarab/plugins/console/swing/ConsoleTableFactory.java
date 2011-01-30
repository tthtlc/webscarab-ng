/**
 *
 */
package org.owasp.webscarab.plugins.console.swing;

import org.owasp.webscarab.ui.rcp.*;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JTable;
import javax.swing.table.TableCellRenderer;

import org.jdesktop.swingx.JXTable;
import org.owasp.webscarab.domain.Annotation;
import org.owasp.webscarab.domain.Conversation;
import org.owasp.webscarab.services.ConversationService;
import org.owasp.webscarab.util.UrlUtils;
import org.owasp.webscarab.util.swing.renderers.DateRenderer;
import org.owasp.webscarab.util.swing.renderers.TableColorProvider;
import org.owasp.webscarab.util.swing.renderers.TableColorRenderer;
import org.springframework.richclient.application.support.ApplicationServicesAccessor;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.gui.AdvancedTableFormat;
import ca.odell.glazedlists.gui.WritableTableFormat;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TableComparatorChooser;
import java.util.logging.Level;
import javax.swing.table.TableColumnModel;
import org.owasp.webscarab.plugins.console.WebScarabLogRecord;

/**
 * @author rdawes
 *
 */
public class ConsoleTableFactory extends ApplicationServicesAccessor {

    private CompoundConsoleTableFormat tableFormat = new CompoundConsoleTableFormat();
    //private ConversationService conversationService;

    public ConsoleTableFactory() {
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return Integer.class;
            }

            public String getAttributeId() {
                return "record.sequenceNumber";
            }

            public Object getValue(WebScarabLogRecord record) {
                return record.getSequenceNumber();
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return String.class;
            }

            public String getAttributeId() {
                return "record.level";
            }

            public Object getValue(WebScarabLogRecord record) {
                return record.getLevel().toString();
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            @Override
            public Class<?> getAttributeClass() {
                
                return String.class;
            }

            public String getAttributeId() {
                return "record.loggerName";
            }

            public Object getValue(WebScarabLogRecord record) {
                return record.getLoggerName();
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return Date.class;
            }

            public String getAttributeId() {
                return "record.millis";
            }

            public Object getValue(WebScarabLogRecord record) {
                return new Date(record.getMillis());
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return String.class;
            }

            public String getAttributeId() {
                return "record.sourceClassName";
            }

            public Object getValue(WebScarabLogRecord record) {
                return record.getSourceClassName();
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return String.class;
            }

            public String getAttributeId() {
                return "record.sourceMethodName";
            }

            public Object getValue(WebScarabLogRecord record) {
                return record.getSourceMethodName();
            }
        });
        tableFormat.addColumn(new ObjectAttribute<WebScarabLogRecord>() {

            public Class<?> getAttributeClass() {
                return String.class;
            }

            public String getAttributeId() {
                return "record.message";
            }

            public Object getValue(WebScarabLogRecord record) {
                if(record.getThrown()==null)
                    return record.getMessage();
                return record.getThrown().getMessage();
            }
        });
    }

    private void registerRenderersForTable(JTable table, TableColorProvider colorProvider) {
        Set<Class<?>> columnClasses = new HashSet<Class<?>>();
        for (int i = 0; i < table.getColumnCount(); i++) {
            columnClasses.add(table.getColumnClass(i));
        }
        for (Class<?> klass : columnClasses) {
            TableCellRenderer delegate = table.getDefaultRenderer(klass);
            TableCellRenderer renderer = new TableColorRenderer(delegate, colorProvider);
            table.setDefaultRenderer(klass, renderer);
        }
    }

    public JTable getConsoleTable(SortedList<WebScarabLogRecord> records) {
        JTable table = getComponentFactory().createTable();
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

        if (table instanceof JXTable) {
            JXTable jx = (JXTable) table;
            jx.setColumnControlVisible(true);
            jx.setSortable(false);
        }
        EventTableModel<WebScarabLogRecord> etm = new EventTableModel<WebScarabLogRecord>(records, tableFormat);
        table.setModel(etm);
       
        new TableComparatorChooser<WebScarabLogRecord>(table, records, true);
        table.setDefaultRenderer(Date.class, new DateRenderer());
        TableColorProvider colorProvider = new AnnotationColorProvider(records);
        registerRenderersForTable(table, colorProvider);
        int[] colwidth = {20, 40, 60};
        TableColumnModel tcm = table.getColumnModel();
        for(int i=0; i< colwidth.length; ++i) {
            tcm.getColumn(i).setPreferredWidth(colwidth[i]);
            tcm.getColumn(i).setWidth(colwidth[i]);
        }
        table.setColumnModel(tcm);
        return table;
    }

    private class CompoundConsoleTableFormat implements WritableTableFormat<WebScarabLogRecord>, AdvancedTableFormat<WebScarabLogRecord> {

        private List<ObjectAttribute<WebScarabLogRecord>> columns = new ArrayList<ObjectAttribute<WebScarabLogRecord>>();

        public void addColumn(ObjectAttribute<WebScarabLogRecord> column) {
            columns.add(column);
        }

        public Class<?> getColumnClass(int column) {
            return columns.get(column).getAttributeClass();
        }

        public Comparator<?> getColumnComparator(int column) {
            return columns.get(column).getComparator();
        }

        public int getColumnCount() {
            return columns.size();
        }

        public String getColumnName(int column) {
            String name = getMessage(columns.get(column).getAttributeId());
            if (name != null) {
                return name;
            }
            return columns.get(column).getAttributeId();
        }

        public Object getColumnValue(WebScarabLogRecord record, int column) {
            return columns.get(column).getValue(record);
        }

        public boolean isEditable(WebScarabLogRecord record, int column) {
            return columns.get(column).isAttributeEditable();
        }

        public WebScarabLogRecord setColumnValue(WebScarabLogRecord record, Object value, int column) {
            return columns.get(column).setAttribute(record, value);
        }
    }

    private class AnnotationColorProvider implements TableColorProvider {

        private EventList<WebScarabLogRecord> records;

        public AnnotationColorProvider(EventList<WebScarabLogRecord> records) {
            this.records = records;
        }

        public Color getBackGroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            return table.getBackground();
        }

        public Color getForegroundColor(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            WebScarabLogRecord record = records.get(row);
            if (record.getLevel() == Level.SEVERE) {
                return Color.RED;
            } else if (record.getLevel() == Level.WARNING) {
                return Color.ORANGE;
            }
            return Color.GRAY;
        }
    }
}
