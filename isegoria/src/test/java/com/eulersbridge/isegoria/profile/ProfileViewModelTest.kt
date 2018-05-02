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
        repository = mock {
            on { getInstitutionName(any()) } doReturn Single.just(Optional("CIS-GReS"))
            on { getContact(dummyContact.email) } doReturn Single.just(Optional(dummyContact))
            on { getUserPhoto() } doReturn Single.just(Optional<Photo>(null))
            on { getTasks() } doReturn Single.just(emptyList())
            on { getRemainingTasks() } doReturn Single.just(dummyTasks)
            on { logOut() } doReturn Completable.complete()
        }

        appRouter = mock()

        profileViewModel = ProfileViewModel(repository, appRouter)
    }

    @Test
    fun `view model should not allow friends to be shown for other users`() {
        profileViewModel.setUser(dummyContact)
        profileViewModel.viewFriends()
        verifyZeroInteractions(appRouter)
    }

    @Test
    fun `view model calculates total xp from tasks`() {
        val totalXpObserver = mock<Observer<Long>>()
        profileViewModel.totalXp.observeForever(totalXpObserver)

        val tasksObserver = mock<Observer<List<Task>?>>()
        profileViewModel.getRemainingTasks()!!.observeForever(tasksObserver)

        verify(totalXpObserver, times(1)).onChanged(6)
        verifyNoMoreInteractions(totalXpObserver)
    }

    @Test
    fun `view model calls Repository to log out`() {
        profileViewModel.logOut()
        verify(repository, times(1)).logOut()
    }

}