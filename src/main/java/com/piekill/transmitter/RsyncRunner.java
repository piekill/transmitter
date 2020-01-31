package com.piekill.transmitter;

import com.github.fracpete.processoutput4j.output.CollectingProcessOutput;
import com.github.fracpete.rsync4j.RSync;
import com.intellij.notification.Notification;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.vfs.VirtualFile;
import com.piekill.transmitter.utils.MsgUtil;

import java.util.Map;

public class RsyncRunner implements Runnable {

    private final Map<String, String> config;
    private final String basePath;
    private VirtualFile file = null;

    public RsyncRunner(Map<String, String> config, String basePath) {
        this.config = config;
        this.basePath = basePath;
    }

    public RsyncRunner(Map<String, String> config, String basePath, VirtualFile file) {
        this(config, basePath);
        this.file = file;
    }

    @Override
    public void run() {
        Notification notification = MsgUtil.showMsg("Running rsync...", NotificationType.INFORMATION);

        String remotePath = config.get(".remote.path");
        String base, remote;
        if (file == null) {
            base = basePath + "/";
            remote = remotePath.endsWith("/") ? remotePath : remotePath + "/";
        } else {
            String filePath = file.getPath();
            base = file.isDirectory() ? filePath + "/" : filePath;
            remote = remotePath + filePath.replace(basePath, "");
        }

        RSync rsync = new RSync()
                .source(base)
                .destination(config.get(".user") + "@" + config.get(".host") + ":" + remote)
                .recursive(true)
                .exclude(".*");
        // --rsync-path trick: https://stackoverflow.com/a/14877351
        if (file != null) {
            rsync.rsyncPath("mkdir -p " + remotePath + file.getParent().getPath().replace(basePath, "") + " && rsync");
        }

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

        try {
            CollectingProcessOutput output = rsync.execute();
            notification.expire();
            if (output.getExitCode() > 0) {
                MsgUtil.showMsg(output.getStdErr(), NotificationType.ERROR);
            } else {
                MsgUtil.showMsg("Transmission Success", NotificationType.INFORMATION);
            }
        } catch (Exception ex) {
            MsgUtil.showMsg(ex.getLocalizedMessage(), NotificationType.ERROR);
        }
    }
}
