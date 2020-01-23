package com.piekill.transmitter;

import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.piekill.transmitter.config.RsyncConfig;
import com.github.fracpete.rsync4j.RSync;
import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;

import java.util.Map;

public class TransmitterAction extends AnAction {
    private static final String groupId = "Transmitter";
    @Override
    public void actionPerformed(AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) {
            showMsg("No Project Selected", NotificationType.ERROR);
            return;
        }
        RsyncConfig rsyncConfig = new RsyncConfig(project);
        Map<String, String> config = rsyncConfig.getDefaultConfig();
        if (config == null) {
            showMsg("No Default Connection", NotificationType.ERROR);
            return;
        }
        String remotePath = config.get(".remote.path");
        if(!remotePath.endsWith("/")){
            remotePath = remotePath + "/";
        }
        RSync rsync = new RSync()
                .source(project.getBasePath() + "/")
                .destination(config.get(".user") + "@" + config.get(".host") + ":" + remotePath)
                .recursive(true);
        if(config.get(".key.file")!= null && !config.get(".key.file").equals("") ||
                config.get(".password")!= null && !config.get(".password").equals("")) {
            String keyFileOpt = config.get(".key.file").equals("") ? "" : " -i " + config.get(".key.file");
            String portOpt = config.get(".port").equals("") ? "" : " -p " + config.get(".port");
            if (config.get(".password").equals("")) {
                rsync.rsh("/usr/bin/ssh -o StrictHostKeyChecking=no" + keyFileOpt + portOpt);
            } else {
                rsync.rsh("/usr/bin/sshpass -p " + config.get(".password") + " ssh -o StrictHostKeyChecking=no" + keyFileOpt + portOpt);
            }
        }
        if (!config.get(".exclude").equals("")) {
            String[] excludePaths = config.get(".exclude").split(",");
            for(String excludePath: excludePaths){
                rsync.exclude(excludePath);
            }
        }
        // or if you prefer using commandline options:
        // rsync.setOptions(new String[]{"-r", "/one/place/", "/other/place/"});
        try {
            CollectingProcessOutput output = rsync.execute();
            if (output.getExitCode() > 0) {
                showMsg(output.getStdErr(), NotificationType.ERROR);
            } else {
                showMsg("Transmission Success", NotificationType.INFORMATION);
            }
        } catch (Exception ex) {
            showMsg(ex.getLocalizedMessage(), NotificationType.ERROR);
        }
    }

    private void showMsg(String msg, NotificationType error) {
        Notifications.Bus.notify(new Notification(groupId, groupId, msg, error));
    }
}
