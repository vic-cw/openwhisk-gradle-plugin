# OpenWhisk Gradle plugin

Apache OpenWhisk is a serverless event-based programming service.

This plugin enables to manage OpenWhisk actions from a Gradle build script,
without requiring to install anything on your system.

## Quick start - minimal configuration

Add the following to `build.gradle`:

    buildscript {
        repositories {
            maven { url 'https://jitpack.io' }
        }
        dependencies {
            classpath 'com.github.vic-cw:openwhisk-gradle-plugin:29837b'
        }
    }
    
    apply plugin: 'com.github.viccw.openwhisk-gradle-plugin'
    
    task buildCloudFunctionZip (type: Zip) {
        // Logic to build artifact
        ...
    }
    
    updateDockerAction {
        artifactBuildTaskName = "buildCloudFunctionZip"
    }

Set the following environment variables:

- `OPENWHISK_API_HOST`: host of target OpenWhisk platform, for example `eu-gb.functions.cloud.ibm.com`
- `OPENWHISK_AUTHORIZATION_KEY`: key provided by OpenWhisk platform
- `OPENWHISK_NAMESPACE`: namespace of the action to update,
  for example `yourcloudfoundryorg_yourcloudfoundryspace`
- `OPENWHISK_ACTION_NAME`: action name
- `OPENWHISK_DOCKER_IMAGE`: full name of the Docker image to use as the base of OpenWhisk action,
  for example `com.domain/myOpenWhiskImage:latest`
- `OPENWHISK_WEB_SECURE_KEY`: a 16 digit integer to secure access to OpenWhisk action

Use it:

    // This updates the corresponding action on the target OpenWhisk platform
    // with the output of the buildCloudFunctionZip task
    ./gradlew updateDockerAction

For more details see [Full configuration options](./docs/full_configuration_options.md).

## State of this plugin

This plugin is still in its infancy and only supports a few features.

However, feel free to request a missing feature if you need it.
