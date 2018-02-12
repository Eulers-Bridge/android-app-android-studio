package com.eulersbridge.isegoria.election.candidates.positions


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.onSuccess
import kotlinx.android.synthetic.main.election_positions_fragment.*

class CandidatePositionsFragment : Fragment() {

    private var api: API? = null
    private val adapter: PositionAdapter by lazy {
        PositionAdapter(this, api)
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
}
