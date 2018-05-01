package com.eulersbridge.isegoria.profile

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.Observer
import com.eulersbridge.isegoria.AppRouter
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.network.api.model.Task
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

    @Before
    fun setUp() {
        repository = mock {
            on { getRemainingTasks() } doReturn Single.just(dummyTasks)
            on { logOut() } doReturn Completable.complete()
        }

        appRouter = mock()

        profileViewModel = ProfileViewModel(repository, appRouter)
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