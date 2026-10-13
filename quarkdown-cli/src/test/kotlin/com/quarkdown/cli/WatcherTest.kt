package com.quarkdown.cli

import com.quarkdown.cli.watcher.DirectoryWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

/**
 * Tests for directory watching.
 * @see DirectoryWatcher
 */
class WatcherTest : TempDirectory() {
    private val file = File(directory, "file.txt")

    @BeforeTest
    fun setup() {
        super.reset()
    }

    /**
     * Watches the directory and performs an action, then asserts whether a change was observed.
     * @param exclude directory excluded from watching
     * @param expectChange whether [affect] is expected to trigger a change
     * @param affect action performed while watching
     */
    private fun watch(
        exclude: File? = null,
        expectChange: Boolean = true,
        affect: () -> Unit,
    ) {
        file.createNewFile()
        var changed = false

        runBlocking {
            val watcher =
                DirectoryWatcher.create(directory, exclude) {
                    changed = true
                }

            launch(Dispatchers.IO) {
                watcher.watchBlocking()
            }

            launch {
                delay(1000)
                affect()
                delay(500)
                watcher.stop()
                delay(400)
                assertEquals(expectChange, changed)
            }
        }
    }

    @Test
    fun `file change`() =
        watch {
            file.writeText("Hello, world!")
        }

    @Test
    fun `file creation`() =
        watch {
            File(directory, "new-file.txt").createNewFile()
        }

    @Test
    fun `file deletion`() =
        watch {
            file.delete()
        }

    @Test
    fun `writes inside a package directory are ignored`() {
        val packageDir = File(directory, "@atoms/cv").apply { mkdirs() }
        watch(expectChange = false) {
            File(packageDir, "main.qd").writeText("# CV")
        }
    }

    @Test
    fun exclude() {
        assertFailsWith<AssertionError> {
            watch(exclude = file) {
                file.writeText("Hello, world!")
            }
        }
    }
}
