/**
 *
 */
package org.owasp.webscarab.plugins.console.swing;

import ca.odell.glazedlists.BasicEventList;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.bushe.swing.event.EventService;
import org.bushe.swing.event.EventServiceEvent;
import org.springframework.binding.value.ValueModel;
import org.springframework.richclient.application.PageComponentContext;
import org.springframework.richclient.application.support.AbstractView;
import org.springframework.richclient.command.GuardedActionCommandExecutor;
import org.springframework.richclient.command.support.AbstractActionCommandExecutor;
import org.springframework.richclient.list.ListSelectionValueModelAdapter;
import org.springframework.richclient.list.ListSingleSelectionGuard;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.FilterList;
import ca.odell.glazedlists.SortedList;
import ca.odell.glazedlists.TextFilterator;
import ca.odell.glazedlists.matchers.MatcherEditor;
import ca.odell.glazedlists.swing.EventSelectionModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import org.bushe.swing.event.EventSubscriber;
import org.owasp.webscarab.domain.SessionEvent;
import org.owasp.webscarab.plugins.console.LogEvent;
import org.owasp.webscarab.plugins.console.LogHandler;
import org.owasp.webscarab.plugins.console.WebScarabLogRecord;
import org.owasp.webscarab.util.TableRowResizer;

/**
 * @author lpz
 *
 */
public class ConsoleView extends AbstractView implements EventSubscriber, ActionListener {

    Logger log = Logger.getLogger(this.getClass().getName());
    JFileChooser fc = new JFileChooser();
    JButton saveBtn = new JButton(getMessage("consoleView.saveBtn.label"));
    JButton clearBtn = new JButton(getMessage("consoleView.clearBtn.label"));
    LogHandler logHandler;
    private final EventList<WebScarabLogRecord> records = new SortedList<WebScarabLogRecord>(
            new BasicEventList<WebScarabLogRecord>());
    private EventService eventService;
    private ConsoleTableFactory consoleTableFactory = new ConsoleTableFactory();
    private EventSelectionModel<WebScarabLogRecord> consoleSelectionModel;


    /*


    /*
     * (non-Javadoc)
     *
     * @see org.springframework.richclient.application.support.AbstractView#createControl()
     */
    @Override
    protected JComponent createControl() {
        JTextField filterField = getComponentFactory().createTextField();

        TextFilterator<WebScarabLogRecord> filterator = new ConsoleFilter();
        MatcherEditor<WebScarabLogRecord> matcher = new TextComponentMatcherEditor<WebScarabLogRecord>(
                filterField, filterator);
        FilterList<WebScarabLogRecord> filterList = new FilterList<WebScarabLogRecord>(
                records, matcher);
        SortedList<WebScarabLogRecord> sortedList = new SortedList<WebScarabLogRecord>(
                filterList);

        JPanel filterPanel = new JPanel(new BorderLayout());
        JPanel filterPanelF = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JPanel filterPanelB = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        filterPanelF.add(getComponentFactory().createLabelFor("filter",
                filterField));
        filterPanelF.add(filterField);

        saveBtn.addActionListener(this);
        filterPanelB.add(saveBtn);

        clearBtn.addActionListener(this);
        filterPanelB.add(clearBtn);
        filterPanel.add(filterPanelF, BorderLayout.WEST);
        filterPanel.add(filterPanelB, BorderLayout.EAST);

        JTable table = getConsoleTableFactory().getConsoleTable(
                sortedList);
        consoleSelectionModel = new EventSelectionModel<WebScarabLogRecord>(
                sortedList);
        table.setSelectionModel(consoleSelectionModel);
        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {

                    public void valueChanged(ListSelectionEvent e) {
                        if (e.getValueIsAdjusting()) {
                            return;
                        }
                        EventList<WebScarabLogRecord> selected = consoleSelectionModel.getSelected();
                        WebScarabLogRecord[] selection = selected.toArray(new WebScarabLogRecord[selected.size()]);
                        ConsoleSelectionEvent cse = new ConsoleSelectionEvent(
                                ConsoleView.this, selection);
                        eventService.publish(cse);
                    }
                });
        ValueModel selectionHolder = new ListSelectionValueModelAdapter(table.getSelectionModel());
        JScrollPane tableScrollPane = getComponentFactory().createScrollPane(
                table);
        tableScrollPane.setMinimumSize(new Dimension(100, 60));

        JPanel mainPanel = getComponentFactory().createPanel(new BorderLayout());
        mainPanel.add(tableScrollPane, BorderLayout.CENTER);
        mainPanel.add(filterPanel, BorderLayout.SOUTH);

        //setting handler for all logs in app
        logHandler = new LogHandler(this.getEventService());
        Logger.getLogger("").addHandler(logHandler);
        return mainPanel;
    }

    public WebScarabLogRecord[] getSelectedWebScarabLogRecords() {
        EventList<WebScarabLogRecord> selected = consoleSelectionModel.getSelected();
        return selected.toArray(new WebScarabLogRecord[selected.size()]);
    }

    public EventList<WebScarabLogRecord> getRecords() {
        return records;
    }

    private EventService getEventService() {
        return eventService;
    }

    public void setEventService(EventService eventService) {
        this.eventService = eventService;
        eventService.subscribeStrongly(LogEvent.class, this);
        eventService.subscribeStrongly(SessionEvent.class, this);
    }

    public ConsoleTableFactory getConsoleTableFactory() {
        return consoleTableFactory;
    }

    public void setConsoleTableFactory(ConsoleTableFactory consoleTableFactory) {
        this.consoleTableFactory = consoleTableFactory;
    }

    public void onEvent(EventServiceEvent ese) {
        if (ese instanceof LogEvent) {
            LogEvent le = (LogEvent) ese;
            synchronized (records) {
                records.add(le.getLogRecord());
            }
        } else if (ese instanceof SessionEvent) {
            if (logHandler == null) {
            }
        }
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == saveBtn) {
            fc.addChoosableFileFilter(new LogFileFilter());
            int returnVal = fc.showSaveDialog(saveBtn);
            if (returnVal == JFileChooser.APPROVE_OPTION) {
                File file = fc.getSelectedFile();
                try {
                    BufferedWriter out = new BufferedWriter(new FileWriter(file.getAbsolutePath()));

                    synchronized (records) {
                        Iterator<WebScarabLogRecord> it = records.iterator();
                        while (it.hasNext()) {
                            out.write(it.next().toFullString());
                        }
                    }
                    out.close();
                } catch (IOException ex) {
                    log.log(Level.SEVERE, "Error with writing to log file", ex);
                }

            }
        } else if (e.getSource() == clearBtn) {
            synchronized (records) {
                records.clear();
            }
        }
    }

    private class ConsoleFilter implements TextFilterator<WebScarabLogRecord> {

        public void getFilterStrings(List<String> list,
                WebScarabLogRecord record) {
            list.add(record.getMessage());
            list.add(record.getSourceClassName());
            list.add(record.getSourceMethodName());
            list.add(record.getLoggerName());
        }
    }

    class LogFileFilter extends javax.swing.filechooser.FileFilter {

        public boolean accept(File f) {
            return f.isDirectory() || f.getName().toLowerCase().endsWith(".txt");
        }

        public String getDescription() {
            return getMessage("consoleView.saveWindow.fileDescription.label");
        }
    }
}
