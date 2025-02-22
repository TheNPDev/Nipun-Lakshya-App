package com.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.data.db.models.entity.AssessmentSubmission

@Dao
interface AssessmentSubmissionsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(submissions: List<AssessmentSubmission>): List<Long>

    @Query("DELETE from assessment_submissions where id in (:dbIds)")
    fun delete(dbIds: MutableList<Long>)

    @Query("DELETE from assessment_submissions where student_id=:studentId")
    suspend fun deleteSubmissionForId(studentId: String)

    @Query("SELECT * from assessment_submissions")
    fun getSubmissions() : List<AssessmentSubmission>

    @Query("SELECT count(*) from assessment_submissions")
    fun getSubmissionsCount() : Long

    @Query("SELECT DISTINCT(school_udise) from assessment_submissions as ass inner join students as s where ass.student_id = s.id ")
    fun getUdiseWithUnSyncedStudents() : List<Long>

}
