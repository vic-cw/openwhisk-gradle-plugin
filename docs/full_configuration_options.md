## Build script

    buildscript {
        repositories {
            maven { url 'https://jitpack.io' }
        }
        dependencies {
            // Replace '29837b' by any later commit hash,
            // see jitpack.io for explanations
            classpath 'com.github.vic-cw:openwhisk-gradle-plugin:29837b'
        }
    }
    
    apply plugin: 'com.github.viccw.openwhisk-gradle-plugin'
    
    task buildCloudFunctionZip (type: Zip) {
        // Logic to build artifact
        ...
    }
    
    updateDockerAction {
        // Required
        // The task specified here is automatically marked as a
        // dependency of updateDockerAction task.
        // Its output is sent to OpenWhisk platform.
        artifactBuildTaskName = "buildCloudFunctionZip"
        
        // Toggle on or off ability of OpenWhisk action to respond
        // to HTTP requests.
        // Optional, default false.
        // Only "true" and "false" supported for the moment.
        web = false
        
        // Toggle on or off requirement for an HTTP request to
        // OpenWhisk action to bear an X-Require-Whisk-Auth header.
        // Optional, default false.
        // Requires `web` to be set to true.
        webSecure = false
        
        // Customize from which environment variable to read web
        // security key for the X-Require-Whisk-Auth header.
        // Optional, default "OPENWHISK_WEB_SECURE_KEY".
        // If empty, or if corresponding variable is not set, but
        // `webSecure` is set to true, a random key is generated.
        webSecureKeyEnvironmentVariable = "OPENWHISK_WEB_SECURE_KEY"
        
        // Set memory limit for the OpenWhisk action, in MB.
        // Optional, default 256.
        memoryLimit = 256
        
        // Turn on or off logging of request and response to and from
        // OpenWhisk platform.
        // Optional, default false.
        debug = false
        
        // Customize from which environment variable to read API host.
        // Optional, default "OPENWHISK_API_HOST".
        apiHostEnvironmentVariable = "OPENWHISK_API_HOST"
        
        // Customize from which environment variable to read
        // authorization key for requests to OpenWhisk platform.
        // Optional, default "OPENWHISK_AUTHORIZATION_KEY".
        authorizationKeyEnvironmentVariable = "OPENWHISK_AUTHORIZATION_KEY"
        
        // Customize from which environment variable to read OpenWhisk
        // action namespace.
        // Optional, default "OPENWHISK_NAMESPACE".
        namespaceEnvironmentVariable = "OPENWHISK_NAMESPACE"
        
        // Customize from which environment variable to read action name.
        // Optional, default "OPENWHISK_ACTION_NAME".
        actionNameEnvironmentVariable = "OPENWHISK_ACTION_NAME"
        
        // Customize from which environment variable to read name of
        // Docker image to use as base image for this action.
        // Optional, default "OPENWHISK_DOCKER_IMAGE".
        dockerImageEnvironmentVariable = "OPENWHISK_DOCKER_IMAGE"
    }

## Environment variables

The following values are read from the following environment variables, or
from the environment variables specified as customizations of
`updateDockerAction` task:

Required:

- `OPENWHISK_API_HOST`: OpenWhisk platform API host,
  for example `eu-gb.functions.cloud.ibm.com`.
- `OPENWHISK_AUTHORIZATION_KEY`: Authorization key provided by OpenWhisk platform API, that
  is also used with `wsk` CLI.
- `OPENWHISK_NAMESPACE`: Namespace of the action to update,
  for example `yourcloudfoundryorg_yourcloudfoundryspace`.
- `OPENWHISK_ACTION_NAME`: Name of OpenWhisk action to update.
- `OPENWHISK_DOCKER_IMAGE`: Full name of the Docker image to use as the base of OpenWhisk
  action, for example `com.domain/myOpenWhiskImage:latest`.

Optional:

- `OPENWHISK_WEB_SECURE_KEY`: a 16 digit integer to secure access to OpenWhisk action. If the
  value of this environment variable is empty, and `updateDockerAction` is set with `webSecure`,
  a random key is generated instead.
