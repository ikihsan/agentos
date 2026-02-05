pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "AgentLauncher"

include(":app")
include(":core")
include(":voice")
include(":llm")
include(":taskengine")
include(":uirenderer")
include(":automation")
include(":memory")
