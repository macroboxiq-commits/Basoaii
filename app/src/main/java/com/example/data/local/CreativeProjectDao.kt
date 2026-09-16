package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CreativeProjectDao {
  @Query("SELECT * FROM creative_projects ORDER BY createdAt DESC")
  fun getAllProjects(): Flow<List<CreativeProjectEntity>>

  @Query("SELECT * FROM creative_projects WHERE type = :type ORDER BY createdAt DESC")
  fun getProjectsByType(type: String): Flow<List<CreativeProjectEntity>>

  @Query("SELECT * FROM creative_projects WHERE id = :id LIMIT 1")
  suspend fun getProjectById(id: String): CreativeProjectEntity?

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insertProject(project: CreativeProjectEntity)

  @Update
  suspend fun updateProject(project: CreativeProjectEntity)

  @Delete
  suspend fun deleteProject(project: CreativeProjectEntity)

  @Query("DELETE FROM creative_projects WHERE id = :id")
  suspend fun deleteProjectById(id: String)

  @Query("UPDATE creative_projects SET title = :newTitle, updatedAt = :updatedAt WHERE id = :id")
  suspend fun renameProject(id: String, newTitle: String, updatedAt: Long = System.currentTimeMillis())
}
