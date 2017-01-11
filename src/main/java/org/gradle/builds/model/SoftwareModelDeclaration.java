package org.gradle.builds.model;

import java.util.LinkedHashSet;
import java.util.Set;

public class SoftwareModelDeclaration {
    private final Set<ProjectDependencyDeclaration> dependencies = new LinkedHashSet<>();
    private final String name;
    private final String type;

    public SoftwareModelDeclaration(String name, String type) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Set<ProjectDependencyDeclaration> getDependencies() {
        return dependencies;
    }

    public void dependsOn(String projectPath) {
        dependencies.add(new ProjectDependencyDeclaration(projectPath));
    }
}
