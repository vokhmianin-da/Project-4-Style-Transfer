package com.example.transferstyle

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var helper: StyleTransferHelper
    private lateinit var imageViewResult: ImageView
    private lateinit var imageViewPreviewContent: ImageView
    private lateinit var imageViewPreviewStyle: ImageView
    private lateinit var btnStylize: Button

    private var contentUri: Uri? = null
    private var styleUri: Uri? = null

    // Регистрируем контракт для выбора изображения
    private val getContent = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            when (currentPickerMode) {
                PickerMode.CONTENT -> {
                    contentUri = it
                    loadImageToView(it, imageViewPreviewContent)
                }
                PickerMode.STYLE -> {
                    styleUri = it
                    loadImageToView(it, imageViewPreviewStyle)
                }
            }
            updateStylizeButton()
        }
    }

    private enum class PickerMode { CONTENT, STYLE }
    private var currentPickerMode = PickerMode.CONTENT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        helper = StyleTransferHelper(this)
        imageViewResult = findViewById(R.id.imageViewResult)
        imageViewPreviewContent = findViewById(R.id.imageViewPreviewContent)
        imageViewPreviewStyle = findViewById(R.id.imageViewPreviewStyle)
        btnStylize = findViewById(R.id.btnStylize)

        findViewById<Button>(R.id.btnSelectContent).setOnClickListener {
            currentPickerMode = PickerMode.CONTENT
            getContent.launch("image/*")
        }

        findViewById<Button>(R.id.btnSelectStyle).setOnClickListener {
            currentPickerMode = PickerMode.STYLE
            getContent.launch("image/*")
        }

        btnStylize.setOnClickListener {
            val contentBitmap = uriToBitmap(contentUri)
            val styleBitmap = uriToBitmap(styleUri)
            if (contentBitmap == null || styleBitmap == null) {
                Toast.makeText(this, "Failed to load images", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val btnSave = findViewById<Button>(R.id.button_save)
            btnSave.setOnClickListener {
                saveResultImage()
            }

            btnStylize.isEnabled = false
            Toast.makeText(this, "Stylizing... please wait", Toast.LENGTH_SHORT).show()

            Thread {
                val result = helper.stylize(contentBitmap, styleBitmap)
                runOnUiThread {
                    imageViewResult.setImageBitmap(result)
                    btnStylize.isEnabled = true
                    Toast.makeText(this, "Done!", Toast.LENGTH_SHORT).show()
                }
            }.start()
        }
    }

    private fun loadImageToView(uri: Uri, imageView: ImageView) {
        try {
            val bitmap = MediaStore.Images.Media.getBitmap(contentResolver, uri)
            imageView.setImageBitmap(bitmap)
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun uriToBitmap(uri: Uri?): Bitmap? {
        if (uri == null) return null
        return try {
            MediaStore.Images.Media.getBitmap(contentResolver, uri)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun updateStylizeButton() {
        btnStylize.isEnabled = contentUri != null && styleUri != null
    }

    private fun saveResultImage() {
        // Получаем изображение из ImageView (предполагается, что оно там есть)
        val drawable = imageViewResult.drawable ?: return
        // Конвертируем Drawable в Bitmap (если это BitmapDrawable)
        val bitmap = (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
        if (bitmap == null) {
            Toast.makeText(this, "No image to save", Toast.LENGTH_SHORT).show()
            return
        }

        // Используем Intent ACTION_CREATE_DOCUMENT для выбора места и имени файла
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/jpeg"
            putExtra(Intent.EXTRA_TITLE, "stylized_${System.currentTimeMillis()}.jpg")
        }
        startIntentForResult.launch(intent)
    }

    // Регистрируем контракт для получения результата выбора файла
    private val startIntentForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            result.data?.data?.let { uri ->
                // Сохраняем Bitmap по полученному URI
                saveBitmapToUri(bitmapFromImageView(), uri)
            }
        }
    }

    // Вспомогательная функция для получения текущего Bitmap (можно вынести)
    private fun bitmapFromImageView(): Bitmap? {
        val drawable = imageViewResult.drawable ?: return null
        return (drawable as? android.graphics.drawable.BitmapDrawable)?.bitmap
    }

    // Функция сохранения Bitmap в указанный URI
    private fun saveBitmapToUri(bitmap: Bitmap?, uri: Uri) {
        if (bitmap == null) return
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
                outputStream.flush()
            }
            Toast.makeText(this, "Image saved successfully", Toast.LENGTH_SHORT).show()
        } catch (e: IOException) {
            e.printStackTrace()
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show()
        }
    }
}