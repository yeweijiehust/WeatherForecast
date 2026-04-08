package io.github.yeweijiehust.weatherforecast

import com.google.common.truth.Truth.assertWithMessage
import java.io.File
import org.junit.Test

class NoToastUsageTest {
    @Test
    fun mvpSource_doesNotUseAndroidToast() {
        val root = File(System.getProperty("user.dir") ?: ".")
        val sourceRoots = listOf(
            File(root, "app/src/main"),
            File(root, "app/src/androidTest"),
        )
        val offending = sourceRoots
            .flatMap { dir ->
                if (!dir.exists()) {
                    emptyList()
                } else {
                    dir.walkTopDown()
                        .filter { file ->
                            file.isFile && (file.extension == "kt" || file.extension == "java")
                        }
                        .filter { file ->
                            val text = file.readText()
                            text.contains("Toast.makeText(") || text.contains("android.widget.Toast")
                        }
                        .map { file -> file.invariantSeparatorsPath }
                        .toList()
                }
            }

        assertWithMessage("Toast usage is not allowed in MVP flows.")
            .that(offending)
            .isEmpty()
    }
}
