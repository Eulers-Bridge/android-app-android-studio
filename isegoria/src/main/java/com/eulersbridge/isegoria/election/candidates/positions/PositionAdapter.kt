package com.eulersbridge.isegoria.election.candidates.positions

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.ViewGroup
import com.eulersbridge.isegoria.FRAGMENT_EXTRA_CANDIDATE_POSITION
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.API
import com.eulersbridge.isegoria.network.api.models.Position
import com.eulersbridge.isegoria.network.api.responses.PhotosResponse
import com.eulersbridge.isegoria.util.network.SimpleCallback
import com.eulersbridge.isegoria.util.ui.LoadingAdapter
import retrofit2.Response
import java.lang.ref.WeakReference

internal class PositionAdapter(fragment: Fragment, private val api: API?) :
    LoadingAdapter<Position, PositionViewHolder>(1), PositionViewHolder.PositionItemListener {

    private val weakFragment: WeakReference<Fragment> = WeakReference(fragment)

    private fun isValidFragment(fragment: Fragment?): Boolean {
        return (fragment != null
                && fragment.activity != null
                && !fragment.isDetached
                && fragment.isAdded)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PositionViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.election_position_grid_item, parent, false)
        return PositionViewHolder(itemView, this)
    }

    override fun onClick(item: Position?) {
        if (item == null) return

        val fragment = weakFragment.get()
        if (!isValidFragment(fragment)) return

        val arguments = Bundle()
        arguments.putParcelable(FRAGMENT_EXTRA_CANDIDATE_POSITION, item)

        val detailFragment = CandidatePositionFragment()
        detailFragment.arguments = arguments

        fragment?.childFragmentManager
            ?.beginTransaction()
            ?.addToBackStack(null)
            ?.add(R.id.candidateFrame, detailFragment)
            ?.commit()
    }

    override fun getPhoto(viewHolder: PositionViewHolder, itemId: Long) {
        if (api != null) {

            val weakViewHolder = WeakReference(viewHolder)

            api.getPhotos(itemId).enqueue(object : SimpleCallback<PhotosResponse>() {
                override fun handleResponse(response: Response<PhotosResponse>) {
                    val body = response.body()

                    if (body != null && body.totalPhotos > 0 && weakViewHolder.get() != null) {
                        weakViewHolder.get()!!.setImageURL(body.photos!![0].thumbnailUrl, itemId)
                    }
                }
            })
        }
    }
}