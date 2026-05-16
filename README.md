# StyleBop for JetBrains IDEs

Right-click any `.css`, `.html`, or `.htm` file (or a folder) in IntelliJ IDEA, WebStorm, PyCharm, etc. and pick **Open in StyleBop** to launch [StyleBop](https://bendansby.com/apps/stylebop) on that target. For HTML files, StyleBop edits the inner `<style>` block (and inline `style="…"` attributes) as if they were a CSS file — the surrounding markup round-trips verbatim. The cursor's line number is forwarded so StyleBop's smart-router lands on the matching visual surface — Rulesets canvas, Animations tab, Tokens tab, or Fonts column — instead of dumping you in the Code tab.

## Requirements

- macOS
- [StyleBop](https://bendansby.com/apps/stylebop) installed
- A JetBrains IDE 2023.2 or newer

## Building locally

The repo intentionally doesn't ship the Gradle wrapper binary, since it's specific to your local Gradle install. One-time bootstrap:

```bash
# Install JDK 17 (the build targets IntelliJ Community 2024.1, which
# requires JDK 17). Newer JDKs work for running Gradle but the build
# wants 17 specifically — point JAVA_HOME at it before invoking the
# wrapper.
brew install --cask zulu@17
export JAVA_HOME="/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home"

# Install Gradle (only needed once, to generate the wrapper)
brew install gradle

# In this folder, generate the wrapper
gradle wrapper --gradle-version 8.10
```

After that, every subsequent build just uses the wrapper:

```bash
./gradlew buildPlugin
# Output: build/distributions/stylebop-intellij-0.1.0.zip
```

## Try it without publishing

1. `./gradlew buildPlugin` (first run pulls down the IntelliJ Platform SDK, ~1 GB, cached after)
2. In any JetBrains IDE: **Settings → Plugins → ⚙ → Install Plugin from Disk…**
3. Pick `build/distributions/stylebop-intellij-0.1.0.zip`
4. Restart the IDE when prompted

To smoke-test in a fresh sandbox IDE:

```bash
./gradlew runIde
```

This launches a clean IDE instance with just this plugin installed.

## Publishing to JetBrains Marketplace

1. Sign in / register at https://plugins.jetbrains.com (free)
2. Generate a "Hub Permanent Token" at https://hub.jetbrains.com → Profile → Authentication
3. Add `signPlugin` + `publishPlugin` config in `build.gradle.kts` with your token
4. `./gradlew publishPlugin`

First publish needs manual review by JetBrains (1-7 business days). Subsequent updates are instant.

## How it works

The plugin assembles a `stylebop://open?path=…&line=…` URL and shells out to `/usr/bin/open` so macOS LaunchServices routes it to StyleBop. There's no IPC, no daemon, no file-locking — StyleBop saves edits to disk, and the JetBrains IDE's built-in file watcher reloads them automatically.

## License

MIT
