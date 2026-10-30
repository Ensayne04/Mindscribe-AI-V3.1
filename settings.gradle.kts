pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

    dependencyResolutionManagement {
        repositoriesMode.set(RepositoriesMode.PREFER_PROJECT)
        repositories {
            google()
            mavenCentral()
        }
    }

    rootProject.name = "TaskWorkspace" // You can change this to your actual project folder name
    include(":app")