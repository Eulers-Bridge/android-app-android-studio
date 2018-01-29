package com.eulersbridge.isegoria.personality

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.eulersbridge.isegoria.R
import com.eulersbridge.isegoria.network.api.models.UserPersonality
import kotlinx.android.synthetic.main.personality_screen2_fragment.*

class PersonalityQuestionsFragment : Fragment() {

    private val viewModel: PersonalityViewModel by lazy {
        ViewModelProviders.of(activity!!).get(PersonalityViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?
            = inflater.inflate(R.layout.personality_screen2_fragment, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        doneButton.setOnClickListener {
            val extroversion =
                ((sliderBar1.score + (8 - sliderBar6.score)) / 2).toFloat()
            val agreeableness =
                ((sliderBar7.score + (8 - sliderBar2.score)) / 2).toFloat()
            val conscientiousness =
                ((sliderBar3.score + (8 - sliderBar8.score)) / 2).toFloat()
            val emotionalStability =
                ((sliderBar9.score + (8 - sliderBar4.score)) / 2).toFloat()
            val opennessToExperiences =
                ((sliderBar5.score + (10 - sliderBar6.score)) / 2).toFloat()

            val personality = UserPersonality(
                agreeableness, conscientiousness,
                emotionalStability, extroversion, opennessToExperiences
            )

            viewModel.setUserCompletedQuestions(personality).observe(this, Observer { success ->
                if (success == false) {
                    doneButton.post { doneButton.isEnabled = true }
                }
            })
        }
    }

    private var sliderBar1: PersonalitySliderBar = personalitySliderBar1 as PersonalitySliderBar
    private var sliderBar2: PersonalitySliderBar = personalitySliderBar2 as PersonalitySliderBar
    private var sliderBar3: PersonalitySliderBar = personalitySliderBar3 as PersonalitySliderBar
    private var sliderBar4: PersonalitySliderBar = personalitySliderBar4 as PersonalitySliderBar
    private var sliderBar5: PersonalitySliderBar = personalitySliderBar5 as PersonalitySliderBar
    private var sliderBar6: PersonalitySliderBar = personalitySliderBar6 as PersonalitySliderBar
    private var sliderBar7: PersonalitySliderBar = personalitySliderBar7 as PersonalitySliderBar
    private var sliderBar8: PersonalitySliderBar = personalitySliderBar8 as PersonalitySliderBar
    private var sliderBar9: PersonalitySliderBar = personalitySliderBar9 as PersonalitySliderBar
}