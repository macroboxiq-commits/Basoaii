package com.example.domain.repository

import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeType
import kotlinx.coroutines.flow.Flow

interface CreativeRepository {
  fun getAllProjects(): Flow<List<CreativeProject>>
  fun getProjectsByType(type: CreativeType): Flow<List<CreativeProject>>
  suspend fun getProjectById(id: String): CreativeProject?
  suspend fun saveProject(project: CreativeProject)
  suspend fun updateProject(project: CreativeProject)
  suspend fun deleteProject(id: String)
  suspend fun renameProject(id: String, newTitle: String)
}
