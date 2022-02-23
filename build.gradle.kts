import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.4.9"
	id("io.spring.dependency-management") version "1.0.11.RELEASE"
	kotlin("jvm") version "1.5.31"
	kotlin("plugin.spring") version "1.5.31"
}

group = "io.dereknelson.lostcities"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_16


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

dependencies {
	implementation("io.dereknelson.lostcities-cloud:lostcities-common:1.0-SNAPSHOT")
	implementation("io.dereknelson.lostcities-cloud:lostcities-models:1.0-SNAPSHOT")

	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

	implementation("org.apache.commons:commons-pool2:2.11.1")
	implementation("redis.clients:jedis:4.1.0")

	implementation("org.springframework.boot:spring-boot-starter-undertow")
	implementation("org.springframework.boot:spring-boot-starter-amqp")
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-data-redis")

	implementation("org.springdoc:springdoc-openapi-webmvc-core:1.5.10")
	implementation("org.springdoc:springdoc-openapi-ui:1.5.10")
	implementation("org.springdoc:springdoc-openapi-kotlin:1.5.10")

	ktlint("com.pinterest:ktlint:0.44.0") {
		attributes {
			attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
		}
	}

	testImplementation("org.springframework.boot:spring-boot-starter-test:2.6.3")
	testImplementation("org.springframework.amqp:spring-rabbit-test:2.4.2")
}

val outputDir = "${project.buildDir}/reports/ktlint/"
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


tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "16"
	}
}

tasks.bootRun {
	if (project.hasProperty("debug_jvm")) {
		jvmArgs("-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005")
	}
}


tasks.withType<Test> {
	useJUnitPlatform()
}
