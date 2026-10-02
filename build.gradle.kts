plugins {
    base
    alias(libs.plugins.spring.boot) apply false
}

// Capturamos el catálogo acá: dentro de subprojects {} el accessor `libs`
// no siempre resuelve porque cambia el receiver implícito.
val catalog = libs

allprojects {
    group = "io.github.serg10arg.springailab"
    version = "0.1.0-SNAPSHOT"
    repositories { mavenCentral() }
}

subprojects {
    apply(plugin = "java")

    extensions.configure<JavaPluginExtension> {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    dependencies {
        add("implementation", platform(catalog.spring.boot.bom))
        add("implementation", platform(catalog.spring.ai.bom))
    }

    tasks.withType<Test>().configureEach {
        useJUnitPlatform()
        maxHeapSize = "1g"
    }
}
