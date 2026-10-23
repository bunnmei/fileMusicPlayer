package space.webkombinat.musicplayer.data.model

import android.net.Uri

/** 端末内の 1 曲。MediaStore の 1 行に対応する */
data class Song(
    val id: Long,
    val uri: Uri,
    /** 表示用タイトル（先頭のトラック番号 "01" などは取り除き済み） */
    val title: String,
    /** タグが無いときは null */
    val artist: String?,
    val album: String,
    val albumId: Long,
    val albumArtist: String?,
    val trackNumber: Int,
    val discNumber: Int,
    val durationMs: Long,
    val year: Int,
    /** 曲が入っているフォルダ（例: "Music/Records/01火気厳禁/"） */
    val folder: String,
    val fileName: String,
    /** ジャケット画像の候補。先頭から順に試す */
    val artworkUris: List<Uri>,
)

data class Album(
    val id: Long,
    val title: String,
    val artist: String?,
    val year: Int,
    val songs: List<Song>,
) {
    val artworkUris: List<Uri> get() = songs.firstOrNull()?.artworkUris.orEmpty()
    val durationMs: Long get() = songs.sumOf { it.durationMs }
}

data class Artist(
    /** タグが無い曲は null（「不明なアーティスト」）にまとめる */
    val name: String?,
    val albums: List<Album>,
    val songs: List<Song>,
) {
    val artworkUris: List<Uri> get() = albums.firstOrNull()?.artworkUris.orEmpty()
}

data class Library(
    val songs: List<Song> = emptyList(),
    val albums: List<Album> = emptyList(),
    val artists: List<Artist> = emptyList(),
) {
    private val songsById by lazy { songs.associateBy { it.id } }
    private val albumsById by lazy { albums.associateBy { it.id } }

    fun song(id: Long): Song? = songsById[id]
    fun album(id: Long): Album? = albumsById[id]
    fun artist(name: String?): Artist? = artists.firstOrNull { it.name == name }
}
