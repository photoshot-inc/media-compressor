package devs.core.utils

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.ImageView
import androidx.annotation.DrawableRes
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import devs.core.R
import kotlin.math.sqrt


fun Number.int(): Int {
    return this.toInt()
}

fun ImageView.loadImageWithGlide(
    location: String?,
    placeholder: Int = R.drawable.progress_animation,
    cacheStrategy: Boolean = true
) {
    //Log.d("TAG", "loadImageWithGlide: $location")
    Glide.with(this)
        .load(location)
        .placeholder(placeholder)
        .diskCacheStrategy(if (cacheStrategy) DiskCacheStrategy.AUTOMATIC else DiskCacheStrategy.NONE)
        .skipMemoryCache(cacheStrategy.not())
        .into(this)
}

fun Context.getBitmap(location: String, width: Int, height: Int): Bitmap? {
    val bit: Bitmap = try {
        return if (location.startsWith("file:///android_asset/")) {
            val asset = location.replace("file:///android_asset/", "")
            BitmapFactory.decodeStream(assets.open(asset))
        } else {
            BitmapFactory.decodeFile(location)
        }
    } catch (e: Exception) {
        Log.e("TAG", "getAssetBitmap: $e")
        null
    } ?: return null

    val bit2 = Bitmap.createScaledBitmap(bit, width, height, false)
    bit.recycle()
    return bit2

}

fun Context.getBitmap(location: String): Bitmap? {
    return try {
        return if (location.startsWith("file:///android_asset/")) {
            val asset = location.replace("file:///android_asset/", "")
            BitmapFactory.decodeStream(assets.open(asset))
        } else {
            BitmapFactory.decodeFile(location)
        }
    } catch (e: Exception) {
        Log.e("TAG", "getAssetBitmap: $e")
        null
    }
}

fun String.loadTypeFace(context: Context): Typeface? {
    try {
        if (this.startsWith("file:///android_asset/")) {
            return Typeface.createFromAsset(
                context.assets,
                this.replace("file:///android_asset/", "")
            )
        }
        return Typeface.createFromFile(this)
    } catch (ex: java.lang.Exception) {
        return null
    }
}

fun Bundle.withArg(key: String, block: (data: Any) -> Unit) {
    if (this.get(key) != null)
        return block.invoke(this.get(key)!!)
}

fun safeRun(block: () -> Unit) = try {
    block.invoke()
    Log.w("SAFE_RUN", "safeRun: success")
} catch (ex: Exception) {
    Log.e("SAFE_RUN", "safeRun: ", ex)
}

fun Activity.withStoragePermission(block: () -> Unit) {
    when {
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> block()
        else -> {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                safeRun {
                    AlertDialog.Builder(this)
                        .setTitle("Info")
                        .setMessage("We need storage permission to select photos")
                        .setPositiveButton("ok") { p0, p1 ->
                            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                            val uri: Uri = Uri.fromParts("package", application.packageName, null)
                            intent.data = uri
                            this.startActivity(intent)
                        }
                        .setNegativeButton("cancel") { p0, p1 ->

                        }
                        .create()
                        .show()

                }
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ),
                    120
                )
            }
        }
    }

}

fun ImageView.load(url: String, @DrawableRes placeholder: Int? = null) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholder ?: R.drawable.glide_placeholder)
        //.transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun ImageView.load(url: Uri, @DrawableRes placeholder: Int? = null) {
    Glide.with(this)
        .load(url)
        .placeholder(placeholder ?: R.drawable.glide_placeholder)
        //.transition(DrawableTransitionOptions.withCrossFade())
        .into(this)
}

fun Fragment.withStoragePermission(block: () -> Unit) {
    activity?.withStoragePermission(block)
}

object MathExt {
    fun calculateDistance(x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)))
        // return sqrt((x1 - x2.toDouble()).pow(2.0) + (y1 - y2.toDouble()).pow(2.0))
    }

    fun PointF.calculateDistance(x2: Float, y2: Float): Float =
        calculateDistance(this.x, this.y, x2, y2)

}