/*
 *  Copyright (c) 2020, 2021 Microsoft Corporation
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Apache License, Version 2.0 which is available at
 *  https://www.apache.org/licenses/LICENSE-2.0
 *
 *  SPDX-License-Identifier: Apache-2.0
 *
 *  Contributors:
 *       Microsoft Corporation - initial API and implementation
 *       Fraunhofer Institute for Software and Systems Engineering - added dependencies
 *       ZF Friedrichshafen AG - add dependency
 *
 */

plugins {
    `java-library`
    id("application")
    id("com.github.johnrengelman.shadow") version "7.1.2"
}

val groupId: String by project
val edcVersion: String by project

dependencies {
    implementation("$groupId:control-plane-core:$edcVersion")

    implementation("$groupId:api-observability:$edcVersion")

    implementation("$groupId:configuration-filesystem:$edcVersion")
    implementation("$groupId:iam-mock:$edcVersion")

    implementation("$groupId:auth-tokenbased:$edcVersion")
    implementation("$groupId:management-api:$edcVersion")

    implementation("$groupId:dsp-api-configuration:$edcVersion")
    implementation("$groupId:dsp-http-core:$edcVersion")
    implementation("$groupId:dsp-http-spi:$edcVersion")
    implementation("$groupId:dsp-transform:$edcVersion")
    implementation("$groupId:dsp-catalog-http-dispatcher:$edcVersion")
    implementation("$groupId:dsp-catalog-transform:$edcVersion")
    implementation("$groupId:dsp-catalog-api:$edcVersion")
    implementation("$groupId:dsp-negotiation-http-dispatcher:$edcVersion")
    implementation("$groupId:dsp-negotiation-transform:$edcVersion")
    implementation("$groupId:dsp-negotiation-api:$edcVersion")
    implementation("$groupId:dsp-transfer-process-http-dispatcher:$edcVersion")
    implementation("$groupId:dsp-transfer-process-transform:$edcVersion")
    implementation("$groupId:dsp-transfer-process-api:$edcVersion")

    implementation("$groupId:data-plane-selector-core:$edcVersion")


    implementation(project(":transfer:transfer-01-file-transfer:status-checker"))

}

application {
    mainClass.set("org.eclipse.edc.boot.system.runtime.BaseRuntime")
}

tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
    exclude("**/pom.properties", "**/pom.xm")
    mergeServiceFiles()
    archiveFileName.set("consumer.jar")
}
