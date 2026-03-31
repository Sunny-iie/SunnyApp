package com.example.sunny.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.net.Uri
import android.os.ParcelFileDescriptor

object PdfUtil {
    fun renderPdfToBitmaps(context: Context, uriString: String): List<Bitmap> {
        val bitmaps = mutableListOf<Bitmap>()
        try {
            val uri = Uri.parse(uriString)
            val fileDescriptor: ParcelFileDescriptor? =
                context.contentResolver.openFileDescriptor(uri, "r")

            fileDescriptor?.let {
                val renderer = PdfRenderer(it)
                val pageCount = renderer.pageCount

                // 为了性能和内存，我们默认最多渲染前 5 页（体检报告通常没那么长）
                val maxPages = minOf(pageCount, 5)

                for (i in 0 until maxPages) {
                    val page = renderer.openPage(i)
                    // 按照屏幕宽度比例创建高质量 Bitmap
                    val bitmap = Bitmap.createBitmap(
                        page.width * 2, // 2倍清晰度
                        page.height * 2,
                        Bitmap.Config.ARGB_8888
                    )
                    page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                    bitmaps.add(bitmap)
                    page.close()
                }
                renderer.close()
                it.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmaps
    }
}