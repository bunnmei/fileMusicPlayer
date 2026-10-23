package space.webkombinat.musicplayer.data

import org.junit.Assert.assertEquals
import org.junit.Test

class TitleFormatterTest {

    @Test
    fun `track tag is split into disc and track`() {
        assertEquals(0 to 4, TitleFormatter.parseTrack(4, "04深いブルー.mp3"))
        assertEquals(2 to 3, TitleFormatter.parseTrack(2003, "song.mp3"))
    }

    @Test
    fun `missing track tag falls back to file name`() {
        assertEquals(0 to 1, TitleFormatter.parseTrack(null, "01SWINGIN'TRAIN.wav"))
        assertEquals(0 to 12, TitleFormatter.parseTrack(0, "12 - Song.flac"))
        assertEquals(0 to 0, TitleFormatter.parseTrack(null, "Song.mp3"))
    }

    @Test
    fun `leading number matching the track is removed`() {
        assertEquals("素敵なハートでまたどうぞ", TitleFormatter.displayTitle("01素敵なハートでまたどうぞ", "01素敵なハートでまたどうぞ.mp3", 1))
        assertEquals("Song", TitleFormatter.displayTitle("03 - Song", "x.mp3", 3))
        assertEquals("Song", TitleFormatter.displayTitle("07. Song", "x.mp3", 7))
        assertEquals("12月のうた", TitleFormatter.displayTitle("0412月のうた", "0412月のうた.mp3", 4))
    }

    @Test
    fun `numbers that are part of the title are kept`() {
        assertEquals("1984", TitleFormatter.displayTitle("1984", "1984.mp3", 0))
        assertEquals("2 Become 1", TitleFormatter.displayTitle("2 Become 1", "x.mp3", 5))
        assertEquals("01", TitleFormatter.displayTitle("01", "01.mp3", 1))
        assertEquals("1984", TitleFormatter.displayTitle("1984", "1984.mp3", 1))
        assertEquals("0412", TitleFormatter.displayTitle("0412", "x.mp3", 3))
    }

    @Test
    fun `file name is used when title tag is missing`() {
        assertEquals("Song", TitleFormatter.displayTitle(null, "05 Song.mp3", 5))
        assertEquals("Song", TitleFormatter.displayTitle("", "Song.ogg", 0))
    }
}
