# Spotify Top Track Visualizer

An OPENRNDR application that visualizes your Spotify top tracks using OAuth 2.0 authentication.

## Prerequisites

1. JDK 17 or newer
2. A Spotify account (free or premium)
3. A Spotify Developer App (see setup instructions below)

## Spotify OAuth Setup

Before running the application, you need to set up Spotify OAuth authentication:

1. **Create a Spotify Developer App**: Follow the detailed instructions
   in [SPOTIFY_OAUTH_SETUP.md](SPOTIFY_OAUTH_SETUP.md)

2. **Configure credentials**: Copy `spotify.properties.example` to `spotify.properties` and add your Client ID and
   Client Secret:
   ```bash
   Copy-Item spotify.properties.example spotify.properties
   ```

3. **Run the authentication test**:
   ```bash
   ./gradlew run -Popenrndr.application=SpotifyAuthTestKt
   ```

The first time you run the application, it will open your browser for Spotify authorization. After authorizing, the
token will be saved locally for future use.

## Project Structure

- `src/main/kotlin/TemplateProgram.kt` - Main visualization program
- `src/main/kotlin/TemplateLiveProgram.kt` - Live-coding example with Spotify integration
- `src/main/kotlin/SpotifyAuthTest.kt` - Simple authentication test program
- `src/main/kotlin/client/` - Spotify API client and OAuth implementation
- `src/main/kotlin/service/` - Spotify service layer
- `src/main/kotlin/domain/` - Data models

If you are looking at this from IntelliJ IDEA you can start by expanding the _project_ tab on the left.

You will find some [basic instructions](https://guide.openrndr.org/setUpYourFirstProgram.html) in
the [OPENRNDR guide](https://guide.openrndr.org).

## Gradle tasks

- `./gradlew run` runs `TemplateProgram.kt` (Use `gradlew.bat run` under Windows)
- `./gradlew run -Popenrndr.application=MyProgramKt` runs `src/main/kotlin/myProgram.kt`
- `./gradlew run -Popenrndr.application=foo.bar.MyProgramKt` runs `src/main/kotlin/foo/bar/myProgram.kt` (assuming
  `package foo.bar` is used in myProgram.kt)
- `./gradlew shadowJar` creates an executable platform specific jar file with all dependencies. Run the resulting
  program by typing `java -jar build/libs/openrndr-template-1.0.0-all.jar` in a terminal from the project root. If your
  project contains multiple `main` methods, specify which one to run with
  `java -cp build/libs/openrndr-template-1.0.0-all.jar MyProgramKt`, where `MyProgramKt` can also be
  `foo.bar.MyProgramKt` if it's in the package `foo.bar`.
- `./gradlew jpackageZip` creates a zip with a stand-alone executable for the current platform (requires Java 17 or
  newer). Run it like this: `cd build/jpackage/openrndr-application/ && bin/openrndr-application`.
- `./gradlew dependencyUpydates` checks whether any dependencies have newer versions.

## Tips and issues

See the [wiki](https://github.com/openrndr/openrndr-template/wiki)

## Cross builds

To create a runnable jar for a platform different from your current platform, use
`./gradlew jar -PtargetPlatform=<platform>`, where `<platform>` is either `windows`, `macos`, `linux-x64`, or
`linux-arm64`.

## Updating OPENRNDR, ORX and other dependencies

The openrndr-template depends on various packages including the core [openrndr](https://github.com/openrndr/openrndr/)
and the [orx](https://github.com/openrndr/orx/) extensions and
provides the optional [orsl](https://github.com/openrndr/orsl/) shader helper modules.
The version numbers of these dependencies are specified in your [libs.versions.toml](gradle/libs.versions.toml) file.
Learn more about this file in
the [Gradle documentation](https://docs.gradle.org/current/userguide/platforms.html#sub:conventional-dependencies-toml)
website.

Newer versions bring useful features and bug fixes. The most recent versions are<br>
![Maven Central Version](https://img.shields.io/maven-central/v/org.openrndr/openrndr-math-jvm?label=OPENRNDR&color=%23FFC0CB)
![Maven Central Version](https://img.shields.io/maven-central/v/org.openrndr.extra/orx-noise-jvm?label=ORX&color=%23FFC0CB)
![Maven Central Version](https://img.shields.io/maven-central/v/org.openrndr.orsl/orsl-shader-generator-jvm?label=ORSL&color=%23FFC0CB).

Switch to the [next-version branch](https://github.com/openrndr/openrndr-template/tree/next-version) or enter these
versions manually in your toml file.
They can look like "0.4.3" or "0.4.3-alpha4". Use the complete string, as in:

    openrndr = "0.4.5-alpha5"
         orx = "0.4.5-alpha5"
        orsl = "0.4.5-alpha5"

You can add other dependencies needed by your project to your [build.gradle.kts](build.gradle.kts) file, inside the
`dependencies { }` block.

⚠️ Remember to reload the Gradle configuration after changing any dependencies.

## Github Actions

This repository contains various Github Actions under `./github/workflows`:

- [build-on-commit.yaml](.github/workflows/build-on-commit.yaml) runs a basic build on every commit,
  which can help detect issues in the source code.

- [publish-binaries.yaml](.github/workflows/publish-binaries.yaml) publishes binaries for Linux, Mac and Windows
  any time a commit is tagged with a version number like `v1.*`. For example, we can create and push a tag with these
  git commands:
    ```
    git tag -a v1.0.0 -m "v1.0.0"
    git push origin v1.0.0
    ```

  You can follow the progress of the action under the Actions tab in GitHub. Once complete, the executables will appear
  under the Releases section.
