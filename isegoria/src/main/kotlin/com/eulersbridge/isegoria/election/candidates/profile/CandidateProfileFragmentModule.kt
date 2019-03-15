package com.eulersbridge.isegoria.election.candidates.profile

import android.arch.lifecycle.ViewModelProvider
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.inject.ViewModelProviderFactory
import dagger.Module
import dagger.Provides

@Module
class CandidateProfileFragmentModule {

    @Provides
    fun candidateProfileViewModel(repository: Repository): CandidateProfileViewModel = CandidateProfileViewModel(repository)

    @Provides
    fun provideCandidateProfileViewModel(candidateProfileViewModel: CandidateProfileViewModel): ViewModelProvider.Factory
            = ViewModelProviderFactory(candidateProfileViewModel)

}