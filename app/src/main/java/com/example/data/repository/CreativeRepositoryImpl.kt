package com.example.data.repository

import com.example.data.local.CreativeProjectDao
import com.example.data.local.CreativeProjectEntity
import com.example.domain.model.CreativeProject
import com.example.domain.model.CreativeStatus
import com.example.domain.model.CreativeType
import com.example.domain.repository.CreativeRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CreativeRepositoryImpl(
  private val dao: CreativeProjectDao
) : CreativeRepository {

  override fun getAllProjects(): Flow<List<CreativeProject>> {
    return dao.getAllProjects().map { list -> list.map { it.toDomain() } }
  }

  override fun getProjectsByType(type: CreativeType): Flow<List<CreativeProject>> {
    return dao.getProjectsByType(type.name).map { list -> list.map { it.toDomain() } }
  }

  override suspend fun getProjectById(id: String): CreativeProject? {
    return dao.getProjectById(id)?.toDomain()
  }

  override suspend fun saveProject(project: CreativeProject) {
    dao.insertProject(project.toEntity())
  }

  override suspend fun updateProject(project: CreativeProject) {
    dao.updateProject(project.toEntity())
  }

  override suspend fun deleteProject(id: String) {
    dao.deleteProjectById(id)
  }

  override suspend fun renameProject(id: String, newTitle: String) {
    dao.renameProject(id, newTitle)
  }

  private fun CreativeProject.toEntity(): CreativeProjectEntity =
    CreativeProjectEntity.fromDomain(this)
}
