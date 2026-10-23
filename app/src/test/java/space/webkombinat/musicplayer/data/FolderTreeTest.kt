package space.webkombinat.musicplayer.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FolderTreeTest {

    @Test
    fun `single-child chains are merged into one row`() {
        val tree = FolderTree.build(
            mapOf(
                "Huawei/CloudDrive/extractfiles/records/Kaki/" to 10,
                "Huawei/CloudDrive/extractfiles/records/Daikichi/" to 5,
                "Music/Records/01火気厳禁/" to 10,
                "Music/" to 1,
            ),
        )
        assertEquals(
            listOf(
                FolderNode("Huawei/CloudDrive/extractfiles/records/", "Huawei/CloudDrive/extractfiles/records", 0, 15),
                FolderNode("Huawei/CloudDrive/extractfiles/records/Daikichi/", "Daikichi", 1, 5),
                FolderNode("Huawei/CloudDrive/extractfiles/records/Kaki/", "Kaki", 1, 10),
                FolderNode("Music/", "Music", 0, 11),
                FolderNode("Music/Records/01火気厳禁/", "Records/01火気厳禁", 1, 10),
            ),
            tree,
        )
    }

    @Test
    fun `absolute paths keep the leading slash`() {
        val tree = FolderTree.build(mapOf("/storage/emulated/0/Music/A/" to 2, "/storage/emulated/0/Music/B/" to 3))
        assertEquals("/storage/emulated/0/Music/", tree.first().path)
        assertEquals("storage/emulated/0/Music", tree.first().name)
        assertEquals("/storage/emulated/0/Music/A/", tree[1].path)
    }

    @Test
    fun `excluded folder hides its sub folders but not similarly named ones`() {
        val excluded = setOf("Huawei/CloudDrive/extractfiles/records/")
        assertTrue(LibraryBuilder.isExcluded("Huawei/CloudDrive/extractfiles/records/Daikichi/", excluded))
        assertFalse(LibraryBuilder.isExcluded("Huawei/CloudDrive/extractfiles/records2/", excluded))
        assertFalse(LibraryBuilder.isExcluded("Music/Records/01火気厳禁/", excluded))
    }
}
