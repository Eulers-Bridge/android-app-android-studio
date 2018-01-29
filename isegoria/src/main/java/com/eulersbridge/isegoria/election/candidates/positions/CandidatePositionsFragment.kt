package com.eulersbridge.isegoria.election.candidates.positions


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.IsegoriaApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Election
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.util.network.SimpleCallback
import kotlinx.android.synthetic.main.election_positions_fragment.*
import retrofit2.Response

class CandidatePositionsFragment : Fragment() {

    private var api: API? = null
    private lateinit var adapter: PositionAdapter

    private val electionsCallback = object : SimpleCallback<List<Election>>() {
        override fun handleResponse(response: Response<List<Election>>) {
            val elections = response.body()
            if (elections != null && elections.isNotEmpty()) {
                val election = elections[0]

                api!!.getElectionPositions(election.id).enqueue(positionsCallback)
            }
        }
    }

    private val positionsCallback = object : SimpleCallback<List<Position>>() {
        override fun handleResponse(response: Response<List<Position>>) {
            val positions = response.body()

            if (positions != null)
                setPositions(positions)
        }
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

        adapter = PositionAdapter(this, api)

        return rootView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        positionsGridView.adapter = adapter

        val app = activity?.application as IsegoriaApp?

        app?.loggedInUser?.value?.institutionId?.let { institutionId ->
            api?.getElections(institutionId)?.enqueue(electionsCallback)
        }
    }

    private fun setPositions(positions: List<Position>) {
        adapter.isLoading = false
        adapter.replaceItems(positions)
    }
}
