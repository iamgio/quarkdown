package eu.iamgio.quarkdown.pdf.html

/**
 * Wrapper for invoking the Node Package Manager.
 * @param path path to the NPM executable
 */
class NpmWrapper(
    override val path: String = DEFAULT_PATH,
) : ExecutableWrapper() {
    override val isValid: Boolean
        get() =
            try {
                super.getCommandOutput("--version").trim().let {
                    it.isNotBlank() && it.lines().size == 1
                }
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }

    companion object {
        const val DEFAULT_PATH = "npm"
    }
}
