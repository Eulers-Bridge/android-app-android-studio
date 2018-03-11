package com.eulersbridge.isegoria.election.candidates.positions


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.os.bundleOf
import com.bumptech.glide.request.RequestOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.election.candidates.FRAGMENT_EXTRA_CANDIDATE_POSITION
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import kotlinx.android.synthetic.main.election_positions_fragment.*

class CandidatePositionsFragment : Fragment(), PositionAdapter.PositionClickListener {

    private var api: API? = null
    private val adapter: PositionAdapter by lazy {
        val glide = GlideApp.with(this).applyDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.color.grey)
                .transform(TintTransformation())
        )

        PositionAdapter(glide, api, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.election_positions_fragment, container, false)

        val app = activity?.application as IsegoriaApp?

        if (app != null)
            api = app.api

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        positionsGridView.adapter = adapter

        val app = activity?.application as IsegoriaApp?

        app?.loggedInUser?.value?.institutionId?.let { institutionId ->
            api?.getElections(institutionId)?.onSuccess { elections ->

                elections.firstOrNull()?.also {
                    api?.getElectionPositions(it.id)?.onSuccess { positions ->
                        setPositions(positions)
                    }
                }
            }
        }
    }

    private fun setPositions(positions: List<Position>) {
        adapter.apply {
            isLoading = false
            replaceItems(positions)
        }
    }

    override fun onClick(item: Position) {
        val detailFragment = CandidatePositionFragment()
        detailFragment.arguments = bundleOf(FRAGMENT_EXTRA_CANDIDATE_POSITION to item)

        childFragmentManager
            .beginTransaction()
            .addToBackStack(null)
            .add(R.id.electionCandidateFrame, detailFragment)
            .commit()
    }
}
