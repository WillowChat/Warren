import org.gradle.jvm.tasks.Jar
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.JavaVersion
import org.gradle.api.Project
import org.gradle.api.artifacts.dsl.RepositoryHandler
import org.gradle.api.artifacts.repositories.MavenArtifactRepository
import org.gradle.api.publish.PublishingExtension
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.SourceSetContainer
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.api.tasks.testing.Test
import java.io.File

val warrenVersion by project

buildscript {
    repositories {
        gradleScriptKotlin()
        jcenter()
    }

    dependencies {
        classpath(kotlinModule("gradle-plugin"))
        classpath("com.github.jengelman.gradle.plugins:shadow:1.2.3")
    }
}

apply {
    plugin("kotlin")
    plugin("com.github.johnrengelman.shadow")
    plugin("maven")
    plugin("maven-publish")
}

compileJava {
    sourceCompatibility = JavaVersion.VERSION_1_7.toString()
    targetCompatibility = JavaVersion.VERSION_1_7.toString()
}

repositories {
    gradleScriptKotlin()
    mavenCentral()
    maven { setUrl("https://maven.hopper.bunnies.io") }
}

dependencies {
    compile(kotlinModule("stdlib"))
    compile("org.slf4j:slf4j-api:1.7.21")
    compile("engineer.carrot.warren.kale:Kale:1.1.0.96")
    compile("com.squareup.okio:okio:1.9.0")

    runtime("org.slf4j:slf4j-simple:1.7.21")

    testCompile("junit:junit:4.12")
    testCompile("org.mockito:mockito-core:2.0.42-beta")
    testCompile("com.nhaarman:mockito-kotlin:0.3.1")
}

test {
    testLogging.setEvents(listOf("passed", "skipped", "failed", "standardError"))
}


val buildNumberAddition = if(project.hasProperty("BUILD_NUMBER")) { ".${project.property("BUILD_NUMBER")}" } else { "" }

version = "$warrenVersion$buildNumberAddition"
group = "engineer.carrot.warren.warren"

shadowJar().mergeServiceFiles()
shadowJar().relocate("kotlin", "engineer.carrot.warren.warren.repack.kotlin")
shadowJar().exclude("META-INF/*.DSA")
shadowJar().exclude("META-INF/*.RSA")

val sourcesTask = task<Jar>("sourcesJar") {
    dependsOn("classes")

    from(sourceSets("main").allSource)
    classifier = "sources"
}

project.artifacts.add("archives", sourcesTask)
project.artifacts.add("archives", project.tasks.getByName("shadowJar") as ShadowJar)

if (project.hasProperty("DEPLOY_DIR")) {
    configure<PublishingExtension> {
        mavenDeploy(this.repositories) { setUrl("file://${project.property("DEPLOY_DIR")}") }

        publications {
            it.create<MavenPublication>("mavenJava") {
                from(components.getByName("java"))

                artifact(shadowJar())
                artifact(sourcesTask)
            }
        }
    }
}

fun Project.jar(setup: Jar.() -> Unit) = (project.tasks.getByName("jar") as Jar).setup()
fun Project.test(setup: Test.() -> Unit) = (project.tasks.getByName("test") as Test).setup()
fun Project.compileJava(setup: JavaCompile.() -> Unit) = (project.tasks.getByName("compileJava") as JavaCompile).setup()
fun shadowJar() = (project.tasks.findByName("shadowJar") as ShadowJar)
fun mavenDeploy(repositoryHandler: RepositoryHandler, configuration: MavenArtifactRepository.() -> Unit) =
        repositoryHandler.maven({ it.configuration() })
fun sourceSets(name: String) = (project.property("sourceSets") as SourceSetContainer).getByName(name)
