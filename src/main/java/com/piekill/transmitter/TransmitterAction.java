package com.piekill.transmitter;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.piekill.transmitter.config.RsyncConfig;
import com.piekill.transmitter.utils.MsgUtil;

import java.util.Map;

public class TransmitterAction extends AnAction {

    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            MsgUtil.showMsg("No Project Selected", NotificationType.ERROR);
            return;
        }
        RsyncConfig rsyncConfig = new RsyncConfig(project);
        Map<String, String> config = rsyncConfig.getDefaultConfig();
        if (config == null) {
            MsgUtil.showMsg("No Default Connection", NotificationType.ERROR);
            return;
        }
        FileDocumentManager.getInstance().saveAllDocuments();
        RsyncRunner rsyncRunner = new RsyncRunner(config, project.getBasePath());
        Thread thread = new Thread(rsyncRunner);
        thread.start();
    }
}
