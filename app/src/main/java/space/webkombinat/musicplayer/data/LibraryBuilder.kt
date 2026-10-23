package space.webkombinat.musicplayer.data

import space.webkombinat.musicplayer.data.model.Album
import space.webkombinat.musicplayer.data.model.Artist
import space.webkombinat.musicplayer.data.model.Library
import space.webkombinat.musicplayer.data.model.Song
import java.text.Collator

/** 曲の一覧をアルバム・アーティストごとにまとめ、表示用に並べる */
object LibraryBuilder {

    fun build(songs: List<Song>): Library {
        val collator = Collator.getInstance()
        val trackOrder = compareBy<Song>({ it.discNumber }, { it.trackNumber }).thenBy(collator) { it.fileName }

        val albums = songs.groupBy { it.albumId }.map { (id, albumSongs) ->
            val sorted = albumSongs.sortedWith(trackOrder)
            Album(
                id = id,
                title = sorted.first().album,
                artist = sorted.firstNotNullOfOrNull { it.albumArtist }
                    ?: sorted.mapNotNull { it.artist }.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key,
                year = sorted.maxOf { it.year },
                songs = sorted,
            )
        }.sortedWith(compareBy(collator) { it.title })

        val artists = songs.groupBy { it.albumArtist ?: it.artist }.map { (name, artistSongs) ->
            val albumIds = artistSongs.map { it.albumId }.toSet()
            Artist(
                name = name,
                albums = albums.filter { it.id in albumIds },
                songs = artistSongs.sortedWith(compareBy(collator) { it.title }),
            )
        }.sortedWith(compareBy<Artist> { it.name == null }.thenBy(collator) { it.name.orEmpty() })

        return Library(
            songs = songs.sortedWith(compareBy(collator) { it.title }),
            albums = albums,
            artists = artists,
        )
    }

    /** [folder] が除外フォルダ（またはその配下）に入っているか */
    fun isExcluded(folder: String, excludedFolders: Set<String>): Boolean =
        excludedFolders.any { folder.startsWith(it) }
}
