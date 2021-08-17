package com.indialone.workmanagerlearnfull.workers

import android.content.Context
import android.util.Log
import androidx.work.Constraints
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.indialone.workmanagerlearnfull.Constants
import java.io.File
import java.lang.Exception

class CleanUpWorker(
    private val context: Context,
    private val workerParams: WorkerParameters
) : Worker(context, workerParams) {
    override fun doWork(): Result {
        makeStatusNotification("Cleaning up old temperory files", applicationContext)
        sleep()
        return try {

            val outputDirectory = File(applicationContext.filesDir, Constants.OUTPUT_PATH)

            if (outputDirectory.exists()) {
                val entries = outputDirectory.listFiles()
                if (entries != null) {
                    for (entry in entries) {
                        val name = entry.name
                        if (name.isNotEmpty() && name.endsWith(".png")) {
                            val deleted = entry.delete()
                            Log.d("clean up worker", "Deleted $name- $deleted")
                        }
                    }
                }
            }

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure()
        }
    }
}