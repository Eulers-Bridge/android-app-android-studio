package com.eulersbridge.isegoria.di

import com.eulersbridge.isegoria.MainActivity
import com.eulersbridge.isegoria.auth.AuthActivity
import com.eulersbridge.isegoria.auth.login.LoginFragmentProvider
import com.eulersbridge.isegoria.auth.signup.SignUpFragmentProvider
import com.eulersbridge.isegoria.auth.verification.EmailVerificationFragmentProvider
import com.eulersbridge.isegoria.election.ElectionFragmentProvider
import com.eulersbridge.isegoria.feed.FeedFragmentProvider
import com.eulersbridge.isegoria.feed.events.detail.EventDetailActivity
import com.eulersbridge.isegoria.feed.events.detail.EventDetailModule
import com.eulersbridge.isegoria.feed.news.detail.NewsDetailActivity
import com.eulersbridge.isegoria.feed.news.detail.NewsDetailModule
import com.eulersbridge.isegoria.feed.photos.detail.PhotoDetailActivity
import com.eulersbridge.isegoria.feed.photos.detail.PhotoDetailModule
import com.eulersbridge.isegoria.friends.FriendsFragmentProvider
import com.eulersbridge.isegoria.personality.PersonalityActivity
import com.eulersbridge.isegoria.personality.PersonalityActivityModule
import com.eulersbridge.isegoria.poll.PollVoteFragmentProvider
import com.eulersbridge.isegoria.poll.PollsFragmentProvider
import com.eulersbridge.isegoria.profile.ProfileFragmentProvider
import com.eulersbridge.isegoria.profile.settings.SettingsActivity
import com.eulersbridge.isegoria.profile.settings.SettingsActivityModule
import com.eulersbridge.isegoria.vote.VoteFragmentProvider
import dagger.Module
import dagger.android.ContributesAndroidInjector


@Module
abstract class ActivityBuilder {

    @ContributesAndroidInjector(modules = [(SignUpFragmentProvider::class),
        (EmailVerificationFragmentProvider::class),(LoginFragmentProvider::class)])
    internal abstract fun bindAuthActivity(): AuthActivity

    @ContributesAndroidInjector(modules = [(PersonalityActivityModule::class)])
    internal abstract fun bindPersonalityActivity(): PersonalityActivity

    @ContributesAndroidInjector(modules = [(FeedFragmentProvider::class),
        (ElectionFragmentProvider::class), (PollsFragmentProvider::class),
        (PollVoteFragmentProvider::class), (VoteFragmentProvider::class),
        (ProfileFragmentProvider::class), (FriendsFragmentProvider::class)])
    internal abstract fun bindMainActivity(): MainActivity

    @ContributesAndroidInjector(modules = [(NewsDetailModule::class)])
    internal abstract fun bindNewsDetailActivity(): NewsDetailActivity

    @ContributesAndroidInjector(modules = [(PhotoDetailModule::class)])
    internal abstract fun bindPhotoDetailActivity(): PhotoDetailActivity

    @ContributesAndroidInjector(modules = [(EventDetailModule::class)])
    internal abstract fun bindEventDetailActivity(): EventDetailActivity

    @ContributesAndroidInjector(modules = [(SettingsActivityModule::class)])
    internal abstract fun bindSettingsActivity(): SettingsActivity

}