package com.eulersbridge.isegoria.election.candidates

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentPagerAdapter
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.election.candidates.all.CandidateAllFragment
import com.eulersbridge.isegoria.election.candidates.positions.CandidatePositionsFragment
import com.eulersbridge.isegoria.election.candidates.ticket.CandidateTicketFragment
import kotlinx.android.synthetic.main.candidate_fragment.*

/**
 * This is the master fragment for candidates. Holds the view pager and tab indicator for the
 * positions, tickets and all candidates fragments.
 */
class CandidateViewPagerFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
        = inflater.inflate(R.layout.candidate_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val fragments = arrayOf(
            Fragment.instantiate(activity, CandidatePositionsFragment::class.java.name),
            Fragment.instantiate(activity, CandidateTicketFragment::class.java.name),
            Fragment.instantiate(activity, CandidateAllFragment::class.java.name)
        )

        viewPager.adapter = object : FragmentPagerAdapter(childFragmentManager) {
            override fun getItem(position: Int) = fragments[position]

            override fun getPageTitle(position: Int): CharSequence? {
                return when (position) {
                    0 -> "Position"
                    1 -> "Ticket"
                    2 -> "All"
                    else -> ""
                }
            }

            override fun getCount() = fragments.size
        }

        tabPageIndicator.apply {
            setViewPager(viewPager)
            setBackgroundResource(R.color.barBackground)
        }
    }
}