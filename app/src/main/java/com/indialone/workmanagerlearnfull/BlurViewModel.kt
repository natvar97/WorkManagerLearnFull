package com.indialone.workmanagerlearnfull

import android.app.Application
import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.strictmode.CleartextNetworkViolation
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.work.*
import com.indialone.workmanagerlearnfull.workers.BlurWorker
import com.indialone.workmanagerlearnfull.workers.CleanUpWorker
import com.indialone.workmanagerlearnfull.workers.SaveImageToFileWorker

class BlurViewModel(application: Application) : ViewModel() {

    internal var imageUri: Uri? = null
    internal var outputUri: Uri? = null
    internal val outputWorkInfos: LiveData<List<WorkInfo>>
    internal val progressWorkInfoItems: LiveData<List<WorkInfo>>

    private val workManager = WorkManager.getInstance(application)

    init {
        imageUri = getImageUri(application.applicationContext)
        outputWorkInfos = workManager.getWorkInfosByTagLiveData(Constants.TAG_OUTPUT)
        progressWorkInfoItems = workManager.getWorkInfosByTagLiveData(Constants.TAG_PROGRESS)
    }

    internal fun applyBlur(blurLevel: Int) {
//        workManager.enqueue(OneTimeWorkRequest.from(BlurWorker::class.java))

        /*
            * for begin from clearn up worker

        var continuation = workManager.beginWith(OneTimeWorkRequest.from(CleanUpWorker::class.java))
         */

        // this for a unique work at a time
//        val constraints = Constraints.Builder()
//            .setRequiresCharging(true)
//            .build()

        var continuation = workManager.beginUniqueWork(
            Constants.IMAGE_MANIPULATION_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            OneTimeWorkRequest.from(CleanUpWorker::class.java)
        )

        for (i in 0 until blurLevel) {
            val blurBuilder = OneTimeWorkRequestBuilder<BlurWorker>()
            if (i == 0) {
                blurBuilder.setInputData(createInputDataForUri())
            }
            continuation = continuation.then(blurBuilder.build())
        }

        /*
        val blurRequest = OneTimeWorkRequestBuilder<BlurWorker>()
            .setInputData(createInputDataForUri())
            .build()
        continuation = continuation.then(blurRequest)

         */

        val save = OneTimeWorkRequest
            .Builder(SaveImageToFileWorker::class.java)
//            .setConstraints(constraints)
            .addTag(Constants.TAG_OUTPUT)
            .build()
        continuation = continuation.then(save)
        continuation.enqueue()
//        workManager.enqueue(blurRequest)
    }

    internal fun cancelWork() {
        workManager.cancelUniqueWork(Constants.IMAGE_MANIPULATION_WORK_NAME)
    }

    private fun uriOrNull(uriString: String?): Uri? {
        return if (!uriString.isNullOrEmpty()) {
            Uri.parse(uriString)
        } else {
            null
        }
    }

    private fun createInputDataForUri(): Data {
        val builder = Data.Builder()
        imageUri?.let {
            builder.putString(Constants.KEY_IMAGE_URI, imageUri.toString())
        }
        return builder.build()
    }

    private fun getImageUri(context: Context): Uri {
        val resources = context.resources

        val imageUri = Uri.Builder()
            .scheme(ContentResolver.SCHEME_ANDROID_RESOURCE)
            .authority(resources.getResourcePackageName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceTypeName(R.drawable.android_cupcake))
            .appendPath(resources.getResourceEntryName(R.drawable.android_cupcake))
            .build()

        return imageUri
    }

    internal fun setOutputUri(outputImageUri: String?) {
        outputUri = uriOrNull(outputImageUri)
    }

    class BlurViewModelFactory(private val application: Application) : ViewModelProvider.Factory {

        override fun <T : ViewModel?> create(modelClass: Class<T>): T {
            return if (modelClass.isAssignableFrom(BlurViewModel::class.java)) {
                BlurViewModel(application) as T
            } else {
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
