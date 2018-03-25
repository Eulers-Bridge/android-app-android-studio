package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.NetworkService
import dagger.Module
import dagger.Provides

@Module
class FriendsFragmentModule {

    @Provides
    fun friendsViewModel(
        app: IsegoriaApp,
        networkService: NetworkService
    ): FriendsViewModel = FriendsViewModel(app, networkService.api)

    @Provides
    fun provideFriendsViewModel(friendsViewModel: FriendsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(friendsViewModel)

}