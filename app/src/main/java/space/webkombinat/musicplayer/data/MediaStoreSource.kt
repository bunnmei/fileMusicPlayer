package space.webkombinat.musicplayer.data

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.core.net.toUri
import space.webkombinat.musicplayer.data.model.Song
import java.io.File

/**
 * MediaStore（Android が管理している端末内メディアの索引）から曲とジャケット画像を読み込む。
 * フォルダ構成には依存せず、ID3 などのタグ情報で整理する。
 */
class MediaStoreSource(private val resolver: ContentResolver) {

    /** 端末内の全曲を読み込む（並び替え・アルバムへのまとめは [LibraryBuilder] で行う） */
    fun loadSongs(): List<Song> {
        val raw = queryAudio()
        val folderImages = queryFolderImages(raw.map { it.folder }.toSet())
        return raw.map { it.toSong(folderImages[it.folder]) }
    }

    private class RawSong(
        val id: Long,
        val title: String?,
        val artist: String?,
        val album: String?,
        val albumId: Long,
        val albumArtist: String?,
        val track: Int?,
        val durationMs: Long,
        val year: Int,
        val folder: String,
        val fileName: String,
    ) {
        fun toSong(folderImage: Uri?): Song {
            val (disc, track) = TitleFormatter.parseTrack(track, fileName)
            val albumArt = ContentUris.withAppendedId(ALBUM_ART_URI, albumId)
            return Song(
                id = id,
                uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id),
                title = TitleFormatter.displayTitle(title, fileName, track),
                artist = artist,
                album = album ?: folder.trimEnd('/').substringAfterLast('/'),
                albumId = albumId,
                albumArtist = albumArtist,
                trackNumber = track,
                discNumber = disc,
                durationMs = durationMs,
                year = year,
                folder = folder,
                fileName = fileName,
                // 埋め込みジャケット → 同じフォルダの画像 の順に試す
                artworkUris = listOfNotNull(albumArt, folderImage),
            )
        }
    }

    private fun queryAudio(): List<RawSong> {
        val hasRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        val projection = buildList {
            add(MediaStore.Audio.Media._ID)
            add(MediaStore.Audio.Media.TITLE)
            add(MediaStore.Audio.Media.ARTIST)
            add(MediaStore.Audio.Media.ALBUM)
            add(MediaStore.Audio.Media.ALBUM_ID)
            add(MediaStore.Audio.Media.TRACK)
            add(MediaStore.Audio.Media.DURATION)
            add(MediaStore.Audio.Media.YEAR)
            add(MediaStore.Audio.Media.DISPLAY_NAME)
            if (hasRelativePath) {
                add(MediaStore.Audio.Media.RELATIVE_PATH)
                add(COLUMN_ALBUM_ARTIST)
            } else {
                @Suppress("DEPRECATION")
                add(MediaStore.Audio.Media.DATA)
            }
        }.toTypedArray()

        val selection = "${MediaStore.Audio.Media.IS_MUSIC} != 0 AND ${MediaStore.Audio.Media.DURATION} >= ?"
        val args = arrayOf(MIN_DURATION_MS.toString())

        val result = mutableListOf<RawSong>()
        resolver.query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, projection, selection, args, null)
            ?.use { c ->
                while (c.moveToNext()) {
                    val fileName = c.string(MediaStore.Audio.Media.DISPLAY_NAME) ?: continue
                    val folder = if (hasRelativePath) {
                        c.string(MediaStore.Audio.Media.RELATIVE_PATH).orEmpty()
                    } else {
                        @Suppress("DEPRECATION")
                        c.string(MediaStore.Audio.Media.DATA)?.let { File(it).parent?.plus("/") }.orEmpty()
                    }
                    result += RawSong(
                        id = c.long(MediaStore.Audio.Media._ID),
                        title = c.string(MediaStore.Audio.Media.TITLE),
                        artist = c.string(MediaStore.Audio.Media.ARTIST).tagOrNull(),
                        album = c.string(MediaStore.Audio.Media.ALBUM).tagOrNull(),
                        albumId = c.long(MediaStore.Audio.Media.ALBUM_ID),
                        albumArtist = if (hasRelativePath) c.string(COLUMN_ALBUM_ARTIST).tagOrNull() else null,
                        track = c.intOrNull(MediaStore.Audio.Media.TRACK),
                        durationMs = c.long(MediaStore.Audio.Media.DURATION),
                        year = c.intOrNull(MediaStore.Audio.Media.YEAR) ?: 0,
                        folder = folder,
                        fileName = fileName,
                    )
                }
            }
        return result
    }

    /** 曲と同じフォルダにある画像（cover.jpg など）をフォルダごとに 1 枚選ぶ */
    private fun queryFolderImages(folders: Set<String>): Map<String, Uri> {
        if (folders.isEmpty()) return emptyMap()
        val hasRelativePath = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
        @Suppress("DEPRECATION")
        val pathColumn = if (hasRelativePath) MediaStore.Images.Media.RELATIVE_PATH else MediaStore.Images.Media.DATA
        val projection = arrayOf(MediaStore.Images.Media._ID, MediaStore.Images.Media.DISPLAY_NAME, pathColumn)

        val candidates = mutableMapOf<String, MutableList<Pair<String, Uri>>>()
        // SQLite の引数上限を超えないよう分割して問い合わせる
        folders.chunked(400).forEach { chunk ->
            val selection: String
            val args: Array<String>
            if (hasRelativePath) {
                selection = "$pathColumn IN (${chunk.joinToString(",") { "?" }})"
                args = chunk.toTypedArray()
            } else {
                selection = chunk.joinToString(" OR ") { "$pathColumn LIKE ?" }
                args = chunk.map { "$it%" }.toTypedArray()
            }
            try {
                resolver.query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, selection, args, null)
                    ?.use { c ->
                        while (c.moveToNext()) {
                            val name = c.string(MediaStore.Images.Media.DISPLAY_NAME) ?: continue
                            val path = c.string(pathColumn) ?: continue
                            val folder = if (hasRelativePath) path else File(path).parent?.plus("/") ?: continue
                            if (folder !in folders) continue
                            val uri = ContentUris.withAppendedId(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                c.long(MediaStore.Images.Media._ID),
                            )
                            candidates.getOrPut(folder) { mutableListOf() } += name to uri
                        }
                    }
            } catch (e: SecurityException) {
                // 画像の読み取り権限が無い場合はフォルダ画像を使わない
                Log.w(TAG, "Folder images are not available", e)
                return emptyMap()
            }
        }
        return candidates.mapValues { (_, images) ->
            images.minWith(compareBy({ coverPriority(it.first) }, { it.first })).second
        }
    }

    private fun coverPriority(fileName: String): Int {
        val base = fileName.substringBeforeLast('.').lowercase()
        val index = PREFERRED_COVER_NAMES.indexOf(base)
        return if (index >= 0) index else PREFERRED_COVER_NAMES.size
    }

    private fun Cursor.string(column: String): String? =
        getColumnIndex(column).takeIf { it >= 0 }?.let { getString(it) }

    private fun Cursor.long(column: String): Long =
        getColumnIndex(column).takeIf { it >= 0 }?.let { getLong(it) } ?: 0L

    private fun Cursor.intOrNull(column: String): Int? =
        getColumnIndex(column).takeIf { it >= 0 && !isNull(it) }?.let { getInt(it) }

    private fun String?.tagOrNull(): String? =
        this?.takeIf { it.isNotBlank() && it != MediaStore.UNKNOWN_STRING }

    companion object {
        private const val TAG = "MediaStoreSource"

        /** 通知音などの短い音声を除外する */
        private const val MIN_DURATION_MS = 5_000L

        /** API 30 で公開された列だが、API 29 にも存在する */
        private const val COLUMN_ALBUM_ARTIST = "album_artist"

        private val ALBUM_ART_URI: Uri = "content://media/external/audio/albumart".toUri()

        private val PREFERRED_COVER_NAMES = listOf("cover", "folder", "front", "album", "albumart", "jacket")
    }
}
