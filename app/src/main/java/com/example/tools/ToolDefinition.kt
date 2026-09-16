package com.example.tools

/**
 * Metadata and specification for tools that Baso AI can invoke.
 */
data class ToolDefinition(
  val id: String,
  val name: String,
  val description: String,
  val isEnabled: Boolean = false
)

sealed interface ToolResult {
  data class Success(val data: String) : ToolResult
  data class Failure(val error: String) : ToolResult
}

interface BasoTool {
  val definition: ToolDefinition
  suspend fun execute(parameters: Map<String, Any>): ToolResult
}
