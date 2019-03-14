package com.eulersbridge.isegoria.election

import com.eulersbridge.isegoria.election.candidates.all.CandidateAllFragment
import com.eulersbridge.isegoria.election.candidates.ticket.CandidateTicketDetailFragment
import com.eulersbridge.isegoria.election.candidates.ticket.CandidateTicketFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionsFragment
import com.eulersbridge.isegoria.election.efficacy.EfficacyFragmentModule
import com.eulersbridge.isegoria.election.efficacy.SelfEfficacyQuestionsFragment
import com.eulersbridge.isegoria.election.overview.ElectionOverviewFragment
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

    @ContributesAndroidInjector
    internal abstract fun provideCandidateAllFragment(): CandidateAllFragment

    @ContributesAndroidInjector
    internal abstract fun provideCandidateTicketFragment(): CandidateTicketFragment

    @ContributesAndroidInjector
    internal abstract fun provideCandidateTicketDetailFragment(): CandidateTicketDetailFragment

    @ContributesAndroidInjector
    internal abstract fun provideCandidatePositionFragment(): CandidatePositionFragment

    @ContributesAndroidInjector
    internal abstract fun provideCandidatePositionsFragment(): CandidatePositionsFragment

}