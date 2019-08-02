package org.gradle.builds.generators;

import org.gradle.builds.assemblers.CppSettings;
import org.gradle.builds.assemblers.Settings;
import org.gradle.builds.model.ConfiguredBuild;

import java.io.IOException;
import java.nio.file.Path;

public class ReadmeGenerator implements Generator<ConfiguredBuild> {
    @Override
    public void generate(ConfiguredBuild build, FileGenerator fileGenerator) throws IOException {
        Path readMe = build.getRootDir().resolve("README.md");
        fileGenerator.generate(readMe, printWriter -> {
            printWriter.println("<!-- GENERATED FILE -->");
            Settings settings = build.getSettings();
            printWriter.println();
            printWriter.println("This build was generated by [build-builder](https://github.com/adammurdoch/build-builder)");

            printWriter.println();
            printWriter.println("- projects: " + settings.getProjectCount());
            printWriter.println("- source files per project: " + settings.getSourceFileCount());
            if (settings instanceof CppSettings) {
                CppSettings cppSettings = (CppSettings) settings;
                printWriter.println("- header files per project: " + cppSettings.getHeaders());
                printWriter.println("- macro includes: " + cppSettings.getMacroIncludes());
                printWriter.println("- boost: " + cppSettings.isBoost());
            }

            printWriter.println();
        });
    }
}
