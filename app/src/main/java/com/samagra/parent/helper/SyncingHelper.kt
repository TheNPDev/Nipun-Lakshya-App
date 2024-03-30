package com.samagra.parent.helper

import com.data.db.DbHelper
import com.data.db.models.entity.AssessmentSubmission
import com.data.db.models.entity.SchoolSubmission
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.morziz.network.config.ClientType
import com.morziz.network.network.Network
import com.samagra.ancillaryscreens.data.model.AssessmentService
import com.samagra.ancillaryscreens.data.prefs.CommonsPrefsHelperImpl
import com.samagra.commons.MetaDataExtensions
import com.samagra.commons.models.submitresultsdata.ResultsVisitData
import com.samagra.commons.models.submitresultsdata.StudentResults
import com.samagra.commons.models.submitresultsdata.SubmitResultsModel
import com.samagra.commons.models.surveydata.AssessmentSurveyModel
import com.samagra.commons.models.surveydata.SurveyModel
import com.samagra.commons.models.surveydata.SurveyResultsModel
import com.samagra.commons.utils.CommonConstants
import com.samagra.commons.utils.NetworkStateManager
import com.samagra.commons.utils.RemoteConfigUtils
import com.samagra.parent.BuildConfig
import com.samagra.parent.UtilityFunctions
import com.samagra.parent.ui.competencyselection.StudentsAssessmentData
import com.samagra.parent.ui.getBearerAuthToken
import timber.log.Timber

class SyncingHelper {



    fun syncSubmissions(prefs: CommonsPrefsHelperImpl): Boolean {
        return syncSubmissions(prefs, DbHelper.db.getAssessmentSubmissionDao().getSubmissions())
    }

    fun syncSchoolSubmission(prefs: CommonsPrefsHelperImpl): Boolean {
        return postSchoolSubmissions(DbHelper.db.getSchoolSubmissionDao().getSubmissions(), prefs)
    }

    fun syncSchoolSubmission(prefs: CommonsPrefsHelperImpl, schoolSubmission: List<SchoolSubmission>): Boolean {
        return postSchoolSubmissions(schoolSubmission, prefs)
    }


    fun syncSubmissions(
        prefs: CommonsPrefsHelperImpl,
        resultSubmissionList: List<AssessmentSubmission>
    ): Boolean {
        Timber.i("Submissions List " + resultSubmissionList.size)
        return try {
            if (resultSubmissionList.isNotEmpty()) {
                postStudentsAssessmentSubmissions(resultSubmissionList, prefs)
            } else {
                true
            }
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }


    private fun postStudentsAssessmentSubmissions(
        resultSubmissionList: List<AssessmentSubmission>,
        prefs: CommonsPrefsHelperImpl
    ): Boolean {
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            return false
        }
        var isSuccess = true;
        resultSubmissionList.chunked(
            RemoteConfigUtils.getFirebaseRemoteConfigInstance()
                .getString(RemoteConfigUtils.RESULTS_INSERTION_CHUNK_SIZE).toInt()
        ) {
            val dbIds = mutableListOf<Long>()
            val submissions = mutableListOf<com.data.models.submissions.SubmitResultsModel>()
            it.forEach { model ->
                dbIds.add(model.id)
                submissions.add(model.studentSubmissions!!)
            }
            Timber.i("submission ids list to be deleted! $dbIds")
            val response =
                generateApiServiceDataModule().postSubmissions(prefs.getBearerAuthToken(), submissions)
                    .execute()
            if (response.isSuccessful) {
                DbHelper.db.getAssessmentSubmissionDao().delete(dbIds)
            } else {
                // Currently not stopping loop if API fails
                isSuccess = false
                Timber.i("Submissions Response Failure - %s", response.message())
                Timber.i("Submissions Response Code - %s", response.code())
                Timber.i("Submissions Response Error - %s", response.errorBody()?.string())
            }
        }
        return isSuccess
    }

    private fun postSchoolSubmissions(
        submissions: List<SchoolSubmission>,
        prefs: CommonsPrefsHelperImpl
    ): Boolean {
        if (NetworkStateManager.instance?.networkConnectivityStatus != true) {
            return false
        }
        var isSuccess = true
        val db = DbHelper.db
        val udisesToIgnore =
            db.getAssessmentSubmissionDao().getUdiseWithUnSyncedStudents()
        submissions.forEach loop@{
            if(udisesToIgnore.contains(it.udise)) {
                isSuccess = false
                return@loop
            }
            val response =
                generateApiService().postSchoolSubmission(
                    prefs.getBearerAuthToken(),
                    it.udise,
                    it.cycleId
                ).execute()
            if (response.isSuccessful) {
                db.getSchoolSubmissionDao().delete(it.id)
            } else {
                // Currently not stopping loop if API fails
                isSuccess = false
                Timber.i("Submissions Response Failure - %s", response.message())
                Timber.i("Submissions Response Code - %s", response.code())
                Timber.i("Submissions Response Error - %s", response.errorBody()?.string())
            }
        }
        return isSuccess
    }

    private fun generateApiService(): AssessmentService {
        return Network.getClient(
            ClientType.RETROFIT,
            AssessmentService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }

    private fun generateApiServiceDataModule(): com.data.network.AssessmentService {
        return Network.getClient(
            ClientType.RETROFIT,
            com.data.network.AssessmentService::class.java,
            CommonConstants.IDENTITY_APP_SERVICE
        )!!
    }
}