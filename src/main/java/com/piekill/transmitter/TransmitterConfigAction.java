package com.piekill.transmitter;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.piekill.transmitter.ui.TransmitterConfig;

public class TransmitterConfigAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        TransmitterConfig configForm = new TransmitterConfig(e.getProject());
        configForm.showAndGet();
    }
}
