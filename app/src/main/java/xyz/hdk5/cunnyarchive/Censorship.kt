package xyz.hdk5.cunnyarchive

import android.content.Context

val ALL_CENSORSHIP = AllCensorship()

abstract class Censorship {
    abstract fun fix(context: Context)
    abstract fun revert(context: Context)
}

open class MultiCensorship(
    private val censorships: Collection<Censorship>,
) : Censorship() {
    override fun fix(context: Context) {
        censorships.forEach { censorship ->
            censorship.fix(context)
        }
    }

    override fun revert(context: Context) {
        censorships.forEach { censorship ->
            censorship.revert(context)
        }
    }
}

open class FileCensorship(
    val censoredPath: String,
    val originalPath: String,
) : Censorship() {

    override fun fix(context: Context) {
        copy(
            context = context,
            src = makeMxTreeUri(originalPath),
            dst = makeMxTreeUri(censoredPath),
        )
    }

    override fun revert(context: Context) {
        delete(
            context = context,
            uri = makeMxTreeUri(censoredPath),
        )
    }
}

class AllCensorship : MultiCensorship(
    listOf(
        ArisCensorship(),
    )
)

class ArisCensorship : MultiCensorship(
    listOf(
        FileCensorship(
            censoredPath = "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_01_c.png",
            originalPath = "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_01.jpg",
        ),
        FileCensorship(
            censoredPath = "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_10_c.png",
            originalPath = "files/PUB/Resource/GameData/MediaResources/UIs/03_Scenario/01_Background/BG_CS_Millenium_10.jpg",
        ),
    )
)
