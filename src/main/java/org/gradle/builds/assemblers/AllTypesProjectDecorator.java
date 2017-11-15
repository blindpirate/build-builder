package org.gradle.builds.assemblers;

import org.gradle.builds.model.Build;
import org.gradle.builds.model.MacroIncludes;

public class AllTypesProjectDecorator implements BuildConfigurer {
    private final JavaModelAssembler javaModelAssembler = new JavaModelAssembler();
    private final AndroidModelAssembler androidModelAssembler = new AndroidModelAssembler(AndroidModelAssembler.defaultVersion);
    private final CppModelAssembler cppModelAssembler = new CppModelAssembler(3, MacroIncludes.simple);
    private final ProjectDepOrderBuildConfigurer buildConfigurer = new ProjectDepOrderBuildConfigurer(new CompositeProjectConfigurer(javaModelAssembler, androidModelAssembler, cppModelAssembler));

    @Override
    public void populate(Build build) {
        buildConfigurer.populate(build);
    }
}
