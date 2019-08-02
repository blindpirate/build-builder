package org.gradle.builds.generators;

import org.gradle.builds.model.BuildProjectStructureBuilder;
import org.gradle.builds.model.Project;

import java.io.IOException;

public abstract class ProjectFileGenerator implements Generator<BuildProjectStructureBuilder> {
    @Override
    public void generate(BuildProjectStructureBuilder build, FileGenerator fileGenerator) throws IOException {
        for (Project project : build.getProjects()) {
            generate(build, project, fileGenerator);
        }
    }

    protected abstract void generate(BuildProjectStructureBuilder build, Project project, FileGenerator fileGenerator) throws IOException;
}
