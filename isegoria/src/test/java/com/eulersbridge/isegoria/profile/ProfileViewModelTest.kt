package com.eulersbridge.isegoria.profile

import android.arch.core.executor.testing.InstantTaskExecutorRule
import com.eulersbridge.isegoria.data.Repository
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import io.reactivex.Completable
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class ProfileViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    private lateinit var repository: Repository
    private lateinit var profileViewModel: ProfileViewModel

    @Before
    fun setUp() {
        repository = mock {
            on { logOut() } doReturn Completable.complete()
        }

        profileViewModel = ProfileViewModel(repository)
    }

    @Test
    fun `view model calls Repository to log out`() {
        profileViewModel.logOut()
        verify(repository, times(1)).logOut()
    }

}