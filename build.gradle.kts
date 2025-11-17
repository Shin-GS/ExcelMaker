import com.vanniktech.maven.publish.SonatypeHost

plugins {
    id("java")
    id("com.vanniktech.maven.publish") version "0.30.0"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

group = "io.github.shin-gs"
version = "0.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.apache.poi:poi-ooxml:5.4.1")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

mavenPublishing {
    coordinates(
        groupId = "io.github.shin-gs",
        artifactId = "excelmaker",
        version = "0.0.1"
    )

    pom {
        name.set("ExcelMaker")
        description.set("A lightweight Java library for generating Excel and CSV files.")
        inceptionYear.set("2025")
        url.set("https://github.com/Shin-GS/ExcelMaker")

        licenses {
            license {
                name.set("The Apache License, Version 2.0")
                url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
            }
        }

        developers {
            developer {
                id.set("shin-gs")
                name.set("Shin GS")
                email.set("rudtjq1213@gmail.com")
            }
        }

        scm {
            url.set("https://github.com/Shin-GS/ExcelMaker")
            connection.set("scm:git:https://github.com/Shin-GS/ExcelMaker.git")
            developerConnection.set("scm:git:git@github.com:Shin-GS/ExcelMaker.git")
        }
    }

    publishToMavenCentral(SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
}
