package com.mjdominiczak.songbook.resolvers

import android.content.Context
import androidx.annotation.StringRes

class ResourcesResolver(private val context: Context) {

    fun getString(@StringRes id: Int) = context.getString(id)

    fun getAsset(filename: String) = context.assets.open(filename)
}