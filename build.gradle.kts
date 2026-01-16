plugins {
    id("com.eclipse.mod") version "1.0.9"
}

eclipseMod {
    modId.set("cobblemon-flan")
    modName.set("Cobblemon Flan Integration")
    modVersion.set("1.1.3")
    mavenGroup.set("com.eclipse")

    useEclipseCore.set(true)
    useKoin.set(true)
    usePermissionsApi.set(true)
    useCobblemon.set(true)
}

repositories {
    maven {
        name = "CurseMaven"
        url = uri("https://cursemaven.com")
        content {
            includeGroup("curse.maven")
        }
    }
}

dependencies {
    // Flan from CurseForge - compile only since players need to have Flan installed
    // CurseForge project ID for Flan: 404578
    // File ID for Flan 1.21.1-1.12.1: 6989476
    "modCompileOnly"("curse.maven:flan-404578:6989476") {
        isTransitive = false
    }
}
