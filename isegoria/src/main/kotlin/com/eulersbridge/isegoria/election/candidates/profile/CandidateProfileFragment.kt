package com.eulersbridge.isegoria.election.candidates.profile

import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.view.*
import android.widget.Toast
import com.bumptech.glide.Priority
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_CANDIDATE_ID
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.extension.observe
import com.eulersbridge.isegoria.util.transformation.BlurTransformation
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import com.eulersbridge.isegoria.util.ui.TitledFragment
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.candidate_profile_fragment.*
import javax.inject.Inject

class CandidateProfileFragment : Fragment(), TitledFragment {

    @Inject
    internal lateinit var modelFactory: ViewModelProvider.Factory
    private lateinit var viewModel: CandidateProfileViewModel

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
        viewModel = ViewModelProviders.of(this, modelFactory)[CandidateProfileViewModel::class.java]
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View {
        var view = inflater.inflate(R.layout.candidate_profile_fragment, container, false)

        super.onActivityCreated(savedInstanceState)

        viewModel.setCandidateId(arguments?.getLong(FRAGMENT_EXTRA_CANDIDATE_ID))

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //Sets up the observables that will set the components
        observe(viewModel.candidateName) { nameTextView.text = it }

        observe(viewModel.candidateDescription) { descriptionTextView.text = it }

        observe(viewModel.candidateInstitutionName) { institutionTextView.text = it }

        observe(viewModel.candidateProfilePhotoUrl) {

            GlideApp.with(this)
                    .load(it)
                    .transform(CircleCrop())
                    .priority(Priority.HIGH)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(smallProfilePhotoImageView)

            GlideApp.with(this)
                    .load(it)
                    .transforms(BlurTransformation(context!!), TintTransformation(0.1))
                    .priority(Priority.HIGH)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(backgroundProfileIPhotoImageView)
        }

        observe(viewModel.candidateLikedByUser) {
            if (it != null && it) {
                likeButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.gold))
                likeButton.setOnClickListener {
                    viewModel.onCandidateUnliked()
                }
            } else {
                likeButton.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context!!, R.color.grey))

                likeButton.setOnClickListener {
                    viewModel.onCandidateLiked()
                }
            }
        }

        observe(viewModel.candidateAddedAsFriend) {
            // refreshes the menu options
            activity?.invalidateOptionsMenu()
        }

        observe(viewModel.toastMessage) { showMessage(it ?: "") }

        // setup menu options
        activity?.invalidateOptionsMenu()
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu?, inflater: MenuInflater?) {
        inflater?.inflate(R.menu.candidate_profile, menu)
        super.onCreateOptionsMenu(menu, inflater)

        val addFriendItem = menu?.findItem(R.id.candidate_profile_add_friend)
        val removeFriendItem = menu?.findItem(R.id.candidate_profile_remove_friend)

        when (viewModel.candidateAddedAsFriend.value) {
            true -> {
                addFriendItem?.isVisible = false
                removeFriendItem?.isVisible = true
            }
            false -> {
                addFriendItem?.isVisible = true
                removeFriendItem?.isVisible = false
            }
            null -> {
                addFriendItem?.isVisible = false
                removeFriendItem?.isVisible = false
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.candidate_profile_add_friend -> {
                viewModel.onAddCandidateAsFriend()
                true
            }
            R.id.candidate_profile_remove_friend -> {
                viewModel.onRemoveCandidateAsFriend()
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun getTitle(context: Context?) = getString(R.string.section_title_candidate_profile)


    private fun showMessage(message: String)
            = Toast.makeText(activity, message, Toast.LENGTH_LONG).show()
}
