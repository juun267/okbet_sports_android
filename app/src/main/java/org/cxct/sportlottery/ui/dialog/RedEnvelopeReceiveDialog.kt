package org.cxct.sportlottery.ui.dialog

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Point
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.Interpolator
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.RelativeLayout
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.fragment_red_envelope_receive.*
import org.cxct.sportlottery.R
import org.cxct.sportlottery.network.money.RedEnveLopeModel
import org.cxct.sportlottery.ui.base.BaseDialog
import java.lang.ref.WeakReference
import java.util.*

class RedEnvelopeReceiveDialog(
    context: Context?,
    var redenpId: Int?,
) : BaseDialog<RedEnveLopeModel>(RedEnveLopeModel::class) {
    private val mHandler = MyHandler(WeakReference(this))
    var bitmap = listOf(
        BitmapFactory.decodeResource(context?.resources, R.drawable.packet_one),
        BitmapFactory.decodeResource(context?.resources, R.drawable.packet_two),
        BitmapFactory.decodeResource(context?.resources, R.drawable.packet_three),
        BitmapFactory.decodeResource(context?.resources, R.drawable.luck_packet),
    )
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

    init {
        setStyle(R.style.FullScreen)
    }
    private val BARRAGE_GAP_MIN_DURATION: Long = 1000 //两个弹幕的最小间隔时间
    private val BARRAGE_GAP_MAX_DURATION: Long = 3000 //两个弹幕的最大间隔时间

    var bitmap1: Bitmap? = null
    var image: ImageView? = null
    var mRandom = Random()
    private var layoutParams1: RelativeLayout.LayoutParams? = null
    private var p: Point? = null
    private var randomX = 0
    private var randomY: Int = 0

    private var successDialog: RedEnvelopeSuccessDialog? = null
    private var failDialog: RedEnvelopeFailDialog? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? = inflater.inflate(R.layout.fragment_red_envelope_receive, container, false)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
                dismiss()
            }
        }
    }

    private fun initView() {
        //获取屏幕宽p.x 获取屏幕高p.y
        p = Point()
        activity?.windowManager?.defaultDisplay?.getSize(p);

        setContentView()

        if (successDialog?.isVisible == true) successDialog?.dismiss()
        if (failDialog?.isVisible == true) failDialog?.dismiss()
    }

    fun setCanceledOnTouchOutside(boolean: Boolean) {
        dialog?.setCanceledOnTouchOutside(boolean)
    }


    private fun setContentView() {
        val duration: Int =
            ((BARRAGE_GAP_MAX_DURATION - BARRAGE_GAP_MIN_DURATION) * Math.random()).toInt()
        mHandler.sendEmptyMessageDelayed(0, duration.toLong())
        val operatingAnim = AnimationUtils.loadAnimation(
            activity, R.anim.red_envelope_rotate
        )
        val lin = LinearInterpolator()
        operatingAnim.interpolator = lin
        iv_radiance.startAnimation(operatingAnim)
        iv_red_close.setOnClickListener {
            iv_radiance.clearAnimation()
            dismiss()
        }
    }


    inner class MyHandler(val wDialogFragment: WeakReference<DialogFragment>) :
        Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            wDialogFragment.get()?.run {
                for (i in 0..2) {
                    bitmap1 = bitmap[mRandom.nextInt(bitmap.size)]
                    image = ImageView(activity)
                    image!!.setImageBitmap(bitmap1)
                    layoutParams1 = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT
                    )
                    randomX = if (i == 0) {
                        Random().nextInt((p!!.x * 0.15).toInt())
                    } else {
                        Random().nextInt((p!!.x * 0.1).toInt()) + (p!!.x * (0.15 + 0.25 * i)).toInt()
                    }

                    randomY =
                        (Random().nextInt((p!!.y * (0.2 + 0.05 * i)).toInt()) + image!!.height * 1.3).toInt()
                    layoutParams1!!.setMargins(randomX, -randomY, 0, 0)
                    relative_layout.addView(image, layoutParams1)
                    var duration = map[ bitmap1]
                    startAnimation(image, 0f, duration)
                    image!!.setOnClickListener {
                        viewModel.getRedEnvelopePrize(redenpId)
                    }
                    image = null
                }

                val duration: Int =
                    ((BARRAGE_GAP_MAX_DURATION - BARRAGE_GAP_MIN_DURATION) * Math.random()).toInt()
                sendEmptyMessageDelayed(0, duration.toLong())
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

    override fun onStart() {
        super.onStart()

//        dialog?.window?.setLayout(
//            ViewGroup.LayoutParams.MATCH_PARENT,
//            ViewGroup.LayoutParams.MATCH_PARENT
//        )
    }

    override fun onDestroy() {
        super.onDestroy()
        image = null
        mHandler.removeMessages(0)

    }
}
