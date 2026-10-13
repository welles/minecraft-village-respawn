plugins {
    id("dev.kikugie.stonecutter")
}

stonecutter active "1.21.1-fabric"

stonecutter parameters {
    val (version, loader) = current.project.split('-', limit = 2)

    properties {
        tags(version, loader)
    }

    constants {
        match(loader, "fabric", "neoforge")
    }

    replacements {
        // DimensionTransition was renamed in 1.21.2
        string(current.parsed >= "1.21.2") {
            replace("DimensionTransition", "TeleportTransition")
        }
    }
}
