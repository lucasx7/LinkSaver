
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(ktorLibs.plugins.ktor)
    id("org.flywaydb.flyway") version "10.11.0"
    id("com.google.protobuf") version "0.10.1" 
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
    implementation(ktorLibs.serialization.jackson)
    implementation("org.jetbrains.exposed:exposed-java-time:0.50.1")

    // Flyway (Migrações)
    implementation("org.flywaydb:flyway-core:10.10.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.10.0")

    testImplementation(kotlin("test"))
    
    implementation("io.grpc:grpc-netty-shaded:1.85.1")
    implementation("io.grpc:grpc-protobuf:1.85.1")
    implementation("io.grpc:grpc-stub:1.85.1")
    implementation("com.google.protobuf:protobuf-java:4.36.2")
    compileOnly("org.apache.tomcat:annotations-api:6.0.53")
    testImplementation("io.grpc:grpc-inprocess:1.85.1")
    testImplementation(ktorLibs.server.testHost)

    //implementation("org.flywaydb:flyway-core")

    runtimeOnly("org.postgresql:postgresql:42.7.2") 
}

sourceSets { main { proto { srcDir("../protos") } } }

protobuf {
    protoc { artifact = "com.google.protobuf:protoc:4.36.2" }
    plugins { create("grpc") { artifact = "io.grpc:protoc-gen-grpc-java:1.85.1" } }
    generateProtoTasks { all().forEach { it.plugins { create("grpc") } } }
}
