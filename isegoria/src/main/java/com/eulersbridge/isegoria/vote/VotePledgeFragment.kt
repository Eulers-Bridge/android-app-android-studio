package com.eulersbridge.isegoria.vote

import android.content.Context
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.util.ui.TitledFragment
import kotlinx.android.synthetic.main.vote_fragment_pledge.*

class VotePledgeFragment : Fragment(), TitledFragment {

    private lateinit var viewModel: VoteViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View?
            = inflater.inflate(R.layout.vote_fragment_pledge, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        votePledgeNextButton.setOnClickListener { val data = viewModel.setPledgeComplete() }
    }

    fun setViewModel(viewModel: VoteViewModel) {
        this.viewModel = viewModel
    }

    override fun getTitle(context: Context?) = context?.getString(R.string.vote_tab_2)
}