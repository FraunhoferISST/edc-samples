/*
 *  Copyright (c) 2024 Fraunhofer Institute for Software and Systems Engineering
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Fraunhofer Institute for Software and Systems Engineering - initial API and implementation
 *
 */

plugins {
    `java-library`
    id("application")
    alias(libs.plugins.shadow)
}

dependencies {
    runtimeOnly(project(":fc:fc-00-basic:federated-catalog-base"))

    implementation(libs.edc.connector.core)
    implementation(libs.edc.control.plane.core)
    implementation(libs.edc.configuration.filesystem)
    implementation(libs.edc.management.api)
    implementation(libs.edc.dsp)
    implementation(libs.edc.iam.mock)
    implementation(libs.edc.http)


    // Federated catalog
//    runtimeOnly(libs.fc.core)
//    runtimeOnly(libs.fc.ext.api)
}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    mergeServiceFiles()
    archiveFileName.set("connector3.jar")
}