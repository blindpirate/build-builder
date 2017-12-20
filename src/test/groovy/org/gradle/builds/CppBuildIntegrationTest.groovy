package org.gradle.builds

import spock.lang.Unroll

class CppBuildIntegrationTest extends AbstractIntegrationTest {
    def setup() {
        gradleVersion = "4.5-20171218235901+0000"
    }

    def "can generate single project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers").list() as Set == ["app.h", "app_defs1.h"] as Set
        def srcDir = build.project(":").file("src/main/cpp")
        srcDir.list() as Set == ["app.cpp", "app_private.h", "appimpl1api.cpp", "appimpl2api.cpp"] as Set
        new File(srcDir, "app.cpp").text.contains("AppImpl1Api")
        new File(srcDir, "appimpl1api.cpp").text.contains("AppImpl2Api")

        build.project(":").file("performance.scenarios").text.contains('apply-h-change-to = "src/main/headers/app.h"')
        build.project(":").file("performance.scenarios").text.contains('apply-cpp-change-to = "src/main/cpp/app.cpp"')

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        build.buildSucceeds("publish")
        file("repo/test/testApp/1.2/testApp-1.2.pom").file
    }

    @Unroll
    def "can generate single project build with #sourceFiles source files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--source-files", sourceFiles as String)

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/cpp").list().size() == sourceFiles + 1

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        sourceFiles << [1, 2, 5, 10, 20]
    }

    def "can generate 2 project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers").list() as Set == ["app.h", "app_defs1.h"] as Set
        def srcDir = build.project(":").file("src/main/cpp")
        srcDir.list() as Set == ["app.cpp", "app_private.h", "appimpl1api.cpp", "appimpl2api.cpp"] as Set
        new File(srcDir, "appimpl1api.cpp").text.contains("Lib1Api")

        build.project(":lib1api").isCppLibrary()
        build.project(":lib1api").file("src/main/public").list() as Set == ["lib1api.h"] as Set
        build.project(":lib1api").file("src/main/headers").list() as Set == ["lib1api_impl.h"] as Set
        build.project(":lib1api").file("src/main/cpp").list() as Set == ["lib1api.cpp", "lib1api_private.h", "lib1apiimpl1api.cpp", "lib1apiimpl2api.cpp"] as Set

        build.project(":").file("performance.scenarios").text.contains('apply-h-change-to = "lib1api/src/main/public/lib1api.h"')
        build.project(":").file("performance.scenarios").text.contains('apply-cpp-change-to = "lib1api/src/main/cpp/lib1api.cpp"')

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        build.buildSucceeds("publish")
        file("repo/test/testApp/1.2/testApp-1.2.pom").file
        file("repo/test/lib1api/1.2/lib1api-1.2.pom").file
    }

    def "can generate build with API dependencies between projects"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "6")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers/app.h").text.contains("#include \"lib1api1.h\"")
        build.project(":").file("src/main/headers/app.h").text.contains("void doSomethingWith(Lib1Api1& p);")
        build.project(":lib1api1").isCppLibrary()
        build.project(":lib1api1").file("src/main/public/lib1api1.h").text.contains("#include \"lib2api1.h\"")
        build.project(":lib1api1").file("src/main/public/lib1api1.h").text.contains("void doSomethingWith(Lib2Api1& p);")
        build.project(":lib1api2").isCppLibrary()
        build.project(":lib1api2").file("src/main/public/lib1api2.h").text.contains("#include \"lib2api1.h\"")
        build.project(":lib1api2").file("src/main/public/lib1api2.h").text.contains("void doSomethingWith(Lib2Api1& p);")

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
        build.buildSucceeds("publish")
    }

    def "can generate build with API dependencies between projects and source files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "6", "--source-files", "6")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers/app.h").text.contains("void doSomethingWith(AppImpl2Api1& p);")
        build.project(":lib1api1").isCppLibrary()
        build.project(":lib1api1").file("src/main/headers/lib1api1_impl.h").text.contains("void doSomethingWith(Lib1Api1Impl2Api1& p);")
        build.project(":lib1api2").isCppLibrary()
        build.project(":lib1api2").file("src/main/headers/lib1api2_impl.h").text.contains("void doSomethingWith(Lib1Api2Impl2Api1& p);")

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
        build.buildSucceeds("publish")
    }

    def "can generate multi-project build with simple macro includes"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--macro-include", "simple")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers/app.h").text.contains("#include APP_DEFS1_H")
        build.project(":").file("src/main/headers/app.h").text.contains('#define APP_DEFS1_H "app_defs1.h"')

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    def "can generate multi-project build with complex macro includes"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--macro-include", "complex")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers/app.h").text.contains("#include APP_DEFS1_H")
        build.project(":").file("src/main/headers/app.h").text.contains('#define APP_DEFS1_H __APP_DEFS1_H(app_defs1.h)')

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    def "can generate multi-project build with no macro includes"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--macro-include", "none")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        !build.project(":").file("src/main/headers/app.h").text.contains("#include APP_DEFS1_H")

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    def "can generate multi-project build with boost includes"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--boost")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/cpp/app_private.h").text.contains('#include <boost/asio.hpp>')
        build.project(":lib1api").file("src/main/cpp/lib1api_private.h").text.contains('#include <boost/asio.hpp>')

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    @Unroll
    def "can generate #projects project build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", projects)

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers").list() as Set == ["app.h", "app_defs1.h"] as Set
        build.project(":").file("src/main/cpp").list() as Set == ["app.cpp", "app_private.h", "appimpl1api.cpp", "appimpl2api.cpp"] as Set

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        build.buildSucceeds("publish")
        file("repo/test/testApp/1.2/testApp-1.2.pom").file

        where:
        projects << ["3", "4", "5", "10", "20"]
    }

    @Unroll
    def "can generate multi-project build with #sourceFiles source files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "4", "--source-files", sourceFiles as String)

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/cpp").list().size() == sourceFiles + 1
        build.project(":lib1api1").isCppLibrary()
        build.project(":lib1api1").file("src/main/cpp").list().size() == sourceFiles + 1
        build.project(":lib1api2").isCppLibrary()
        build.project(":lib2api").isCppLibrary()

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        sourceFiles << [1, 2, 5, 10, 20]
    }

    def "can generate multi-project build with 4 header files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--header-files", "4")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers").list() == ["app.h", "app_defs1.h"]
        build.project(":").file("src/main/cpp").list().findAll { it.endsWith('.h') } == ["app_private.h", "app_private_defs1.h"]

        build.project(":lib1api").isCppLibrary()
        build.project(":lib1api").file("src/main/public").list() == ["lib1api.h"]
        build.project(":lib1api").file("src/main/headers").list() == ["lib1api_impl.h", "lib1api_impl_defs1.h"]
        build.project(":lib1api").file("src/main/cpp").list().findAll { it.endsWith('.h') } == ["lib1api_private.h"]

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    def "can generate multi-project build with 8 header files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--header-files", "8")

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":").file("src/main/headers").list() == ["app.h", "app_defs1.h", "app_defs2.h", "app_defs3.h"]
        build.project(":").file("src/main/cpp").list().findAll { it.endsWith('.h') } == ["app_private.h", "app_private_defs1.h", "app_private_defs2.h", "app_private_defs3.h"]

        build.project(":lib1api").isCppLibrary()
        build.project(":lib1api").file("src/main/public").list() == ["lib1api.h", "lib1api_defs1.h"]
        build.project(":lib1api").file("src/main/headers").list() == ["lib1api_impl.h", "lib1api_impl_defs1.h", "lib1api_impl_defs2.h"]
        build.project(":lib1api").file("src/main/cpp").list().findAll { it.endsWith('.h') } == ["lib1api_private.h", "lib1api_private_defs1.h", "lib1api_private_defs2.h"]

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")
    }

    @Unroll
    def "can generate multi-project build with #headerFiles header files"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "4", "--header-files", headerFiles)

        then:
        build.isBuild()
        build.project(":").isCppApplication()
        build.project(":lib1api1").isCppLibrary()
        build.project(":lib1api2").isCppLibrary()
        build.project(":lib2api").isCppLibrary()

        build.buildSucceeds(":installDebug")
        build.app("build/install/main/debug/testApp").succeeds()

        build.buildSucceeds("build")

        where:
        headerFiles << ["6", "10", "20"]
    }

    def "can generate composite build"() {
        when:
        new Main().run("cpp", "--dir", projectDir.absolutePath, "--projects", "2", "--builds", "2")

        then:
        build.isBuild()

        build.project(":").isCppApplication()
        build.project(":lib1api").isCppLibrary()
        def srcDir = build.project(":").file("src/main/cpp")
        new File(srcDir, "appimpl1api.cpp").text.contains("Child1Lib1Api")

        def coreSrcDir = build.project(":lib1api").file("src/main/cpp")
        new File(coreSrcDir, "lib1apiimpl1api.cpp").text.contains("Child1Lib1Api")

        def child = build(file("child1"))
        child.isBuild()
        child.project(":").isEmptyProject()
        child.project(":child1lib1api").isCppLibrary()

        build.buildSucceeds(":installDebug")

        def app = build.app("build/install/main/debug/testApp")
        app.succeeds()

        build.buildSucceeds("build")
    }

    def "can generate single project build with http repo"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("cpp", "--http-repo", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        build.project(":").isCppApplication()

        def srcDir = build.project(":").file("src/main/cpp")
        new File(srcDir, "appimpl1api.cpp").text.contains("ExtLib1Api1")
        new File(srcDir, "appimpl1api.cpp").text.contains("ExtLib1Api2")
        new File(srcDir, "appimpl1api.cpp").text.contains("ExtLib2Api")

        def repoBuild = build(file('external'))
        repoBuild.isBuild()
        repoBuild.project(':').isEmptyProject()
        repoBuild.project(':extlib1api1').isCppLibrary()
        repoBuild.project(':extlib1api2').isCppLibrary()
        repoBuild.project(':extlib2api').isCppLibrary()

        def serverBuild = build(file('repo-server'))
        serverBuild.buildSucceeds("installDist")
        file("http-repo/org/gradle/example/extlib2api/1.2/extlib2api-1.2.pom").file
        file("http-repo/org/gradle/example/extlib2api/1.2/extlib2api-1.2-cpp-api-headers.zip").file

        def server = serverBuild.app("build/install/repo/bin/repo").start()
        waitFor(new URI("http://localhost:5005"))

        build.buildSucceeds(":installDebug")

        def app = build.app("build/install/main/debug/testApp")
        app.succeeds()

        build.buildSucceeds("build")

        cleanup:
        server?.kill()
    }

    def "can generate single project build with http repo with single library"() {
        given:
        useIsolatedUserHome()

        when:
        new Main().run("cpp", "--http-repo", "--http-repo-libraries", "1", "--dir", projectDir.absolutePath)

        then:
        build.isBuild()
        build.project(":").isCppApplication()

        def srcDir = build.project(":").file("src/main/cpp")
        new File(srcDir, "appimpl1api.cpp").text.contains("ExtLib1Api")

        def repoBuild = build(file('external'))
        repoBuild.isBuild()
        repoBuild.project(':').isEmptyProject()
        repoBuild.project(':extlib1api').isCppLibrary()

        def serverBuild = build(file('repo-server'))
        serverBuild.buildSucceeds("installDist")
        file("http-repo/org/gradle/example/extlib1api/1.2/extlib1api-1.2.pom").file
        file("http-repo/org/gradle/example/extlib1api/1.2/extlib1api-1.2-cpp-api-headers.zip").file

        def server = serverBuild.app("build/install/repo/bin/repo").start()
        waitFor(new URI("http://localhost:5005"))

        build.buildSucceeds(":installDebug")

        def app = build.app("build/install/main/debug/testApp")
        app.succeeds()

        build.buildSucceeds("build")

        cleanup:
        server?.kill()
    }
}
