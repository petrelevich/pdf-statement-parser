plugins {
    java
    id("fr.brouillard.oss.gradle.jgitver") version "0.10.0-rc03"
    id("io.spring.dependency-management") version "1.1.4"
    id("name.remal.sonarlint") version "3.4.9"
    id("com.diffplug.spotless") version "6.25.0"
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

val lombok = "1.18.30"
val jsr305 = "3.0.2"
val pdfboxTools = "3.0.3"

apply(plugin = "io.spring.dependency-management")
dependencyManagement {
    dependencies {
        imports {
            mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.5")
            mavenBom("com.fasterxml.jackson:jackson-bom:2.18.0")
          }
    }
}

dependencies {

    implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-yaml")

    implementation("org.apache.pdfbox:pdfbox-tools:$pdfboxTools")
    implementation("ch.qos.logback:logback-classic")
    implementation("com.google.code.findbugs:jsr305:$jsr305")

    compileOnly("org.projectlombok:lombok:$lombok")

    annotationProcessor("org.projectlombok:lombok")

    testImplementation ("org.junit.jupiter:junit-jupiter-api")
    testImplementation ("org.junit.jupiter:junit-jupiter-engine")
    testImplementation ("org.assertj:assertj-core")
}

configurations.all {
    resolutionStrategy {
        failOnVersionConflict()

//        force("org.sonarsource.analyzer-commons:sonar-analyzer-commons:2.4.0.1317")
//        force("com.google.code.findbugs:jsr305:3.0.2")
//        force("org.sonarsource.sslr:sslr-core:1.24.0.633")
//        force("org.eclipse.platform:org.eclipse.osgi:3.18.300")
    }
}
apply(plugin = "name.remal.sonarlint")

tasks {
    compileJava {
        options.encoding = "UTF-8"
        options.compilerArgs.addAll(listOf("-Xlint:all,-serial,-processing", "-Werror"))
    }
    compileTestJava {
        options.encoding = "UTF-8"
    }
}

spotless {
    java {
        googleJavaFormat("1.17.0").aosp()
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


