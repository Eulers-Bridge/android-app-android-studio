package com.eulersbridge.isegoria.election.candidates.profile

import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class CandidateProfileFragmentProvider {

    @ContributesAndroidInjector(modules = [(CandidateProfileFragmentModule::class)])
    internal abstract fun candidateProfileFragmentModule(): CandidateProfileFragment

}