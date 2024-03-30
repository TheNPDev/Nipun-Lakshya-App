package com.samagra.commons.models.submitresultsdata


open class ResultsVisitData(
    var submissionTimeStamp: Long?,
    var mentor_id: Int?,
    var flowUUID: String?,
    var grade: Int?,
    var subject: String?,
    var is_visited: Boolean = true,
    var module_result: String?,
    var no_of_student: Int?,
    var udise_code: String? = "0",
    var total_time_taken: String?,
    var actor: String?,
    var block: String?,
    var studentSession: String?,
)
