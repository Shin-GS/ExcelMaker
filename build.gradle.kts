plugins {
    id("java")
    id("maven-publish")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }

    // Optional but recommended: also publish sources and javadoc jars
    withSourcesJar()
    withJavadocJar()
}

group = "io.github.shin-gs"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
}
