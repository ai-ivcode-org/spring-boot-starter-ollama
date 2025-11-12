package org.ivcode.ai.ollama.cli

import io.github.ollama4j.Ollama


/**
 * Simple test entry point that starts the interactive Ollama CLI.
 *
 * This program:
 * - Creates an `Ollama` client pointed at a local Ollama server (default: http://localhost:11434/).
 * - Calls the `startChat` REPL helper with a specified model name so you can manually test model behavior.
 *
 * Notes (for testing only):
 * - A running Ollama server must be accessible at the configured URL.
 * - Replace the model name ("gpt-oss:20b") with any model available to your Ollama instance.
 * - This is a blocking, single-threaded test runner and is not intended for production use.
 */
internal fun main() {
    // Create a client for a locally running Ollama server and start the interactive chat REPL
    // using the "gpt-oss:20b" model. Change the model string to test a different model.
    Ollama("http://localhost:11434").startChat("gpt-oss:20b")
}