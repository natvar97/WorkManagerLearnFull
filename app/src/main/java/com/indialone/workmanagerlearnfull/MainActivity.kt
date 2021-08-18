package com.indialone.workmanagerlearnfull

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.lifecycle.ViewModelProvider
import androidx.work.WorkInfo
import com.indialone.workmanagerlearnfull.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var mBinding: ActivityMainBinding
    private lateinit var blurViewModel: BlurViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        mBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        blurViewModel =
            ViewModelProvider(this, BlurViewModel.BlurViewModelFactory(application)).get(
                BlurViewModel::class.java
            )

        mBinding.goButton.setOnClickListener { blurViewModel.applyBlur(blurLevel) }

        blurViewModel.outputWorkInfos.observe(this) { listWorkInfos ->
            if (listWorkInfos.isNullOrEmpty()) {
                return@observe
            }
            val workInfo = listWorkInfos[0]
            if (workInfo.state.isFinished) {
                showWorkFinished()
                val outPutImageUri = workInfo.outputData.getString(Constants.KEY_IMAGE_URI)
                if (!outPutImageUri.isNullOrEmpty()) {
                    blurViewModel.setOutputUri(outPutImageUri)
                    mBinding.seeFileButton.visibility = View.VISIBLE
                }
            } else {
                showWorkInProgress()
            }
        }

        blurViewModel.progressWorkInfoItems.observe(this) { listOfWorkInfos ->
            if (listOfWorkInfos.isNullOrEmpty())
                return@observe

            listOfWorkInfos.forEach { workInfo ->
                if (WorkInfo.State.RUNNING == workInfo.state) {
                    val progress = workInfo.progress.getInt(Constants.PROGRESS, 0)
                    mBinding.progressBar.progress = progress
                }
            }

        }

        mBinding.seeFileButton.setOnClickListener {
            blurViewModel.outputUri?.let { uri ->
                val actionView = Intent(Intent.ACTION_VIEW, uri)
                actionView.resolveActivity(packageManager)?.run {
                    startActivity(actionView)
                }
            }
        }

        mBinding.cancelButton.setOnClickListener { blurViewModel.cancelWork() }

    }

    private fun showWorkInProgress() {
        with(mBinding) {
            progressBar.visibility = View.VISIBLE
            cancelButton.visibility = View.VISIBLE
            goButton.visibility = View.GONE
            seeFileButton.visibility = View.GONE
        }
    }

    private fun showWorkFinished() {
        with(mBinding) {
            progressBar.visibility = View.GONE
            cancelButton.visibility = View.GONE
            goButton.visibility = View.VISIBLE
            progressBar.progress = 0
        }
    }

    private val blurLevel: Int
        get() =
            when (mBinding.radioBlurGroup.checkedRadioButtonId) {
                R.id.radio_blur_lv_1 -> 1
                R.id.radio_blur_lv_2 -> 2
                R.id.radio_blur_lv_3 -> 3
                else -> 1
            }

}