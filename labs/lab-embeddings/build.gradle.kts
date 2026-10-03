plugins {
    alias(libs.plugins.spring.boot)
}

// El plugin java, el toolchain 21 y los BOMs de Spring Boot y Spring AI los aplica
// el build raiz.
dependencies {
    implementation(libs.spring.ai.starter.model.ollama)
    // SimpleVectorStore y SearchRequest no vienen con el starter del proveedor (D-018).
    implementation(libs.spring.ai.vector.store)

    testImplementation(libs.spring.boot.starter.test)
    testRuntimeOnly(libs.junit.platform.launcher)
}
