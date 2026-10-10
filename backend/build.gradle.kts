
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    id("org.flywaydb.flyway") version "10.11.0" 
}

group = "com.lucasx7.linksaver"
version = "1.0.0-SNAPSHOT"

application {
    mainClass = "io.ktor.server.netty.EngineMain"
}

kotlin {
    jvmToolchain(21)
}
dependencies {
    implementation(ktorLibs.server.config.yaml)
    implementation(ktorLibs.server.contentNegotiation)
    implementation(ktorLibs.server.core)
    implementation(ktorLibs.server.netty)
    implementation(libs.logback.classic)

    // Driver PostgreSQL
    implementation("org.postgresql:postgresql:42.7.2")

    // JetBrains Exposed
    implementation("org.jetbrains.exposed:exposed-core:0.50.1")
    implementation("org.jetbrains.exposed:exposed-dao:0.50.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.50.1")

    // Flyway (Migrações)
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")

    testImplementation(kotlin("test"))
    testImplementation(ktorLibs.server.testHost)

    //implementation("org.flywaydb:flyway-core")

    runtimeOnly("org.postgresql:postgresql:42.7.2") 
}
