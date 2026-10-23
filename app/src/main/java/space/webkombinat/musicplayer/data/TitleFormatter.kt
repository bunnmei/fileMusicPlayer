package space.webkombinat.musicplayer.data

/**
 * タグとファイル名の揺れを吸収するためのヘルパー。
 * "01素敵なハート.mp3" のようにタイトル先頭にトラック番号が付いたファイルでも、
 * きれいなタイトルと正しいトラック番号が得られるようにする。
 */
object TitleFormatter {
    // 1〜3 桁の数字（直後が数字でないこと）+ 区切り文字
    private val leadingNumber = Regex("""^(\d{1,3})(?!\d)[\s._\-)]*""")
    private val extension = Regex("""\.[^.]+$""")

    /**
     * MediaStore の TRACK 値をディスク番号とトラック番号に分ける。
     * TRACK は「ディスク番号 * 1000 + トラック番号」で入っていることがある。
     * タグが無い場合はファイル名先頭の数字をトラック番号とみなす。
     */
    fun parseTrack(rawTrack: Int?, fileName: String): Pair<Int, Int> {
        if (rawTrack != null && rawTrack > 0) {
            return (rawTrack / 1000) to (rawTrack % 1000)
        }
        val fromName = leadingNumber.find(fileName)?.groupValues?.get(1)?.toIntOrNull() ?: 0
        return 0 to fromName
    }

    /** タイトル先頭の番号がトラック番号と一致するときだけ取り除く */
    fun displayTitle(rawTitle: String?, fileName: String, trackNumber: Int): String {
        val title = rawTitle?.takeIf { it.isNotBlank() } ?: fileName.replace(extension, "")
        val match = leadingNumber.find(title)
        if (match != null) {
            val rest = title.substring(match.range.last + 1)
            if (rest.isNotBlank() && match.groupValues[1].toInt() == trackNumber) return rest
        }
        // "0412月のうた" のように、ゼロ埋めの番号の直後に数字で始まるタイトルが続く場合
        val padded = "0$trackNumber"
        if (trackNumber in 1..9 && title.length > padded.length && title.startsWith(padded)) {
            return title.substring(padded.length).trimStart(' ', '.', '_', '-', ')')
        }
        return title
    }
}
