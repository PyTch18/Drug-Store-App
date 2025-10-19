pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        maven {
            url = uri("https://repo.heremaps.com/repository/here-sdk/")
            credentials {
                username = "MldG0F9XjjAl8uqYUzR0jA"
                password = "m6QBHBWJ5aJsTjxyLQA20lnZiuP94NmRJfVbZxLBYWhsb1GdOEkqpAtbNFAWLx2Xh9zGg4anHwXC0pdXpgIoDw"
            }
        }
    }
}

rootProject.name = "Drug Store"
include(":app")
