import org.gradle.kotlin.dsl.`kotlin-dsl`

plugins {
    `kotlin-dsl`
}
repositories {
    mavenCentral()
}

subprojects {
    apply(plugin = "com.google.gms.google-services")
}