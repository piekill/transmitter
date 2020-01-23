package com.piekill.transmitter.ui;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.ui.ValidationInfo;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ConnectionNameDialog extends DialogWrapper {
    private JPanel contentPane;
    private JTextField connNameField;
    private TransmitterConfig parent;
    public ConnectionNameDialog(Project project, TransmitterConfig transmitterConfig) {
        super(project);
        init();
        parent = transmitterConfig;
        setTitle("Name the Connection");
        setModal(true);
    }

    protected void doOKAction() {
        parent.addToConnList(connNameField.getText());
        close(OK_EXIT_CODE);
    }

    protected ValidationInfo doValidate() {
        String connName = connNameField.getText().trim();
        if(connName.equals("")) {
            return new ValidationInfo("Empty Connection Name", connNameField);
        } else if (parent.getConnSet().contains(connName)){
            return new ValidationInfo("Connection Exists", connNameField);
        } else {
            return null;
        }
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
}
