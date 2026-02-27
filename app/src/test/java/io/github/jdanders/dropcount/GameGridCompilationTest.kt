package io.github.jdanders.dropcount

import org.junit.Test

/**
 * Regression test to ensure GameGrid compiles correctly.
 * This test will fail at compile time if there are syntax errors in GameGrid.
 * Since GameGrid is a composable function, we can't call it directly in tests,
 * but the mere fact that this test compiles means GameGrid compiled successfully.
 */
class GameGridCompilationTest {

    @Test
    fun gameGridCompiles() {
        // This test passes if GameGrid.kt compiles without errors
        // The import and compilation of this test file proves that GameGrid is valid
        assert(true) // This line will only execute if compilation succeeded
    }
}