plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    // IntelliJ Platform Gradle Plugin v2 — Gradle-9 compatible.
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.bendansby"
version = "0.1.0"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
        intellijDependencies()
    }
}

dependencies {
    intellijPlatform {
        // Build against IntelliJ Community 2024.1 — the last release
        // that targets JDK 17 source compatibility (2024.2+ requires
        // JDK 21). The plugin uses only platform APIs, so it loads in
        // every JetBrains IDE (WebStorm, PyCharm, etc.) on 2024.1+.
        intellijIdeaCommunity("2024.1")
        instrumentationTools()
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin {
    jvmToolchain(17)
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            // 241 = IntelliJ 2024.1. Older builds have stale Kotlin
            // metadata + missing ActionUpdateThread.
            sinceBuild = "241"
            // Compatible up through IntelliJ 2029.x (branch 299).
            // The plugin only uses extremely stable platform APIs
            // (AnAction, ActionUpdateThread, Messages, VirtualFile),
            // so this is a generous-but-realistic upper bound.
            // (Marketplace rejects an empty until-build attribute,
            // so we have to set *something*.)
            untilBuild = "299.*"
        }
    }
}
