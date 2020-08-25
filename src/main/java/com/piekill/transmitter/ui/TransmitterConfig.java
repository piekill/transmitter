package com.piekill.transmitter.ui;

import com.intellij.openapi.fileChooser.FileChooser;
import com.intellij.openapi.fileChooser.FileChooserDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.TextFieldWithBrowseButton;
import com.intellij.ui.components.JBList;
import com.piekill.transmitter.ui.actions.EditListAction;
import com.piekill.transmitter.ui.actions.ListAction;
import com.piekill.transmitter.config.RsyncConfig;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class TransmitterConfig extends DialogWrapper {
    private JPanel contentPane;
    private JBList connList;
    private JButton addConn;
    private JButton removeConn;
    private JTextField hostField;
    private JTextField portField;
    private JTextField userField;
    private TextFieldWithBrowseButton keyFileField;
    private JTextField excludeField;
    private JTextField remotePathField;
    private JButton setDefault;
    private JPasswordField passwordField;
    private JCheckBox includeHiddenFilesCheckBox;

    private ListAction listAction;
    private DefaultListModel<String> connListModel;
    private RsyncConfig rsyncConfig;

    public TransmitterConfig(Project project) {
        super(project);
        init();
        addConn.addActionListener(e -> {
            ConnectionNameDialog connectionNameDialog = new ConnectionNameDialog(project, this);
            connectionNameDialog.showAndGet();
        });
        connListModel = new DefaultListModel<>();
        rsyncConfig = new RsyncConfig(project);
        String[] configConnList = rsyncConfig.getConnList();
        if (configConnList != null && configConnList.length > 0) {
            for (String conn : rsyncConfig.getConnList()) {
                connListModel.addElement(conn);
            }
        }
        connList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        connList.setModel(connListModel);
        connList.addListSelectionListener(e -> {
            int selected = connList.getSelectedIndex();
            if (e.getValueIsAdjusting()) {
                int previous = selected == e.getFirstIndex() ? e.getLastIndex() : e.getFirstIndex();
                if (previous != selected && previous >= 0) {
                    saveConfig(false, previous);
                }
            }
            int idx = Math.max(selected, 0);
            String connName = connListModel.size() > 0 ? connListModel.get(idx) : "";
            Map<String, String> config = rsyncConfig.getConfig(connName);
            updateConfigField(config);
            if (connListModel.size() > 0) {
                connList.setSelectedIndex(idx);
            }
        });
        connList.setCellRenderer(new ConnListCellRenderer());
        listAction = new ListAction(connList, new EditListAction(rsyncConfig, this));

        removeConn.addActionListener(e -> {
            int idx = connList.getSelectedIndex();
            if(idx >= 0){
                rsyncConfig.removeConfig(connListModel.getElementAt(idx));
                connListModel.removeElementAt(idx);
            }
        });
        setDefault.addActionListener(e -> {
            int idx = connList.getSelectedIndex();
            if(idx >= 0){
                saveConfig(true);
            }
        });
        if (connListModel.size() > 0){
            connList.setSelectedIndex(0);
        }

        ConfigFieldAdaptor configFieldAdaptor = new ConfigFieldAdaptor();
        hostField.addFocusListener(configFieldAdaptor);
        portField.addFocusListener(configFieldAdaptor);
        userField.addFocusListener(configFieldAdaptor);
        keyFileField.addFocusListener(configFieldAdaptor);
        excludeField.addFocusListener(configFieldAdaptor);
        remotePathField.addFocusListener(configFieldAdaptor);
        passwordField.addFocusListener(configFieldAdaptor);
        includeHiddenFilesCheckBox.addItemListener(e -> saveConfig(false));

        keyFileField.addBrowseFolderListener("Choose Key File", "Choose key file", project,
                new FileChooserDescriptor(true, true, false, false, false, true)
                        .withShowHiddenFiles(true));

        setTitle("Transmitter Configuration");
        setModal(true);

    }

    private void updateConfigField(Map<String, String> config) {
        hostField.setText(config.get(".host"));
        portField.setText(config.get(".port"));
        userField.setText(config.get(".user"));
        keyFileField.setText(config.get(".key.file"));
        passwordField.setText(config.get(".password"));
        excludeField.setText(config.get(".exclude"));
        remotePathField.setText(config.get(".remote.path"));
        includeHiddenFilesCheckBox.setSelected(Boolean.parseBoolean(config.get(".include.hidden")));
    }

    public void addToConnList(String name) {
        connListModel.insertElementAt(name, 0);
        connList.setSelectedIndex(0);
    }

    protected void doOKAction() {
        saveConfig(false);
        super.doOKAction();
    }

    public void doCancelAction() {
        saveConfig(false);
        super.doCancelAction();
    }

    /**
     * Factory method. It creates panel with dialog options. Options panel is located at the
     * center of the dialog's content pane. The implementation can return {@code null}
     * value. In this case there will be no options panel.
     */
    @Nullable
    @Override
    protected JComponent createCenterPanel() {
        return contentPane;
    }

    public Set<String> getConnSet() {
        String[] configConnList = rsyncConfig.getConnList();
        return configConnList != null ?
                new HashSet<>(Arrays.asList(configConnList)) : new HashSet<>();
    }

    public void saveConfig(Boolean isDefault) {
        saveConfig(isDefault, connList.getSelectedIndex());
    }
    public void saveConfig(Boolean isDefault, int idx) {
        if (idx >= 0) {
            rsyncConfig.saveConfig(idx, connListModel.getElementAt(idx), isDefault,
                    hostField.getText().trim(), portField.getText().trim(), userField.getText().trim(),
                    keyFileField.getText().trim(), String.valueOf(passwordField.getPassword()),
                    excludeField.getText().trim(), remotePathField.getText().trim(),
                    String.valueOf(includeHiddenFilesCheckBox.isSelected()));
            connList.repaint();
        }
    }

    class ConnListCellRenderer extends DefaultListCellRenderer {

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

            if (rsyncConfig.getDefaultConn() != null && rsyncConfig.getDefaultConn().equals(value)) {
                c.setFont(c.getFont().deriveFont(Font.BOLD | Font.ITALIC));
            } else {
                c.setFont(c.getFont().deriveFont(Font.PLAIN));
            }
            return c;
        }
    }

    class ConfigFieldAdaptor extends FocusAdapter {
        @Override
        public void focusLost(FocusEvent e) {
            saveConfig(false);
        }
    }
}
