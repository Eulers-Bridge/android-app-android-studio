package com.eulersbridge.isegoria.election

import com.eulersbridge.isegoria.election.candidates.CandidateAllFragment
import com.eulersbridge.isegoria.election.candidates.CandidateFragmentsModule
import com.eulersbridge.isegoria.election.candidates.CandidateTicketDetailFragment
import com.eulersbridge.isegoria.election.candidates.CandidateTicketFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionsFragment
import com.eulersbridge.isegoria.election.efficacy.EfficacyFragmentModule
import com.eulersbridge.isegoria.election.efficacy.SelfEfficacyQuestionsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ElectionFragmentProvider {

    @ContributesAndroidInjector(modules = [(EfficacyFragmentModule::class)])
    internal abstract fun provideSelfEfficacyQuestionsFragment(): SelfEfficacyQuestionsFragment

    @ContributesAndroidInjector(modules = [(ElectionFragmentModule::class)])
    internal abstract fun provideElectionMasterFragment(): ElectionMasterFragment

    @ContributesAndroidInjector(modules = [(ElectionFragmentModule::class)])
    internal abstract fun provideElectionOverviewFragment(): ElectionOverviewFragment

    @ContributesAndroidInjector(modules = [(CandidateFragmentsModule::class)])
    internal abstract fun provideCandidateAllFragment(): CandidateAllFragment

    @ContributesAndroidInjector(modules = [(CandidateFragmentsModule::class)])
    internal abstract fun provideCandidateTicketFragment(): CandidateTicketFragment

    @ContributesAndroidInjector(modules = [(CandidateFragmentsModule::class)])
    internal abstract fun provideCandidateTicketDetailFragment(): CandidateTicketDetailFragment

    @ContributesAndroidInjector(modules = [(CandidateFragmentsModule::class)])
    internal abstract fun provideCandidatePositionFragment(): CandidatePositionFragment

    @ContributesAndroidInjector(modules = [(CandidateFragmentsModule::class)])
    internal abstract fun provideCandidatePositionsFragment(): CandidatePositionsFragment

}