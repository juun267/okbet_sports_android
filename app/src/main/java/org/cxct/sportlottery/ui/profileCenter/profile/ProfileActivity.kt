package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_modify_profile_info.*
import kotlinx.android.synthetic.main.activity_profile.*
import kotlinx.android.synthetic.main.activity_profile.btn_back
import kotlinx.android.synthetic.main.activity_profile.ll_e_mail
import kotlinx.android.synthetic.main.activity_profile.ll_phone_number
import kotlinx.android.synthetic.main.activity_profile.ll_qq_number
import kotlinx.android.synthetic.main.activity_profile.ll_wechat
import org.cxct.sportlottery.R
import org.cxct.sportlottery.db.entity.UserInfo
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.FLAG_NICKNAME_IS_SET
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.ui.base.BaseOddButtonActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyProfileInfoActivity.Companion.MODIFY_INFO
import org.cxct.sportlottery.ui.profileCenter.nickname.ModifyType
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class ProfileActivity : BaseOddButtonActivity<ProfileModel>(ProfileModel::class) {

    companion object {
        private val ENABLE_SWITCH = "1"
        private val UNABLE_SWITCH = "0"
    }

    private val mSelectMediaListener = object : OnResultCallbackListener<LocalMedia> {
        override fun onResult(result: MutableList<LocalMedia>?) {
            try {
                // 图片选择结果回调
                // LocalMedia 里面返回三种path
                // 1.media.getPath(); 为原图path
                // 2.media.getCutPath();为裁剪后path，需判断media.isCut();是否为true
                // 3.media.getCompressPath();为压缩后path，需判断media.isCompressed();是否为true
                // 如果裁剪并压缩了，已取压缩路径为准，因为是先裁剪后压缩的

                val media = result?.firstOrNull() //這裡應當只會有一張圖片
                val path = when {
                    media?.isCompressed == true -> media.compressPath
                    media?.isCut == true -> media.cutPath
                    else -> media?.path
                }

                val file = File(path!!)
                if (file.exists())
                    uploadImg(file)
                else
                    throw FileNotFoundException()
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.showToastInCenter(this@ProfileActivity, getString(R.string.error_reading_file))
            }
        }

        override fun onCancel() {
            Timber.i("PictureSelector Cancel")
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        initView()
        initButton()
        initObserve()
    }

    private fun initView() {
        sConfigData?.apply {
            ll_qq_number.visibility = if (enableWithdrawQQ == ENABLE_SWITCH) View.VISIBLE else View.GONE
            ll_e_mail.visibility = if (enableWithdrawEmail == ENABLE_SWITCH) View.VISIBLE else View.GONE
            ll_phone_number.visibility = if (enableWithdrawPhone == ENABLE_SWITCH) View.VISIBLE else View.GONE
            ll_wechat.visibility = if (enableWithdrawWechat == ENABLE_SWITCH) View.VISIBLE else View.GONE
        }

    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        //設定個人資訊頁面
        setupToInfoSettingPage()

        btn_head.setOnClickListener {
            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
        }
    }

    private fun setupToInfoSettingPage() {
        //暱稱
        btn_nickname.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.NickName) }
        //密碼設置
        btn_pwd_setting.setOnClickListener { startActivity(Intent(this@ProfileActivity, SettingPasswordActivity::class.java)) }
        //QQ號碼
        ll_qq_number.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.QQNumber) }
        //郵箱
        ll_e_mail.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.Email) }
        //手機號碼
        ll_phone_number.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.PhoneNumber) }
        //微信
        ll_wechat.setOnClickListener { putExtraForProfileInfoActivity(ModifyType.WeChat) }
    }

    private fun putExtraForProfileInfoActivity(modifyType: ModifyType) {
        startActivity(Intent(this@ProfileActivity, ModifyProfileInfoActivity::class.java).apply { putExtra(MODIFY_INFO, modifyType) })
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(iv_head.context)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像
    }

    private fun uploadImg(file: File) {
        val userId = viewModel.userInfo.value?.userId.toString()
        val uploadImgRequest = UploadImgRequest(userId, file)
        viewModel.uploadImage(uploadImgRequest)
    }

    private fun initObserve() {
        viewModel.editIconUrlResult.observe(this, Observer {
            if (it?.success == true)
                ToastUtil.showToastInCenter(this, getString(R.string.save_avatar_success))
            else
                ToastUtil.showToastInCenter(this, it?.msg)
        })

        viewModel.userInfo.observe(this, Observer {
            updateAvatar(it?.iconUrl)
            tv_nickname.text = it?.nickName
            tv_member_account.text = it?.userName
            tv_id.text = it?.userId?.toString()
            tv_real_name.text = it?.fullName

            if (it?.setted == FLAG_NICKNAME_IS_SET) {
                btn_nickname.isEnabled = false
                icon_arrow_nickname.visibility = View.GONE
            } else {
                btn_nickname.isEnabled = true
                icon_arrow_nickname.visibility = View.VISIBLE
            }

            it?.let { setWithdrawInfo(it) }
        })
    }

    private fun setWithdrawInfo(userInfo: UserInfo) {
        userInfo.apply {
            judgeImproveInfo(tv_real_name, icon_real_name, fullName)
            judgeImproveInfo(tv_qq_number, icon_qq_number, qq)
            judgeImproveInfo(tv_e_mail, icon_e_mail, email)
            judgeImproveInfo(tv_phone_number, icon_phone_number, phone)
            judgeImproveInfo(tv_we_chat, icon_wechat, wechat)
        }
    }

    private fun judgeImproveInfo(tvInfo: TextView, iconModify: ImageView, infoData: String?) {
        tvInfo.apply {
            if (infoData.isNullOrEmpty()) {
                text = getString(R.string.need_improve)
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.text_tips_blue))
                iconModify.visibility = View.VISIBLE
            } else {
                text = infoData
                setTextColor(ContextCompat.getColor(this@ProfileActivity, R.color.textColorDark))
                iconModify.visibility = View.GONE
            }
        }
    }
}