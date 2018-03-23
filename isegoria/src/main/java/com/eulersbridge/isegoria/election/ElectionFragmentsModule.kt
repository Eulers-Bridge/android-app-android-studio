package com.eulersbridge.isegoria.election

import com.eulersbridge.isegoria.election.candidates.CandidateAllFragment
import com.eulersbridge.isegoria.election.candidates.CandidateTicketDetailFragment
import com.eulersbridge.isegoria.election.candidates.CandidateTicketFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionsFragment
import com.eulersbridge.isegoria.election.efficacy.SelfEfficacyQuestionsFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ElectionFragmentsModule {

    @ContributesAndroidInjector
    abstract fun contributeSelfEfficacyQuestionsFragment(): SelfEfficacyQuestionsFragment

    @ContributesAndroidInjector
    abstract fun contributeElectionMasterFragment(): ElectionMasterFragment

    @ContributesAndroidInjector
    abstract fun contributeElectionOverviewFragment(): ElectionOverviewFragment

    @ContributesAndroidInjector
    abstract fun contributeCandidateAllFragment() : CandidateAllFragment

    @ContributesAndroidInjector
    abstract fun contributeCandidateTicketDetailFragment() : CandidateTicketDetailFragment

    @ContributesAndroidInjector
    abstract fun contributeCandidateTicketFragment() : CandidateTicketFragment

    @ContributesAndroidInjector
    abstract fun contributeCandidatePositionsFragment() : CandidatePositionsFragment

}