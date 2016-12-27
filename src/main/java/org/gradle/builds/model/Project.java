package org.gradle.builds.model;

import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Set;

public class Project {
    public enum Role {
        Application, Library, Empty;
    }

    private final Project parent;
    private final String name;
    private final Path projectDir;
    private final BuildScript buildScript = new BuildScript();
    private final Set<Project> dependencies = new LinkedHashSet<>();
    private final Set<Component> components = new LinkedHashSet<>();
    private Role role = Role.Empty;

    public Project(Project parent, String name, Path projectDir) {
        this.parent = parent;
        this.name = name;
        this.projectDir = projectDir;
    }

    @Override
    public String toString() {
        return getPath();
    }

    public String getName() {
        return name;
    }

    public String getPath() {
        if (parent == null) {
            return ":";
        }
        String parentPath = parent.getPath();
        if (parentPath.equals(":")) {
            return ":" + name;
        }
        return parentPath + ":" + name;
    }

    public Path getProjectDir() {
        return projectDir;
    }

    public Project getParent() {
        return parent;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public BuildScript getBuildScript() {
        return buildScript;
    }

    public Set<Project> getDependencies() {
        return dependencies;
    }

    public void dependsOn(Project project) {
        this.dependencies.add(project);
    }

    public <T extends Component> T component(Class<T> type) {
        for (Component component : components) {
            if (type.isInstance(component)) {
                return type.cast(component);
            }
        }
        return null;
    }

    public <T extends Component> T addComponent(T component) {
        components.add(component);
        return component;
    }
}
