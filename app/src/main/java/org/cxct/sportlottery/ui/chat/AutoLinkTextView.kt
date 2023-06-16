package org.cxct.sportlottery.ui.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.text.SpannableString
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.AttributeSet
import android.view.View
import androidx.annotation.NonNull
import java.util.regex.Matcher
import java.util.regex.Pattern

/**
 * @author Hewie
 * 用於解決原生的autolink會有超連結標記不完全的問題
 */

const val regex = "(https?|http)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]"

class AutoLinkTextView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyle: Int = 0,
) : androidx.appcompat.widget.AppCompatTextView(context, attrs, defStyle) {

    override fun setText(text: CharSequence?, type: BufferType?) {
        val str = text.toString()

        val spannableString = SpannableString(str)
        val urlMatcher: Matcher = Pattern.compile(regex).matcher(str)
        while (urlMatcher.find()) {
            val url: String = urlMatcher.group()
            val start: Int = urlMatcher.start()
            val end: Int = urlMatcher.end()
            spannableString.setSpan(GoToURLSpan(url), start, end, 0)
        }

        super.setText(spannableString, type)
        super.setMovementMethod(LinkMovementMethod())
    }


    class GoToURLSpan(@param: NonNull private val url: String) : ClickableSpan() {
        override fun onClick(view: View) {
            val webPage: Uri = Uri.parse(url)
            val intent = Intent(Intent.ACTION_VIEW, webPage)
            view.context.startActivity(intent)
        }
    }
}