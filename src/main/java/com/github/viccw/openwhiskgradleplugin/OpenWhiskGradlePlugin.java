package com.github.viccw.openwhiskgradleplugin;

import java.util.Set;

import org.gradle.api.InvalidUserCodeException;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import com.google.common.base.Strings;

public class OpenWhiskGradlePlugin implements Plugin<Project> {

    private static final String EXTENSION_NAME = "openWhisk";
    private static final String UPDATE_DOCKER_ACTION_TASK_NAME = "updateDockerAction";

    static final String OPENWHISK_API_HOST_ENVIRONMENT_VARIABLE
        = "OPENWHISK_API_HOST";
    static final String OPENWHISK_AUTHORIZATION_KEY_ENVIRONMENT_VARIABLE
        = "OPENWHISK_AUTHORIZATION_KEY";
    static final String OPENWHISK_NAMESPACE_ENVIRONMENT_VARIABLE
        = "OPENWHISK_NAMESPACE";
    static final String OPENWHISK_ACTION_NAME_ENVIRONMENT_VARIABLE
        = "OPENWHISK_ACTION_NAME";
    static final String OPENWHISK_DOCKER_IMAGE_ENVIRONMENT_VARIABLE
        = "OPENWHISK_DOCKER_IMAGE";
    static final String OPENWHISK_WEB_SECURE_KEY_ENVIRONMENT_VARIABLE
        = "OPENWHISK_WEB_SECURE_KEY";

    @Override
    public void apply(Project project) {
        OpenWhiskExtension extension = new OpenWhiskExtension();
        project.getExtensions().add(EXTENSION_NAME, extension);

        UpdateDockerActionTask updateDockerActionTask = project.getTasks().create(
                UPDATE_DOCKER_ACTION_TASK_NAME,
                UpdateDockerActionTask.class,
                (task) -> {
                    task.setDescription("Update Docker action on OpenWhisk platform");
                    task.setGroup("Publishing");
                });

        project.afterEvaluate((evaluatedProject) -> {
            linkUpdateDockerActionTaskToItsDependency(project, updateDockerActionTask);
            if (!updateDockerActionTask.debug) {
                updateDockerActionTask.debug = extension.debug;
            }
        });
    }

    private void linkUpdateDockerActionTaskToItsDependency(Project project,
            UpdateDockerActionTask updateDockerActionTask) {
        OpenWhiskExtension extension = project.getExtensions()
                .getByType(OpenWhiskExtension.class);
        String artifactBuildTaskName =
                Strings.isNullOrEmpty(updateDockerActionTask.artifactBuildTaskName)
                ? extension.artifactBuildTaskName
                : updateDockerActionTask.artifactBuildTaskName;
        if (Strings.isNullOrEmpty(artifactBuildTaskName)) {
            throw new InvalidUserCodeException(
                    "Missing name of task responsible for building action artifact "
                    + "for task " + updateDockerActionTask.getName());
        }
        Set<Task> matchingTasks = project.getTasksByName(
                artifactBuildTaskName, true);
        if (matchingTasks.isEmpty()) {
            throw new InvalidUserCodeException(
                    "Could not find any task named " + artifactBuildTaskName);
        }
        updateDockerActionTask.artifactBuildTask = matchingTasks.iterator().next();
        updateDockerActionTask.dependsOn(updateDockerActionTask.artifactBuildTask);
    }
}
