package buildSrc.jdk

import com.android.build.api.dsl.CommonExtension
import com.android.build.gradle.BasePlugin
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinJvmCompile

plugins.withType(JavaPlugin::class.java) {
    project.extensions.getByType(JavaPluginExtension::class.java).apply {
        toolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }
}

tasks.withType(KotlinJvmCompile::class.java).configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_21)
    }
}

plugins.withType(BasePlugin::class.java) {
    project.extensions.getByType(CommonExtension::class.java).apply {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }
    }
}
