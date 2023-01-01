import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
	dependencies {
		classpath("gradle.plugin.com.ewerk.gradle.plugins:querydsl-plugin:1.0.10")
		classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.21")
		classpath("org.jetbrains.kotlin:kotlin-allopen:1.6.21")
		classpath("org.jetbrains.kotlin:kotlin-noarg:1.6.21")
	}
}

plugins {
	id("org.springframework.boot") version "3.0.0"
	id("io.spring.dependency-management") version "1.1.0"
	kotlin("jvm") version "1.7.21"
	kotlin("plugin.spring") version "1.7.21"
}

group = "com.carrot"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_17

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-validation")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	implementation("commons-io:commons-io:2.11.0")
	// https://github.com/MicroUtils/kotlin-logging
	implementation("io.github.microutils:kotlin-logging-jvm:3.0.4")
	/** aws sdk */
	implementation("com.amazonaws:aws-java-sdk-s3:1.12.346")
	implementation("software.amazon.awssdk:s3:2.18.21")
	/** M1 mac */
	runtimeOnly("io.netty:netty-resolver-dns-native-macos:4.1.84.Final:osx-aarch_64")
	/** db */
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("mysql:mysql-connector-java:8.0.30")
	/** test */
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.boot:spring-boot-starter-webflux")
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "17"
	}
}

tasks.withType<Test> {
	useJUnitPlatform()
}
