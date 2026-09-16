plugins {
    id("java")
}

group = "dev.riever"
version = "0.1.1"

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
    compileOnly("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")
    annotationProcessor("com.velocitypowered:velocity-api:4.1.2-SNAPSHOT")
    compileOnly("io.netty:netty-all:4.2.16.Final")
    compileOnly("org.spongepowered:configurate-yaml:4.2.0")
}

tasks.test {
    useJUnitPlatform()
}