package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class FriendsFragmentModule {

    @Provides
    fun friendsViewModel(appRouter: AppRouter, repository: Repository): FriendsViewModel = FriendsViewModel(appRouter, repository)

    @Provides
    fun provideFriendsViewModel(friendsViewModel: FriendsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(friendsViewModel)

}