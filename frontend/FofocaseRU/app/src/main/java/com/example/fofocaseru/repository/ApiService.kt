import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import com.example.fofocaseru.repository.RetrofitClient
import com.example.fofocaseru.repository.TranscribedAudioDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

interface ApiService {
    @Multipart
    @POST("/process-audio/")
    suspend fun uploadAudio(
        @Part file: MultipartBody.Part
    ): Response<TranscribedAudioDto>
}


suspend fun uploadAudioFile(context: Context, uri: Uri): List<String>? {
    val apiService = RetrofitClient.createService(ApiService::class.java)
    val tempFile = createTempFileFromUri(context, uri)
    val requestFile = tempFile.asRequestBody("audio/*".toMediaTypeOrNull())
    val body = MultipartBody.Part.createFormData("file", tempFile.name, requestFile)
    try {
        val response = apiService.uploadAudio(body).body()
        return response?.taskList?.split("\n")?.map { it.trim() }
    } catch (e: Exception) {
        Log.e("EBARRRR", "Error uploading file", e)
    } finally {
        tempFile.delete()
    }
    return listOf()
}

private fun createTempFileFromUri(context: Context, uri: Uri): File {
    val fileName = getFileName(context, uri)
    val tempFile = File(context.cacheDir, fileName)
    val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
    val outputStream = FileOutputStream(tempFile)
    inputStream?.use { input ->
        outputStream.use { output ->
            input.copyTo(output)
        }
    }
    return tempFile
}

private fun getFileName(context: Context, uri: Uri): String {
    var name = "temp_file"
    val cursor = context.contentResolver.query(uri, null, null, null, null)
    cursor?.use {
        if (it.moveToFirst()) {
            val nameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (nameIndex != -1) {
                name = it.getString(nameIndex)
            }
        }
    }
    return name
}