package com.example.fofocaseru.repository

import com.google.gson.annotations.SerializedName

data class TranscribedAudioDto(
    @SerializedName("task_list") val taskList: String
)