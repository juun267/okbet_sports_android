package org.cxct.sportlottery.ui.profileCenter.profile

import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Observer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.luck.picture.lib.entity.LocalMedia
import com.luck.picture.lib.listener.OnResultCallbackListener
import kotlinx.android.synthetic.main.activity_profile.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.uploadImg.UploadImgRequest
import org.cxct.sportlottery.repository.sConfigData
import org.cxct.sportlottery.repository.sLoginData
import org.cxct.sportlottery.ui.base.BaseActivity
import org.cxct.sportlottery.ui.profileCenter.changePassword.SettingPasswordActivity
import org.cxct.sportlottery.ui.profileCenter.nickname.ChangeNicknameActivity
import org.cxct.sportlottery.util.ToastUtil
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

class ProfileActivity : BaseActivity<ProfileModel>(ProfileModel::class) {

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

        initButton()
        initObserve()
    }

    override fun onResume() {
        super.onResume()
        refreshView()
    }

    private fun initButton() {
        btn_back.setOnClickListener {
            finish()
        }

        btn_nickname.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, ChangeNicknameActivity::class.java))
        }

        btn_pwd_setting.setOnClickListener {
            startActivity(Intent(this@ProfileActivity, SettingPasswordActivity::class.java))
        }

        btn_head.setOnClickListener {
            AvatarSelectorDialog(this, mSelectMediaListener).show(supportFragmentManager, null)
        }
    }

    private fun refreshView() {
        updateAvatar(sLoginData?.iconUrl)
        tv_nickname.text = sLoginData?.nickName
        tv_member_account.text = sLoginData?.userName
        tv_id.text = sLoginData?.userId?.toString()
        tv_real_name.text = sLoginData?.fullName
    }

    private fun updateAvatar(iconUrl: String?) {
        Glide.with(iv_head.context)
            .load(iconUrl)
            .apply(RequestOptions().placeholder(R.drawable.ic_head))
            .into(iv_head) //載入頭像
    }

    private fun uploadImg(file: File) {
        val uploadImgRequest = UploadImgRequest(sLoginData?.userId.toString(), file)
        viewModel.uploadImage(uploadImgRequest)
    }

    private fun initObserve() {
        viewModel.uploadImgResult.observe(this, Observer {
            if (it?.success == true)
                updateAvatar(sConfigData?.resServerHost + it.imgData?.path)
            else
                ToastUtil.showToastInCenter(this, it?.msg)
        })

        viewModel.editIconUrlResult.observe(this, Observer {
            if (it?.success == true)
                ToastUtil.showToastInCenter(this, getString(R.string.save_avatar_success))
            else
                ToastUtil.showToastInCenter(this, it?.msg)
        })
    }
}