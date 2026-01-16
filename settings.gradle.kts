pluginManagement {
    repositories {
        mavenLocal()
        maven {
            name = "GitHubPackages"
            url = uri("https://maven.pkg.github.com/Cobblemon-Eclipse/eclipse-gradle-plugin")
            credentials {
                username = System.getenv("GITHUB_ACTOR") ?: System.getProperty("gpr.user") ?: ""
                password = System.getenv("GITHUB_TOKEN") ?: System.getProperty("gpr.key") ?: ""
            }
        }
        maven { name = "Fabric"; url = uri("https://maven.fabricmc.net/") }
        gradlePluginPortal()
        mavenCentral()
    }
}

rootProject.name = "cobblemon-flan"
