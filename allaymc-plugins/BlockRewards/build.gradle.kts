plugins {
    id("java-library")
    id("org.allaymc.gradle.plugin") version "0.2.1"
}

// TODO: Update the group to yours (should be same to the package of the plugin main class)
group = "com.allaymc.blockrewards"
// TODO: Update the description to yours
description = "Reward players with items when breaking specific blocks"
version = "0.1.0"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

// See also https://github.com/AllayMC/AllayGradle
allay {
    // TODO: Update the api version to the latest
    // You can find the latest version here: https://central.sonatype.com/artifact/org.allaymc.allay/api
    api = "0.24.0"

    plugin {
        // TODO: Update the entrance when you change your plugin main class
        // Same to `org.allaymc.javaplugintemplate.JavaPluginTemplate`
        entrance = ".BlockRewards"
        // TODO: Use your handsome name here
        authors += "AllayMC Community"
        // TODO: Update the website to yours
        website = "https://github.com/AllayMC/BlockRewards"
    }
}

dependencies {
    compileOnly(group = "org.projectlombok", name = "lombok", version = "1.18.34")
    annotationProcessor(group = "org.projectlombok", name = "lombok", version = "1.18.34")
}
