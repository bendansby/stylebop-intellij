plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.0.21"
    // IntelliJ Platform Gradle Plugin v2 — Gradle-9 compatible.
    id("org.jetbrains.intellij.platform") version "2.1.0"
}

group = "com.bendansby"
version = "0.1.2"

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

// `<idea-version since-build="…"/>` is declared directly in
// plugin.xml so the until-build attribute is fully omitted (per
// JetBrains' "compatible with all future IDE versions" guidance).
// We deliberately don't configure pluginConfiguration.ideaVersion
// here — the v2 plugin's patchPluginXml task would always emit a
// default until-build value, which trips the Marketplace verifier.
