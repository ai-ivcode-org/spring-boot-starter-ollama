package org.ivcode.ai.ollama.cli

import io.github.ollama4j.Ollama
import io.github.ollama4j.models.chat.OllamaChatMessageRole
import io.github.ollama4j.models.chat.OllamaChatRequest
import io.github.ollama4j.models.chat.OllamaChatTokenHandler


/**
 * Start a simple, blocking REPL-style chat session using this `Ollama` client.
 *
 * Purpose:
 * - Provide an easy manual test harness to interact with a local Ollama model and observe
 *   streamed token output as the model responds.
 * - Maintain an in-memory conversation history so the model can keep context across turns.
 *
 * Behavior:
 * - Prompts the user with "You: " and reads a single line from stdin.
 * - Typing `exit` (case-insensitive) or sending EOF will end the session and print "Goodbye.".
 * - Empty input lines are ignored and do not affect conversation history.
 * - Each user message is added to the in-memory `history`, and that history is sent with
 *   each `OllamaChatRequest` so the model sees prior turns.
 * - Responses are streamed using `OllamaChatTokenHandler`. Token chunks are printed as they arrive
 *   and accumulated; when a response indicates completion (`resp.isDone`), the accumulated
 *   assistant message is appended to the history.
 *
 * Usage example:
 * val ollama = Ollama(\"http://localhost:11434/\")   // or an existing client
 * ollama.startChat(\"your-model-name\")
 *
 * Notes and limitations:
 * - This is a blocking, single-threaded REPL intended for local experimentation and debugging.
 * - Not safe for concurrent use; no persistence of conversation history beyond runtime.
 *
 * @param model The name of the Ollama model to use for chat requests. Must be available to the Ollama server.
 */
fun Ollama.startChat(model: String) {
    // In-memory history of (role, content) pairs so the model can maintain context between turns.
    val history = mutableListOf<Pair<OllamaChatMessageRole, String>>()

    // Main REPL loop: read user input, send a chat request including history, stream response.
    while (true) {
        print("User: ")
        val userInput = readlnOrNull()?.trim()
        // Exit on EOF or the literal "exit" (case-insensitive)
        if (userInput == null || userInput.equals("exit", ignoreCase = true)) break
        // Ignore empty lines (no-op)
        if (userInput.isEmpty()) continue

        // Append the user's message to history
        history.add(OllamaChatMessageRole.USER to userInput)

        // Build a chat request including the full conversation history so far.
        val builder = OllamaChatRequest.builder().withModel(model)
        for ((role, content) in history) {
            builder.withMessage(role, content)
        }
        val request = builder.build()

        // StringBuilder accumulates streamed chunks for the assistant's reply.
        val sb = StringBuilder()
        print("Assistant: ")
        // Handler for streaming tokens/chunks from the model.
        val streamListener = OllamaChatTokenHandler { resp ->
            try {
                // Each callback may contain a chunk of the assistant's response.
                val chunk = resp.message?.response
                if (!chunk.isNullOrEmpty()) {
                    // Print chunk immediately to provide streaming output to the user.
                    print (chunk)
                    System.out.flush()
                    // Accumulate for when the response is complete.
                    sb.append(chunk)
                }
                // When the stream signals completion, commit the accumulated assistant message to history.
                val done = resp.isDone
                if (done) {
                    println()
                    val full = sb.toString()
                    history.add(OllamaChatMessageRole.ASSISTANT to full)
                    sb.setLength(0)
                }
            } catch (ex: Exception) {
                // Catch and report any streaming-related exception, but keep the REPL running.
                println("\n[stream error] ${ex.message}")
            }
        }

        try {
            // Send the request and stream the response using the listener.
            chat(request, streamListener)
        } catch (ex: Exception) {
            // Print errors from the synchronous `chat` call and continue the REPL.
            println("Error: ${ex.message}")
            ex.printStackTrace()
        }
    }

    // Clean exit message when the REPL loop ends.
    println("Goodbye.")
}