package org.gradle.builds.model;

public class SwiftClass extends SourceClass<SwiftClass> {
    public SwiftClass(String name) {
        super(name);
    }

    @Override
    public SwiftClass getApi() {
        return this;
    }
}
