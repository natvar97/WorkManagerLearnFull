package com.indialone.workmanagerlearnfull.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.indialone.workmanagerlearnfull.Constants
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*

class SaveImageToFileWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {
    private val title = "Blurred_Image"
    private val dateFormatter = SimpleDateFormat(
        "yyyy:MM:dd 'at' HH:mm:ss z",
        Locale.getDefault()
    )

    override fun doWork(): Result {
        makeStatusNotification("Saving Image", applicationContext)
        sleep()

        val resolver = applicationContext.contentResolver

        return try {
            val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI)
            val bitmap = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )
            val imageUrl = MediaStore.Images.Media.insertImage(
                resolver, bitmap, title, dateFormatter.format(Date())
            )

            if (!imageUrl.isNullOrEmpty()) {
                val output = workDataOf(Constants.KEY_IMAGE_URI to imageUrl)
                Result.success()
            } else {
                Log.d("saveimagetofileworker", "Saving image is failed")
                Result.failure()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }

    }
}