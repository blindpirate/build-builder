package org.gradle.builds.assemblers;

import org.gradle.builds.model.Build;
import org.gradle.builds.model.Model;
import org.gradle.builds.model.PublicationTarget;

public class SourceDependencyBuildAssembler implements ModelStructureAssembler {
    private final ProjectInitializer initializer;
    private final int sourceDependencies;

    public SourceDependencyBuildAssembler(ProjectInitializer initializer, int sourceDependencies) {
        this.initializer = new EmptyRootProjectInitializer(initializer);
        this.sourceDependencies = sourceDependencies;
    }

    @Override
    public void attachBuilds(Settings settings, Model model) {
        if (sourceDependencies > 0) {
            Build childBuild = new Build(model.getBuild().getRootDir().resolve("external/source"), "source dependency build", "src");
            childBuild.setSettings(new Settings(sourceDependencies + 1, 1));
            childBuild.setProjectInitializer(initializer);
            childBuild.setTypeNamePrefix("Src");
            childBuild.publishAs(new PublicationTarget(null));
            model.addBuild(childBuild);
            model.getBuild().sourceDependency(childBuild);
            model.getBuild().dependsOn(childBuild);
        }
    }
}