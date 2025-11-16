package org.ivcode.ai.synapp.agent

import io.github.ollama4j.Ollama
import io.github.ollama4j.models.chat.*
import org.ivcode.ai.synapp.history.OllamaHistoryManager
import org.ivcode.ai.synapp.history.OllamaHistoryMessage
import org.ivcode.ai.synapp.system.OllamaSystemMessage
import org.ivcode.ai.synapp.utils.toSynappToolCalls
import org.ivcode.ai.synapp.utils.withMessage
import java.util.*

class Ollama4jChatAgent(
    override val ollama: Ollama,
    override val model: String,
    systemMessages: List<OllamaSystemMessage>? = null,
    private val historyManager: OllamaHistoryManager,
): OllamaChatAgent {

    override val systemMessages: MutableList<OllamaSystemMessage> = systemMessages?.toMutableList() ?: mutableListOf()

    override fun chat(message: String, tokenHandler: OllamaChatTokenHandler?): OllamaChatResult {
        val builder = OllamaChatRequest.builder().withModel(model)

        // System info messages
        for (systemInfo in systemMessages.orEmpty()) {
            builder.withMessage(OllamaChatMessageRole.SYSTEM, systemInfo())
        }

        // Add existing history
        builder.withHistory()

        // Add user message
        builder.withMessage(OllamaChatMessageRole.USER, message)

        val response = ollama.chat(builder.build(), tokenHandler)

        historyManager.updateHistoryFromResponse(response)

        return response
    }

    override fun getSessionId(): UUID {
        return historyManager.id
    }

    override fun getChatHistory(): List<OllamaHistoryMessage> {
        return historyManager.getMessages()
    }

    private fun OllamaChatRequest.withHistory() {
        historyManager.getMessages().forEach { message ->
            withMessage(message)
        }
    }

    private fun OllamaHistoryManager.updateHistoryFromResponse(response: OllamaChatResult) {
        val collected = mutableListOf<OllamaChatMessage>()

        for (msg in response.chatHistory.asReversed()) {
            val roleName = msg.role.roleName
            collected.add(msg)

            if (roleName == OllamaChatMessageRole.USER.roleName) {
                break
            }
        }

        // Restore chronological order (oldest first) before setting
        collected.reverse()

        addMessages(
            collected.map {
                OllamaHistoryMessage(
                    role = it.role.roleName,
                    content = it.response,
                    toolCalls = it.toolCalls?.toSynappToolCalls()
                )
            }
        )
    }
}