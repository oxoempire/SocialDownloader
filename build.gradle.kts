buildscript {
    repositories {
        google()
        mavenCentral()
        maven("https://chaquo.com/maven")
    }
    dependencies {
        classpath("com.chaquo.python:gradle:15.0.1")
    }
}
plugins {
    id("com.android.application") version "8.2.0" apply false
    id("org.jetbrains.kotlin.android") version "1.9.0" apply false
}
