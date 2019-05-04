package com.github.viccw.openwhiskgradleplugin;

import java.util.Optional;

import org.gradle.api.DefaultTask;
import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.InvalidUserDataException;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;
import org.projectodd.openwhisk.ActionOptions;
import org.projectodd.openwhisk.Configuration;
import org.projectodd.openwhisk.OWskClient;
import org.projectodd.openwhisk.model.ActionExec.KindEnum;

import com.google.common.base.Strings;

public class UpdateDockerActionTask extends DefaultTask {

    private static final String DEFAULT_DOCKER_IMAGE
        = "openwhisk/dockerskeleton:latest";

    public String artifactBuildTaskName = null;

    public String apiHostEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_API_HOST_ENVIRONMENT_VARIABLE;
    public String authorizationKeyEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_AUTHORIZATION_KEY_ENVIRONMENT_VARIABLE;
    public String namespaceEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_NAMESPACE_ENVIRONMENT_VARIABLE;
    public String actionNameEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_ACTION_NAME_ENVIRONMENT_VARIABLE;
    public String dockerImageEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_DOCKER_IMAGE_ENVIRONMENT_VARIABLE;
    public boolean web = false;
    public boolean webSecure = false;
    public String webSecureKeyEnvironmentVariable = OpenWhiskGradlePlugin
            .OPENWHISK_WEB_SECURE_KEY_ENVIRONMENT_VARIABLE;
    public short memoryLimit = 256;
    public boolean debug = false;

    Task artifactBuildTask = null;

    @TaskAction
    public void run() {
        String apiHost = getEnvironmentVariable(
                apiHostEnvironmentVariable,
                "OpenWhisk API host");
        String authorizationKey = getEnvironmentVariable(
                authorizationKeyEnvironmentVariable,
                "OpenWhisk authorization key");
        String namespace = getEnvironmentVariable(
                namespaceEnvironmentVariable,
                "OpenWhisk namespace");
        String actionName = getEnvironmentVariable(
                actionNameEnvironmentVariable,
                "OpenWhisk action name");
        String dockerImage = getOptionalEnvironmentVariable(
                dockerImageEnvironmentVariable,
                DEFAULT_DOCKER_IMAGE);

        Optional<Long> webSecureKey = Optional.empty();

        if (!web && webSecure) {
            throw new InvalidUserCodeException(
                    "WebSecure option selected whereas web option is not");
        }

        if (webSecure) {
            if (!Strings.isNullOrEmpty(webSecureKeyEnvironmentVariable)) {
                String environmentVariable = System.getenv(webSecureKeyEnvironmentVariable);
                if (!Strings.isNullOrEmpty(environmentVariable)) {
                    try {
                        webSecureKey = Optional.of(Long.parseLong(environmentVariable));
                    }
                    catch (NumberFormatException ex) {
                        throw new InvalidUserDataException(
                                "Web security secret key as read from "
                                + webSecureKeyEnvironmentVariable
                                + " must be an integer number");
                    }
                }
            }
            if (!webSecureKey.isPresent()) {
                System.out.println(
                        "WARNING: Action marked as web secure but without providing "
                        + "a secret key. Key will be generated randomly");
            }
        }

        if (artifactBuildTask == null) {
            throw new IllegalStateException("No artifact build task specified or found");
        }

        OWskClient openWhiskClient = new OWskClient();
        openWhiskClient.configure(Configuration.builder()
                .host(apiHost)
                .auth(authorizationKey)
                .namespace(namespace)
                .debugging(debug)
                .build());
        ActionOptions options = new ActionOptions(actionName)
                .kind(KindEnum.BLACKBOX)
                .image(dockerImage)
                .code(artifactBuildTask.getOutputs()
                        .getFiles().getSingleFile().getAbsolutePath())
                .web(web)
                .memory(memoryLimit)
                .overwrite(true);
        if (webSecure) {
            if (webSecureKey.isPresent()) {
                options.webSecure(webSecureKey.get());
            }
            else {
                options.webSecure(true);
                System.out.println("Generated secret key: " + options.webSecureKey());
            }
        }
        openWhiskClient.actions().update(options);
    }

    private String getEnvironmentVariable(String customizableEnvironmentVariableName,
            String humanReadableDescriptionOfVariable) {
        if (Strings.isNullOrEmpty(customizableEnvironmentVariableName)) {
            throw new InvalidUserCodeException(
                    "Missing name of environment variable where to read "
                    + humanReadableDescriptionOfVariable + " from");
        }
        String result = System.getenv(customizableEnvironmentVariableName);
        if (Strings.isNullOrEmpty(result)) {
            throw new InvalidUserDataException(
                    "Environment variable " + customizableEnvironmentVariableName
                    + " not set");
        }
        return result;
    }

    private String getOptionalEnvironmentVariable(String environmentVariableName,
            String defaultValue) {
        if (Strings.isNullOrEmpty(environmentVariableName)) {
            return defaultValue;
        }
        String variableValue = System.getenv(environmentVariableName);
        if (Strings.isNullOrEmpty(variableValue)) {
            return defaultValue;
        }
        return variableValue;
    }
}
