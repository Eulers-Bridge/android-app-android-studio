package com.eulersbridge.isegoria.profile

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Contact
import com.eulersbridge.isegoria.network.api.model.Photo
import com.eulersbridge.isegoria.network.api.model.Task
import com.eulersbridge.isegoria.util.data.Optional
import com.nhaarman.mockitokotlin2.*
import io.reactivex.Completable
import io.reactivex.Single
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var appRouter: AppRouter

    private lateinit var profileViewModel: ProfileViewModel

    private val dummyTasks = listOf(Task(1, "A", 2), Task(3, "B", 4))
    private val dummyContact = Contact(
            "Female",
            null,
            "executive@cis-gres.org",
            "Jane",
            "Doe",
            26,
            4,
            4600,
            0,
            0,
            "",
            0,
            null,
            null
    )

    @Before
    fun setUp() {
        repository = mock()
        appRouter = mock()
        profileViewModel = ProfileViewModel(repository, appRouter)
    }

    @Test
    fun `view model should not allow friends to be shown for other users`() {
        given(repository.getInstitutionName(any())).willReturn(Single.just(Optional("CIS-GReS")))
        given(repository.getContact(dummyContact.email)).willReturn(Single.just(Optional(dummyContact)))
        given(repository.getUserPhoto()).willReturn(Single.just(Optional<Photo>(null)))
        given(repository.getTasks()).willReturn(Single.just(emptyList()))

        profileViewModel.setUser(dummyContact)
        profileViewModel.viewFriends()

        then(appRouter).shouldHaveZeroInteractions()
    }

    @Test
    fun `view model calculates total xp from tasks`() {
        given(repository.getRemainingTasks()).willReturn(Single.just(dummyTasks))

        val totalXpObserver = mock<Observer<Long>>()
        profileViewModel.totalXp.observeForever(totalXpObserver)

        val tasksObserver = mock<Observer<List<Task>?>>()
        profileViewModel.getRemainingTasks()!!.observeForever(tasksObserver)

        then(totalXpObserver).should(times(1)).onChanged(6)
        then(totalXpObserver).shouldHaveNoMoreInteractions()
    }

    @Test
    fun `view model calls Repository to log out`() {
        given(repository.logOut()).willReturn(Completable.complete())

        profileViewModel.logOut()

        then(repository).should(times(1)).logOut()
        then(repository).shouldHaveNoMoreInteractions()
    }

}