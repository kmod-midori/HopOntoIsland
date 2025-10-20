package moe.reimu.hopontoisland.llm

import moe.reimu.hopontoisland.model.ModelRequest

interface ModelProvider {
    suspend fun generate(request: ModelRequest): String
}