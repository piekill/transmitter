package com.piekill.transmitter;

import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.piekill.transmitter.config.RsyncConfig;
import com.piekill.transmitter.utils.MsgUtil;

import java.util.Map;

public class TransmitterCurrentAction extends AnAction {

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
        VirtualFile file = e.getData(PlatformDataKeys.VIRTUAL_FILE);
        if (file != null && file.isValid()) {
            FileDocumentManager.getInstance().saveAllDocuments();
            RsyncRunner rsyncRunner = new RsyncRunner(config, project.getBasePath(), file);
            Thread thread = new Thread(rsyncRunner);
            thread.start();
        } else {
            MsgUtil.showMsg("Invalid File/Directory", NotificationType.ERROR);
        }
    }
}
