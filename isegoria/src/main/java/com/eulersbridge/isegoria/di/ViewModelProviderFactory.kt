package com.eulersbridge.isegoria.di

import android.arch.lifecycle.ViewModel
import android.arch.lifecycle.ViewModelProvider


class ViewModelProviderFactory(private val viewModel: ViewModel) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(viewModel::class.java))
            return viewModel as T

        throw IllegalArgumentException("Unknown view model class name $modelClass")
    }
}