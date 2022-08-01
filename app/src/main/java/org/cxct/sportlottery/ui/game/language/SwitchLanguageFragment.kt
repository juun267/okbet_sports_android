package org.cxct.sportlottery.ui.game.language

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentSwitchLanguageBinding
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseSocketFragment
import org.cxct.sportlottery.ui.game.GameActivity
import org.cxct.sportlottery.ui.game.GameViewModel
import org.cxct.sportlottery.ui.game.publicity.GamePublicityActivity
import org.cxct.sportlottery.util.*


class SwitchLanguageFragment : BaseSocketFragment<GameViewModel>(GameViewModel::class),View.OnClickListener{

    private lateinit var binding: FragmentSwitchLanguageBinding

    override fun onClick(v: View?) {
        when (v) {
            binding.ivBack -> {
                (activity as GameActivity).onBackPressed()
            }
            binding.llChina -> {
                selectLanguage(LanguageManager.Language.ZH)
            }
            binding.llEnglish -> {
                selectLanguage(LanguageManager.Language.EN)
            }
            binding.llVietnam -> {
                selectLanguage(LanguageManager.Language.VI)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSwitchLanguageBinding.inflate(inflater, container, false)
        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.background = ColorDrawable(Color.TRANSPARENT);
        initView()
    }

    private fun initView(){
        binding.ivBack.setOnClickListener(this)
        binding.llEnglish.setOnClickListener(this)
        binding.llChina.setOnClickListener(this)
        binding.llVietnam.setOnClickListener(this)
        when (LanguageManager.getSelectLanguage(context)) {
            LanguageManager.Language.ZH -> {
                binding.tvChina.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.EN -> {
                binding.tvEnglish.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
            LanguageManager.Language.VI -> {
                binding.tvVietnam.setTextColor(ContextCompat.getColor(requireContext(), R.color.color_317FFF_0760D4))
            }
        }
        if(sConfigData?.supportLanguage!!.contains("zh")){
            binding.llChina.visibility = View.VISIBLE
        }
        if(sConfigData?.supportLanguage!!.contains("vi")){
            binding.llVietnam.visibility = View.VISIBLE
        }
        if(sConfigData?.supportLanguage!!.contains("en")){
            binding.llEnglish.visibility = View.VISIBLE
        }
    }

    private fun selectLanguage(select: LanguageManager.Language) {
        if(SPUtil.getInstance(context).getSelectLanguage() != select.key){
            activity?.run {
                LanguageManager.saveSelectLanguage(this, select)
//                if (sConfigData?.thirdOpen == FLAG_OPEN)
//                    MainActivity.reStart(this)
//                else
                GamePublicityActivity.reStart(this)
            }
        }
    }

}
