package com.eulersbridge.isegoria.election.candidates.positions

import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.os.bundleOf
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.election.candidates.FRAGMENT_EXTRA_CANDIDATE_POSITION
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.onSuccess
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import java.lang.ref.WeakReference

internal class PositionAdapter(fragment: Fragment, private val api: API?) :
    LoadingAdapter<Position, PositionViewHolder>(1), PositionViewHolder.PositionItemListener {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.election_position_grid_item, parent, false)
        return PositionViewHolder(itemView, this)
    }

    override fun onClick(item: Position?) {
        if (item == null) return

        weakFragment.get()?.takeIf {
            it.activity != null && !it.isDetached && it.isAdded
        }?.let {
            val detailFragment = CandidatePositionFragment()
            detailFragment.arguments = bundleOf(FRAGMENT_EXTRA_CANDIDATE_POSITION to item)

            it.childFragmentManager
                .beginTransaction()
                .addToBackStack(null)
                .add(R.id.candidateFrame, detailFragment)
                .commit()
        }
    }

    override fun getPhoto(viewHolder: PositionViewHolder, itemId: Long) {
        if (api != null) {

            val weakViewHolder = WeakReference(viewHolder)

            api.getPhotos(itemId).onSuccess {
                it.photos?.firstOrNull()?.thumbnailUrl?.let { photoThumbnailUrl ->
                    weakViewHolder.get()?.apply {
                        setImageURL(photoThumbnailUrl, itemId)
                    }
                }
            }
        }
    }
}