package org.cxct.sportlottery.view.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.View
import android.view.animation.*
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import com.gyf.immersionbar.ImmersionBar
import org.cxct.sportlottery.R
import org.cxct.sportlottery.databinding.FragmentRedEnvelopeReceiveBinding
import org.cxct.sportlottery.network.money.RedEnveLopeModel
import org.cxct.sportlottery.ui.base.BaseDialog
import org.cxct.sportlottery.util.DisplayUtil.dp
import org.cxct.sportlottery.util.ScreenUtil
import java.lang.ref.WeakReference
import java.util.*

class RedEnvelopeReceiveDialog: BaseDialog<RedEnveLopeModel,FragmentRedEnvelopeReceiveBinding>() {

    init {
        setStyle(R.style.FullScreen)
    }

    private val mHandler = MyHandler(WeakReference(this))

    private val redenpId by lazy {
        arguments?.getInt(REDENP_ID)
    }

    companion object {
        const val REDENP_ID = "redenpId"

        @JvmStatic
        fun newInstance(redenpId: Int?) = RedEnvelopeReceiveDialog().apply {
            arguments = Bundle().apply {
                putInt(REDENP_ID, redenpId ?: 0)
            }
        }
    }

    private val bitmap by lazy {
        listOf(
            BitmapFactory.decodeResource(context?.resources, R.drawable.ic_redpacket_coin),
            BitmapFactory.decodeResource(context?.resources, R.drawable.ic_redpacket_coin),
            BitmapFactory.decodeResource(context?.resources, R.drawable.ic_redpacket_coin),
            BitmapFactory.decodeResource(context?.resources, R.drawable.ic_redpacket_coin_small),
        )
    }

    val map by lazy {
        mapOf<Bitmap, Long>(
            bitmap[0] to 7060,
            bitmap[1] to 6003,
            bitmap[2] to 5200,
            bitmap[3] to 7140,
        )
    }
//    按照 UI 動畫高度 896 換算，
//    紅包51x66(大)速率:126.9/s
//    紅包45x58(中)速率:148.59/s
//    紅包38x48(小)速率:172.3/s
//    福袋 40x54 速率:125.49/s
//    紅包51x66(大)速率:07s06
//    紅包45x58(中)速率:06s03
//    紅包38x48(小)速率:05s20
//    福袋 40x54 速率:07s14

    //  private val BARRAGE_GAP_MIN_DURATION: Int = 1200
    private val BARRAGE_GAP_DURATION: Long = 1200
    private val BARRAGE_GAP_START_DURATION: Long = 100

    var bitmap1: Bitmap? = null
    var image: ImageView? = null
    var mRandom = Random()
    private var layoutParams1: RelativeLayout.LayoutParams? = null
    private var p: Point? = null
    private var randomX = 0
    private var randomY: Int = 0

    private var successDialog: RedEnvelopeSuccessDialog? = null
    private var failDialog: RedEnvelopeFailDialog? = null


    override fun onInitView() {
        ImmersionBar.with(this).init();
        initView()
        initObserve()
    }

    private fun showSuccessOrFailDialog(isSuccess: Boolean, msg: String) {
        activity?.supportFragmentManager?.let {
            if (isSuccess) {
                successDialog = RedEnvelopeSuccessDialog.newInstance(msg)
                successDialog?.show(it, null)
            } else {
                failDialog = RedEnvelopeFailDialog.newInstance(msg)
                failDialog?.show(it, null)
            }
        }
    }

    fun closeDialog() {
        successDialog?.dismissAllowingStateLoss()
        failDialog?.dismissAllowingStateLoss()
    }

    private fun initObserve() {
        viewModel.redEnvelopePrizeResult.observe(viewLifecycleOwner) { evenResult ->
            evenResult?.getContentIfNotHandled()?.let { result ->
                if (result.success) {
                    val grabMoney = result.redEnvelopePrize?.grabMoney ?: "0"
                    if (grabMoney != "0") {
                        showSuccessOrFailDialog(true, grabMoney)
                    } else {
                        showSuccessOrFailDialog(false, result.msg)
                    }
                } else {
                    showSuccessOrFailDialog(false, result.msg)
                }
                dismissAllowingStateLoss()
            }
        }
    }

    private fun initView() {
        //获取屏幕宽p.x 获取屏幕高p.y
        p = Point()
        activity?.windowManager?.defaultDisplay?.getSize(p);

        setContentView()

        if (successDialog?.isVisible == true) successDialog?.dismissAllowingStateLoss()
        if (failDialog?.isVisible == true) failDialog?.dismissAllowingStateLoss()
    }

    fun setCanceledOnTouchOutside(boolean: Boolean) {
        dialog?.setCanceledOnTouchOutside(boolean)
    }


    private fun setContentView()=binding.run {
        mHandler.sendEmptyMessageDelayed(0, BARRAGE_GAP_START_DURATION)
        context?.let { it ->
            TranslateAnimation(
                -60.dp.toFloat(), (ScreenUtil.getScreenWidth(it) - 60.dp).toFloat(), 0f, 0f
            ).apply {
                duration = 2000
                interpolator = LinearInterpolator()
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }.let {
                ivLightBall.startAnimation(it)
            }
            AlphaAnimation(0.3f, 1f).apply {
                duration = 1500
                repeatMode = Animation.REVERSE
                repeatCount = Animation.INFINITE
            }.let {
                ivBgTop.startAnimation(it)
            }
        }

        ivRedClose.setOnClickListener {
            ivRadiance.clearAnimation()
            dismiss()
        }
    }


    inner class MyHandler(val wDialogFragment: WeakReference<DialogFragment>) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            wDialogFragment.get()?.run {
                for (i in 0..3) {
                    bitmap1 = bitmap[mRandom.nextInt(bitmap.size)]
                    image = ImageView(activity)
                    image!!.setImageBitmap(bitmap1)
                    layoutParams1 = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    val totalX = p!!.x - bitmap1!!.width
                    randomX = Random().nextInt(totalX)
//                    randomX = if (i == 0) {
//                        Random().nextInt((p!!.x * 0.15).toInt())
//                    } else {
//                        Random().nextInt((p!!.x * 0.1).toInt()) + (p!!.x * (0.15 + 0.25 * i)).toInt()
//                    }

                    randomY =
                        (Random().nextInt((p!!.y * (0.2 + 0.05 * i)).toInt()) + image!!.height * 1.3).toInt()
                    layoutParams1!!.setMargins(randomX, -randomY, 0, 0)
                    binding.relativeLayout.addView(image, layoutParams1)
                    var duration = map[bitmap1]
                    startAnimation(image, 0f, duration)
                    image!!.setOnClickListener {
                        viewModel.getRedEnvelopePrize(redenpId)
                    }
                    image = null
                }

                // val duration: Int =  mRandom.nextInt(BARRAGE_GAP_MAX_DURATION)+BARRAGE_GAP_MIN_DURATION
                sendEmptyMessageDelayed(0, BARRAGE_GAP_DURATION)
            }
        }

        fun startAnimation(imageView: View?, Y: Float, duration: Long?) {
            val yAnimator: ObjectAnimator =
                ObjectAnimator.ofFloat(imageView, "translationY", Y, (p!!.y + 300) * 1.2f)
            val YInterpolator: Interpolator = LinearInterpolator()
            yAnimator.interpolator = YInterpolator
            val animatorSet = AnimatorSet()
            animatorSet.play(yAnimator)
            //      animatorSet.duration = (mRandom.nextInt(3000) + 4000).toLong()
            if (duration != null) {
                animatorSet.duration = duration
            }
            animatorSet.start()
            animatorSet.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {}
                override fun onAnimationEnd(animation: Animator) {}
                override fun onAnimationCancel(animation: Animator) {}
                override fun onAnimationRepeat(animation: Animator) {}
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        image = null
        mHandler.removeCallbacksAndMessages(null)

    }
}
