import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    java
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"
    id("io.spring.dependency-management") version "1.1.7"
    id("name.remal.sonarlint") version "6.0.0-rc-2"
    id("com.diffplug.spotless") version "6.25.0"
    id("com.github.johnrengelman.shadow") version "8.1.1"
}

java {
    sourceCompatibility = JavaVersion.VERSION_21
    targetCompatibility = JavaVersion.VERSION_21
}

group = "ru.petrelevich"

repositories {
    mavenLocal()
    mavenCentral()
}

val lombok = "1.18.36"
val spotbugs = "4.8.6"
val pdfboxTools = "3.0.3"

apply(plugin = "io.spring.dependency-management")
dependencyManagement {
    dependencies {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.5.5")
            mavenBom("com.fasterxml.jackson:jackson-bom:2.18.0")
        }
    }
}

dependencies {
    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")
    implementation("org.apache.pdfbox:pdfbox-tools:$pdfboxTools")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.github.spotbugs:spotbugs-annotations:$spotbugs")

    compileOnly("org.projectlombok:lombok:$lombok")

    annotationProcessor("org.projectlombok:lombok")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation ("org.assertj:assertj-core")
}

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()
        force("com.google.guava:guava:32.1.3-jre")
        force("commons-io:commons-io:2.18.0")
//        force("org.eclipse.jgit:org.eclipse.jgit:6.9.0.202403050737-r")
//        force("org.apache.commons:commons-compress:1.26.1")
//        force("org.jetbrains:annotations:19.0.0")
    }
}

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all,-serial,-processing", "-Werror"))

        dependsOn("spotlessApply")
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
}

sonarLint {

}

spotless {
    java {
        palantirJavaFormat()
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
    testLogging.showExceptions = true
    reports {
        junitXml.required.set(true)
        html.required.set(true)
    }
}

tasks {
    val managedVersions by registering {
        doLast {
            project.extensions.getByType<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>()
                .managedVersions
                .toSortedMap()
                .map { "${it.key}:${it.value}" }
                .forEach(::println)
        }
    }
}

tasks {
    named<ShadowJar>("shadowJar") {
        archiveBaseName.set("pdf-statement-parser")
        archiveVersion.set(project.version.toString())
        archiveClassifier.set("")
        manifest {
            attributes(mapOf("Main-Class" to "ru.petrelevich.PdfStatementParser"))
        }
    }

    build {
        dependsOn(shadowJar)
    }
}

