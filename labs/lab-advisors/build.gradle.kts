plugins {
    alias(libs.plugins.spring.boot)
}

// El plugin java, el toolchain 21 y los BOMs de Spring Boot y Spring AI los aplica
// el build raiz. Los advisors y la memoria de chat llegan con el starter del proveedor.
dependencies {
    implementation(libs.spring.ai.starter.model.ollama)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
