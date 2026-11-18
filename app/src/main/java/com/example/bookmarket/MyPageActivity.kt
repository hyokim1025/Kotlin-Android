package com.example.bookmarket

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.content.FileProvider
import androidx.lifecycle.lifecycleScope
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyPageActivity : CheckoutActivity() {
    private lateinit var photoStore: ProfilePhotoStore
    private lateinit var profileImage: ImageView
    private lateinit var editPhoto: ImageButton

    private val takePhoto = registerForActivityResult(ActivityResultContracts.TakePicture()) { success ->
        if (success) {
            savePhoto(cameraUri(), deleteCapture = true)
        } else {
            photoStore.captureFile.delete()
        }
    }

    private val pickPhoto = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) savePhoto(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        photoStore = ProfilePhotoStore(this)
        showCheckout(R.layout.activity_mypage, "나의 페이지")
        profileImage = findViewById(R.id.profile_image)
        editPhoto = findViewById(R.id.profile_edit)
        lifecycleScope.launch {
            val photo = withContext(Dispatchers.IO) { photoStore.read() }
            if (photo != null && editPhoto.isEnabled) {
                profileImage.setImageBitmap(photo)
            }
        }

        editPhoto.setOnClickListener { showPhotoOptions() }
        findViewById<Button>(R.id.open_memo).setOnClickListener {
            startActivity(Intent(this, MemoActivity::class.java))
        }
    }

    private fun showPhotoOptions() {
        val content = layoutInflater.inflate(R.layout.dialog_profile_photo, null)
        val dialog = AlertDialog.Builder(this)
            .setTitle("선택하세요")
            .setView(content)
            .create()

        content.findViewById<View>(R.id.profile_camera).setOnClickListener {
            dialog.dismiss()
            try {
                photoStore.prepareCapture()
                takePhoto.launch(cameraUri())
            } catch (_: ActivityNotFoundException) {
                showMessage("카메라 앱을 열 수 없습니다")
            } catch (_: IOException) {
                showMessage("사진을 임시 저장할 수 없습니다")
            }
        }

        content.findViewById<View>(R.id.profile_gallery).setOnClickListener {
            dialog.dismiss()
            try {
                pickPhoto.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            } catch (_: ActivityNotFoundException) {
                showMessage("사진 선택 화면을 열 수 없습니다")
            }
        }
        dialog.show()
    }

    private fun cameraUri(): Uri = FileProvider.getUriForFile(
        this, packageName + ".profilephotos", photoStore.captureFile
    )

    private fun savePhoto(uri: Uri, deleteCapture: Boolean = false) {
        editPhoto.isEnabled = false
        lifecycleScope.launch {
            try {
                val photo = withContext(Dispatchers.IO) {
                    try {
                        photoStore.save(uri)
                    } finally {
                        if (deleteCapture) photoStore.captureFile.delete()
                    }
                }
                profileImage.setImageBitmap(photo)
                showMessage("프로필 사진을 저장했습니다")
            } catch (_: IOException) {
                showMessage("사진을 읽거나 저장하지 못했습니다")
            } catch (_: SecurityException) {
                showMessage("선택한 사진에 접근할 수 없습니다")
            } catch (_: IllegalArgumentException) {
                showMessage("이 사진을 불러올 수 없습니다")
            } finally {
                editPhoto.isEnabled = true
            }
        }
    }

    private fun showMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}
