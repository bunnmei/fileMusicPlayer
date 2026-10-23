package space.webkombinat.musicplayer.data

import java.text.Collator

/**
 * 「表示するフォルダ」画面の 1 行。
 * @param path フォルダのパス（末尾は必ず "/"）。除外設定にはこの値を保存する
 * @param name 表示名。1 本道のフォルダはまとめて "Huawei/CloudDrive/records" のように表示する
 * @param depth 字下げの深さ
 * @param songCount このフォルダとその配下にある曲数
 */
data class FolderNode(val path: String, val name: String, val depth: Int, val songCount: Int)

object FolderTree {

    private class Node(val segment: String, val path: String) {
        val children = sortedMapOf<String, Node>(Collator.getInstance())
        var directSongs = 0
        val totalSongs: Int get() = directSongs + children.values.sumOf { it.totalSongs }
    }

    /**
     * 曲が入っているフォルダごとの曲数から、親フォルダも含めたツリーを作って上から順に並べる。
     * 曲が無く子フォルダが 1 つだけのフォルダは、子と 1 行にまとめる。
     */
    fun build(songCountByFolder: Map<String, Int>): List<FolderNode> {
        val root = Node(segment = "", path = "")
        songCountByFolder.forEach { (folder, count) ->
            // API 28 以前は "/storage/emulated/0/Music/" のような絶対パスになる
            val prefix = if (folder.startsWith("/")) "/" else ""
            var node = root
            folder.split('/').filter { it.isNotEmpty() }.forEach { segment ->
                node = node.children.getOrPut(segment) { Node(segment, "${node.path.ifEmpty { prefix }}$segment/") }
            }
            node.directSongs += count
        }

        val result = mutableListOf<FolderNode>()
        fun visit(start: Node, depth: Int) {
            var node = start
            var name = node.segment
            while (node.directSongs == 0 && node.children.size == 1) {
                node = node.children.values.first()
                name += "/" + node.segment
            }
            result += FolderNode(path = node.path, name = name, depth = depth, songCount = node.totalSongs)
            node.children.values.forEach { visit(it, depth + 1) }
        }
        root.children.values.forEach { visit(it, 0) }
        return result
    }
}
