package com.yuni.magangdiskominfoapp.utils

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.signature.ObjectKey
import com.yuni.magangdiskominfoapp.R
import com.yuni.magangdiskominfoapp.api.ApiClient

object PhotoProfileManager {
    private const val PREF_NAME = "PhotoProfilePrefs"
    private const val KEY_PHOTO_PATH = "profile_photo_path"
    private const val KEY_PHOTO_TIMESTAMP = "profile_photo_timestamp"

    fun saveProfilePhotoPath(context: Context, photoPath: String?) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PHOTO_PATH, photoPath)
            .putLong(KEY_PHOTO_TIMESTAMP, System.currentTimeMillis())
            .apply()
    }

    fun getProfilePhotoPath(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PHOTO_PATH, null)
    }

    fun clearProfilePhotoCache(context: Context) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .clear()
            .apply()

        // Clear Glide cache
        Glide.get(context).clearMemory()
        Thread {
            Glide.get(context).clearDiskCache()
        }.start()
    }

    fun loadProfilePhoto(context: Context, imageView: ImageView) {
        val photoPath = getProfilePhotoPath(context)
        val timestamp = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getLong(KEY_PHOTO_TIMESTAMP, 0)

        if (!photoPath.isNullOrEmpty()) {
            Glide.with(context)
                .load("${ApiClient.BASE_URL}storage/$photoPath")
                .diskCacheStrategy(DiskCacheStrategy.NONE) // Disable caching
                .skipMemoryCache(true) // Skip memory cache
                .signature(ObjectKey(timestamp)) // Use timestamp as signature
                .placeholder(R.drawable.ic_person)
                .error(R.drawable.ic_person)
                .into(imageView)
        } else {
            Glide.with(context)
                .load(R.drawable.ic_person)
                .into(imageView)
        }
    }
}