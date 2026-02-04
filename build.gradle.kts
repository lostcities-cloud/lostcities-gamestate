import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    jacoco

    id("org.springframework.boot") version "3.2.+"
    id("org.owasp.dependencycheck") version "11.0.0"
    id("com.github.rising3.semver") version "0.8.2"
    id("io.spring.dependency-management") version "1.1.4"
    id("org.jetbrains.dokka") version "2.1.0"
    id("com.google.cloud.tools.jib") version "3.4.4"
    //id("org.graalvm.buildtools.native") version "0.11.1"
    //id("org.openrewrite.rewrite") version "6.27.0"

	kotlin("jvm") version "2.3.+"
	kotlin("plugin.spring") version "2.3.+"
    kotlin("plugin.serialization") version "2.3.+"
}


group = "io.dereknelson.lostcities"
version = project.property("version")!!

/*rewrite {
    //activeRecipe("org.openrewrite.java.spring.boot3.UpgradeSpringBoot_3_3")

    //exportDatatables = true
}*/

repositories {

    maven {
        url = uri("https://maven.pkg.github.com/lostcities-cloud/lostcities-common")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }
    maven {
        url = uri("https://maven.pkg.github.com/lostcities-cloud/lostcities-models")
        credentials {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

	mavenCentral()
	maven { url = uri("https://repo.spring.io/milestone") }
	maven { url = uri("https://repo.spring.io/snapshot") }
}

val ktlint by configurations.creating


dependencyManagement {
    imports {
        mavenBom(org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES)
    }
}

configurations.matching { it.name.startsWith("dokka") }.configureEach {
    resolutionStrategy.eachDependency {
        if (requested.group.startsWith("com.fasterxml.jackson")) {
            useVersion("2.15.3")
        }
    }
}

val openTelemetryAgent: Configuration by configurations.creating
val otelAgentVersion = "2.24.0" // Use the desired agent version
dependencies {
    //rewrite("org.openrewrite:rewrite-kotlin:1.21.2")
    //rewrite("org.openrewrite.recipe:rewrite-spring:5.22.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-protobuf:1.10.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus")

    implementation("org.springframework.boot:spring-boot-devtools")

    implementation("org.apache.httpcomponents.client5:httpclient5:${rootProject.extra["httpclient5.version"]}")
    implementation("org.apache.httpcomponents.core5:httpcore5:${rootProject.extra["httpcore5.version"]}")

    openTelemetryAgent("io.opentelemetry.javaagent:opentelemetry-javaagent:${otelAgentVersion}") // Use the latest stable version
    implementation("io.micrometer:micrometer-tracing-bridge-otel")

    implementation(project(":lostcities-common"))
    implementation(project(":lostcities-models"))


	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("redis.clients:jedis:${rootProject.extra["jedis.version"]}")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:${rootProject.extra["springdoc.version"]}")
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:${rootProject.extra["springdoc.version"]}")

    ktlint("com.pinterest:ktlint:${rootProject.extra["ktlint.version"]}") {
        attributes {
            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
        }
    }

    dokkaHtmlPlugin("org.jetbrains.dokka:kotlin-as-java-plugin:2.0.0")

    testImplementation("org.assertj:assertj-core:3.22.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.6.2")
    testImplementation("org.junit.jupiter:junit-jupiter-engine:5.6.2")
	testImplementation("org.springframework.boot:spring-boot-starter-test:2.5.5")
	testImplementation("org.springframework.amqp:spring-rabbit-test:2.3.9")
}

tasks.named<BootRun>("bootRun") {
    if(rootProject.hasProperty("debug")) {
        systemProperty("spring.profiles.active", "local")


    }

    // Pass the -javaagent flag as a JVM argument
    jvmArgs("-javaagent:${openTelemetryAgent.singleFile.path}")

    // Optional: Configure common OpenTelemetry properties
    systemProperty("otel.service.name", project.name)
    systemProperty("otel.exporter.otlp.endpoint", "http://localhost:4318") // Default OTLP gRPC port

}


val outputDir = "${layout.buildDirectory}/reports/ktlint/"
val inputFiles = project.fileTree(mapOf("dir" to "src", "include" to "**/*.kt"))

val ktlintCheck by tasks.creating(JavaExec::class) {
	inputs.files(inputFiles)
	outputs.dir(outputDir)

	description = "Check Kotlin code style."
	classpath = ktlint
	mainClass.set("com.pinterest.ktlint.Main")
	args = listOf("src/**/*.kt")
}

val ktlintFormat by tasks.creating(JavaExec::class) {
	inputs.files(inputFiles)
	outputs.dir(outputDir)
	description = "Fix Kotlin code style deviations."
	classpath = ktlint
	mainClass.set("com.pinterest.ktlint.Main")
	args = listOf("-F", "src/**/*.kt")
	jvmArgs("--add-opens", "java.base/java.lang=ALL-UNNAMED")
}

semver {
    noGitPush = false
}

tasks.withType<KotlinCompile>() {

    compilerOptions {
        apiVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
        languageVersion.set(org.jetbrains.kotlin.gradle.dsl.KotlinVersion.KOTLIN_2_3)
        freeCompilerArgs.addAll(listOf(
            "-Xjsr305=strict", "-opt-in=kotlin.RequiresOptIn"
        ))
    }
}

tasks.bootBuildImage {
    docker.host = "unix:///run/user/1000/podman/podman.sock"
}

jib {
    from {
        image = "registry://public.ecr.aws/amazoncorretto/amazoncorretto:21.0.8-al2023-headless"
    }

    to {
        image = "ghcr.io/lostcities-cloud/${project.name}:${project.version}"
        tags = mutableSetOf("latest", "${project.version}")

        auth {
            username = System.getenv("GITHUB_ACTOR")
            password = System.getenv("GITHUB_TOKEN")
        }
    }

}

dependencyCheck {
    failBuildOnCVSS = 11f
    failOnError = false
    formats = mutableListOf("JUNIT", "HTML", "JSON")
    data {
        directory = "${rootDir}/owasp"
    }
    //suppressionFiles = ['shared-owasp-suppressions.xml']
    analyzers {
        assemblyEnabled = false
    }
    nvd {
        apiKey = System.getenv("NVD_KEY")
        delay = 16000
    }
}

tasks.withType<Test> {
	useJUnitPlatform()
}



tasks.test {
    finalizedBy(tasks.jacocoTestReport) // report is always generated after tests run
}
tasks.jacocoTestReport {
    dependsOn(tasks.test) // tests are required to run before generating the report
}
