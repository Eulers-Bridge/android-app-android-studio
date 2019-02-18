package com.eulersbridge.isegoria.election.candidates.positions


import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import com.bumptech.glide.request.RequestOptions
import com.eulersbridge.isegoria.GlideApp
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.data.Repository
import com.eulersbridge.isegoria.election.candidates.FRAGMENT_EXTRA_CANDIDATE_POSITION
import com.eulersbridge.isegoria.network.api.model.Position
import com.eulersbridge.isegoria.util.extension.runOnUiThread
import com.eulersbridge.isegoria.util.extension.subscribeSuccess
import com.eulersbridge.isegoria.util.transformation.TintTransformation
import dagger.android.support.AndroidSupportInjection
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import kotlinx.android.synthetic.main.election_positions_fragment.*
import javax.inject.Inject


/**
 * This fragment shows all the positions in the current election
 */
class CandidatePositionsFragment : Fragment(), PositionAdapter.PositionClickListener {

    @Inject
    internal lateinit var repository: Repository

    private val compositeDisposable = CompositeDisposable()

    override fun onAttach(context: Context?) {
        AndroidSupportInjection.inject(this)
        super.onAttach(context)
    }

    private val adapter: PositionAdapter by lazy {
        val glide = GlideApp.with(this).applyDefaultRequestOptions(
            RequestOptions()
                .placeholder(R.color.grey)
                .transform(TintTransformation())
        )

        PositionAdapter(glide, repository, this)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?  = inflater.inflate(R.layout.election_positions_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        positionsGridView.adapter = adapter

        repository.getLatestElectionPositions()
                .subscribeSuccess {
                    runOnUiThread { setPositions(it) }
                }
                .addTo(compositeDisposable)
    }

    override fun onPause() {
        compositeDisposable.dispose()
        super.onPause()
    }

    private fun setPositions(positions: List<Position>) {
        adapter.replaceItems(positions)
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
