package org.gradle.builds.assemblers;

import org.gradle.builds.model.BuildProjectStructureBuilder;
import org.gradle.builds.model.Project;

public class InitialProjectSetupBuildConfigurer implements BuildConfigurer<BuildProjectStructureBuilder> {
    private final GraphAssembler graphAssembler;

    public InitialProjectSetupBuildConfigurer(GraphAssembler graphAssembler) {
        this.graphAssembler = graphAssembler;
    }

    @Override
    public void populate(BuildProjectStructureBuilder build) {
        StructureAssembler structureAssembler = new StructureAssembler(graphAssembler);
        structureAssembler.arrangeProjects(build, build.getProjectInitializer());
        structureAssembler.arrangeClasses(build);

        // Define publications
        for (Project project: build.getProjects()) {
            project.setVersion(build.getVersion());
        }
        if (build.getPublicationTarget() != null) {
            for (Project project: build.getProjects()) {
                project.publishAs(build.getPublicationTarget());
            }
        }

        // Add incoming dependencies
        for (BuildProjectStructureBuilder other: build.getDependsOn()) {
            if (other.getPublicationTarget().getHttpRepository() != null) {
                build.getRootProject().getBuildScript().allProjects().maven(other.getPublicationTarget().getHttpRepository());
            }
            for (Project project: build.getProjects()) {
                project.requires(other.getExportedLibraries());
            }
        }
    }
}
