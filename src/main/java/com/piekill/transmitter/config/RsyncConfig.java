package com.piekill.transmitter.config;

import com.intellij.credentialStore.CredentialAttributes;
import com.intellij.credentialStore.CredentialAttributesKt;
import com.intellij.credentialStore.Credentials;
import com.intellij.ide.passwordSafe.PasswordSafe;
import com.intellij.ide.util.PropertiesComponent;
import com.intellij.openapi.project.Project;

import java.util.*;

public class RsyncConfig {
    private final PropertiesComponent appProp;
    private final PropertiesComponent projectProp;
    private final String projectName;
    private static final String prefix = "com.piekill.transmitter.";
    private static final String connKey = prefix + "connections";
    private static final String[] appKeys = {".host", ".port", ".user", ".key.file"};
    private static final String[] projectKeys = {".exclude", ".remote.path", ".include.hidden"};

    public RsyncConfig(Project project) {
        this.appProp = PropertiesComponent.getInstance();
        this.projectProp = PropertiesComponent.getInstance(project);
        this.projectName = project.getName();
    }

    public void saveConfig(String connName, Boolean isDefault, String... args){
        saveConfig(0, connName, isDefault, args);
    }

    public void saveConfig(int idx, String connName, Boolean isDefault, String... args) {
        String[] configConn = appProp.getValues(connKey);
        if(configConn != null && configConn.length > 0) {
            List<String> configConnList = new ArrayList<>(Arrays.asList(configConn));
            if (!configConnList.contains(connName)) {
                configConnList.add(idx, connName);
                appProp.setValues(connKey, configConnList.toArray(new String[0]));
            }
        } else {
            appProp.setValues(connKey, new String[]{connName});
        }
        int i = 0;
        for(String appKey: appKeys){
            appProp.setValue(prefix + connName + appKey, args[i]);
            i++;
        }
        CredentialAttributes credentialAttributes = createCredentialAttributes(prefix + connName);
        Credentials credentials = new Credentials(args[i-2], args[i]);
        PasswordSafe.getInstance().set(credentialAttributes, credentials);
        i++;
        for(String projectKey: projectKeys){
            projectProp.setValue(prefix + connName + projectKey, args[i]);
            i++;
        }
        if(isDefault){
            projectProp.setValue(prefix + projectName + ".default", connName);
        }
    }

    public Map<String, String> getConfig(String connName) {
        Map<String, String> config = new HashMap<>();
        for(String appKey: appKeys){
            config.put(appKey, appProp.getValue(prefix + connName + appKey, ""));
        }
        CredentialAttributes credentialAttributes = createCredentialAttributes(prefix + connName);
        String password = PasswordSafe.getInstance().getPassword(credentialAttributes);
        config.put(".password", password);
        for(String projectKey: projectKeys){
            config.put(projectKey, projectProp.getValue(prefix + connName + projectKey, ""));
        }
        return config;
    }

    public String getDefaultConn() {
        return projectProp.getValue(prefix + projectName + ".default");
    }

    public Map<String, String> getDefaultConfig() {
        if(projectProp.isValueSet(prefix + projectName + ".default")) {
            return getConfig(projectProp.getValue(prefix + projectName + ".default"));
        } else {
            return null;
        }
    }

    public void removeConfig(String connName) {
        List<String> configConn = new ArrayList<>(Arrays.asList(appProp.getValues(connKey)));
        configConn.remove(connName);
        appProp.setValues(connKey, configConn.toArray(new String[0]));

        for(String appKey: appKeys){
            appProp.unsetValue(prefix + connName + appKey);
        }
        CredentialAttributes credentialAttributes = createCredentialAttributes(prefix + connName);
        PasswordSafe.getInstance().set(credentialAttributes, null);
        for(String projectKey: projectKeys){
            projectProp.unsetValue(prefix + connName + projectKey);
        }
        if(connName.equals(projectProp.getValue(prefix + projectName + ".default"))) {
            projectProp.unsetValue(prefix + projectName + ".default");
        }
    }

    public String[] getConnList() {
        return appProp.getValues(connKey);
    }

    public void setDefault(String connName) {
        projectProp.setValue(prefix + projectName + ".default", connName);
    }

    private CredentialAttributes createCredentialAttributes(String key) {
        return new CredentialAttributes(CredentialAttributesKt.generateServiceName("Transmitter", key));
    }
}
