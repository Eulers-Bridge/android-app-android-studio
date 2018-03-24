package com.eulersbridge.isegoria.personality

import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.UserPersonality
import com.eulersbridge.isegoria.observe
import kotlinx.android.synthetic.main.personality_screen2_fragment.*



class PersonalityQuestionsFragment : Fragment() {

    private lateinit var viewModel: PersonalityViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
            = inflater.inflate(R.layout.personality_screen2_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        val sliderBars = arrayOf(
            personalitySliderBar1, personalitySliderBar2, personalitySliderBar3,
            personalitySliderBar4, personalitySliderBar5, personalitySliderBar6,
            personalitySliderBar7, personalitySliderBar8, personalitySliderBar9,
            personalitySliderBar10
        )

        doneButton.setOnClickListener {
            val extroversion =
                ((sliderBars[0].score + (8 - sliderBars[5].score)) / 2).toFloat()
            val agreeableness =
                ((sliderBars[6].score + (8 - sliderBars[1].score)) / 2).toFloat()
            val conscientiousness =
                ((sliderBars[2].score + (8 - sliderBars[7].score)) / 2).toFloat()
            val emotionalStability =
                ((sliderBars[8].score + (8 - sliderBars[3].score)) / 2).toFloat()
            val opennessToExperiences =
                ((sliderBars[4].score + (10 - sliderBars[5].score)) / 2).toFloat()

            val personality = UserPersonality(
                agreeableness, conscientiousness,
                emotionalStability, extroversion, opennessToExperiences
            )

            observe(viewModel.setUserCompletedQuestions(personality)) { success ->
                if (success == false)
                    doneButton.post { doneButton.isEnabled = true }
            }
        }
    }

    fun setViewModel(viewModel: PersonalityViewModel) {
        this.viewModel = viewModel
    }
}