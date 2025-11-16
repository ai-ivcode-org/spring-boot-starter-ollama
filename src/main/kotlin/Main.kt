package org.ivcode.ai.synapp

import com.fasterxml.jackson.databind.DeserializationFeature
import io.github.ollama4j.utils.Utils
import org.ivcode.ai.synapp.agent.OllamaChatAgent
import org.ivcode.ai.synapp.agent.OllamaChatAgentFactory
import org.ivcode.ai.synapp.config.OllamaConfig
import org.ivcode.ai.synapp.system.CliSystemMessage
import org.springframework.boot.CommandLineRunner
import org.springframework.boot.WebApplicationType
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.SpringApplication
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import


@SpringBootApplication
@Import(OllamaConfig::class)
class CliApp {

    @Bean
    fun runner(agentFactory: OllamaChatAgentFactory) = CommandLineRunner { args ->
        val session = agentFactory.createOllamaSession()
        session.systemMessages.add(CliSystemMessage())
        session.startChat()
    }
}

fun main(args: Array<String>) {
    Utils.getObjectMapper().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)

    val app = SpringApplication(CliApp::class.java)
    app.webApplicationType = WebApplicationType.NONE // ensures no web server
    app.run(*args)
}

internal fun OllamaChatAgent.startChat() {

    // Main REPL loop: read user input, send a chat request including history, stream response.
    while (true) {
        print("User: ")
        val userInput = readlnOrNull()?.trim()
        // Exit on EOF or the literal "exit" (case-insensitive)
        if (userInput == null || userInput.equals("exit", ignoreCase = true)) break
        // Ignore empty lines (no-op)
        if (userInput.isEmpty()) continue

        print("Assistant: ")
        chat(userInput) { resp ->
            val chunk = resp.message?.response
            if (!chunk.isNullOrEmpty()) {
                print(chunk)
            }

            val done = resp.isDone
            if (done) {
                println()
            }
        }
    }

    // Clean exit message when the REPL loop ends.
    println("Goodbye.")
}