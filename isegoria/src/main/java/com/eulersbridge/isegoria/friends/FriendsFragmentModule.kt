package com.eulersbridge.isegoria.friends

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import com.eulersbridge.isegoria.network.api.API
import dagger.Module
import dagger.Provides

@Module
class FriendsFragmentModule {

    @Provides
    fun friendsViewModel(
        app: IsegoriaApp,
        api: API
    ): FriendsViewModel = FriendsViewModel(app, api)

    @Provides
    fun provideFriendsViewModel(friendsViewModel: FriendsViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(friendsViewModel)

}