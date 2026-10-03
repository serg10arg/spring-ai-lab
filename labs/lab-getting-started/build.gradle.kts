plugins {
    alias(libs.plugins.spring.boot)
}

// El plugin java, el toolchain 21 y los BOMs de Spring Boot y Spring AI los aplica
// el build raiz. Este archivo solo declara lo propio del modulo.
dependencies {
    // La unica linea que nombra al proveedor. Cambiar de proveedor es cambiar esta
    // dependencia y las properties de application.yml; el codigo Java no se toca.
    implementation(libs.spring.ai.starter.model.ollama)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
