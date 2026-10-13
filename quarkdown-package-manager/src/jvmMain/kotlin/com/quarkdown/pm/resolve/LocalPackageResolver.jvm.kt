package com.quarkdown.pm.resolve

import com.quarkdown.pm.install.DiskPackageWriter

/**
 * @return a resolver that installs packages onto the disk, from the default repositories
 */
fun LocalPackageResolver.Companion.disk(): LocalPackageResolver = LocalPackageResolver(writer = DiskPackageWriter())
