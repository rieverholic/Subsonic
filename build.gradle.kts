plugins {
    id("java")
}

group = "dev.riever"
version = "0.2.0"

repositories {
    mavenCentral()
    maven {
        name = "papermc"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    compileOnly("com.velocitypowered:velocity-api:4.3.0")
    annotationProcessor("com.velocitypowered:velocity-api:4.3.0")
    compileOnly("io.netty:netty-all:4.2.19.Final")
}

tasks.test {
    useJUnitPlatform()
}

val generateTemplates = tasks.register<Copy>("generateTemplates") {
    description = "Generate build constants"
    val props = mapOf("version" to project.version)
    inputs.properties(props)
    from(file("src/main/templates"))
    into(layout.buildDirectory.dir("generated/sources/templates"))
    expand(props)
}
sourceSets.main { java.srcDir(generateTemplates.map { it.outputs }) }
