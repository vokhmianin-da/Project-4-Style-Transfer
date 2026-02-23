package com.example.transferstyle

import android.content.Context
import android.graphics.Bitmap
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import org.pytorch.torchvision.TensorImageUtils
import java.io.File
import java.io.FileOutputStream

class StyleTransferHelper(context: Context) {
    private val module: Module
    private val contentSize = 384
    private val styleSize = 256

    init {
        module = Module.load(assetFilePath(context, "adain_style.pt"))
    }

    private fun bitmapToTensor(bitmap: Bitmap, size: Int): Tensor {
        val resized = Bitmap.createScaledBitmap(bitmap, size, size, true)
        return TensorImageUtils.bitmapToFloat32Tensor(
            resized,
            floatArrayOf(0f, 0f, 0f),
            floatArrayOf(1f, 1f, 1f)
        )
    }

    private fun tensorToBitmap(tensor: Tensor): Bitmap {
        val shape = tensor.shape()
        require(shape.size == 4 && shape[0] == 1L && shape[1] == 3L) {
            "Invalid tensor shape: ${shape.joinToString()}"
        }
        val h = shape[2].toInt()
        val w = shape[3].toInt()
        val data = tensor.dataAsFloatArray
        val stride = w * h
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)

        for (y in 0 until h) {
            for (x in 0 until w) {
                val r = (data[y * w + x] * 255).coerceIn(0f, 255f).toInt()
                val g = (data[stride + y * w + x] * 255).coerceIn(0f, 255f).toInt()
                val b = (data[2 * stride + y * w + x] * 255).coerceIn(0f, 255f).toInt()
                bitmap.setPixel(x, y, android.graphics.Color.rgb(r, g, b))
            }
        }
        return bitmap
    }

    fun stylize(contentBitmap: Bitmap, styleBitmap: Bitmap): Bitmap {
        val contentTensor = bitmapToTensor(contentBitmap, contentSize)
        val styleTensor = bitmapToTensor(styleBitmap, styleSize)
        val outputTensor = module.forward(IValue.from(contentTensor), IValue.from(styleTensor)).toTensor()
        return tensorToBitmap(outputTensor)
    }

    private fun assetFilePath(context: Context, assetName: String): String {
        val file = File(context.filesDir, assetName)
        if (file.exists()) return file.absolutePath
        context.assets.open(assetName).use { inputStream ->
            FileOutputStream(file).use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file.absolutePath
    }
}