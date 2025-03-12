package eu.iamgio.quarkdown.cli

import eu.iamgio.quarkdown.cli.watcher.DirectoryWatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.File
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * Tests for directory watching.
 * @see DirectoryWatcher
 */
class WatcherTest : TempDirectory() {
    private val file = File(directory, "file.txt")

    @BeforeTest
    fun setup() {
        directory.deleteRecursively()
        directory.mkdir()
    }

    /**
     * Watches the directory and performs an action that should trigger a change.
     * @param affect action that should trigger a change
     */
    private fun watch(
        exclude: File? = null,
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
                assertTrue(changed)
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
    fun exclude() {
        assertFailsWith<AssertionError> {
            watch(exclude = file) {
                file.writeText("Hello, world!")
            }
        }
    }
}
