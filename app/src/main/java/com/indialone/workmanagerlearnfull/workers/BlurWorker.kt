package com.indialone.workmanagerlearnfull.workers

import android.content.Context
import android.graphics.BitmapFactory
import android.net.Uri
import android.text.TextUtils
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.indialone.workmanagerlearnfull.Constants
import com.indialone.workmanagerlearnfull.R
import java.lang.Exception
import java.lang.IllegalArgumentException

class BlurWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        val appContext = applicationContext
        val resourceUri = inputData.getString(Constants.KEY_IMAGE_URI)
        makeStatusNotification("Blurring image", appContext)

        sleep()

        return try {
            /*
            val picture = BitmapFactory.decodeResource(
                appContext.resources,
                R.drawable.mypic
            )
             */

            if (TextUtils.isEmpty(resourceUri)) {
                throw IllegalArgumentException("Invalid input uri")
            }

            val resolver = appContext.contentResolver
            val picture = BitmapFactory.decodeStream(
                resolver.openInputStream(Uri.parse(resourceUri))
            )

            val output = blurBitmap(picture, appContext)
            val outPutUri = writeBitmapToFile(appContext, output)
            makeStatusNotification("output is $outPutUri", appContext)

            val outputData = workDataOf(Constants.KEY_IMAGE_URI to outPutUri.toString())

            Result.success(outputData)

        } catch (e: Exception) {
            Log.d("blur worker", "Error applying blur")
            Result.failure()
        }

    }
}