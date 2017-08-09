package org.gradle.builds.generators;

import org.gradle.builds.model.Build;
import org.gradle.builds.model.Component;
import org.gradle.builds.model.Project;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class ProjectComponentSpecificSingleFileGenerator<T extends Component> extends ProjectComponentSpecificGenerator<T> {
    private final String filePath;

    public ProjectComponentSpecificSingleFileGenerator(Class<T> type, String filePath) {
        super(type);
        this.filePath = filePath;
    }

    @Override
    protected void generate(Build build, Project project, T component) throws IOException {
        Path file = project.getProjectDir().resolve(filePath);
        Files.createDirectories(file.getParent());
        try (PrintWriter printWriter = new PrintWriter(Files.newBufferedWriter(file))) {
            generate(project, component, printWriter);
        }
    }

    protected abstract void generate(Project project, T component, PrintWriter printWriter);
}
