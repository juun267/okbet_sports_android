package org.cxct.sportlottery.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.ViewChatEmojiBinding
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatColumnAdapter
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatEmojiAdapter
import org.cxct.sportlottery.ui.chat.bean.EmojiColumnBean

class ChatEmojiView(context: Context, attrs: AttributeSet?): FrameLayout(context,attrs) {
    //表情类别
    private val columnAdapter= RecyclerChatColumnAdapter()

    //表情列表adapter
    private val emojiAdapter= RecyclerChatEmojiAdapter()

    val binding:ViewChatEmojiBinding
    init {
        binding =ViewChatEmojiBinding.inflate(LayoutInflater.from(context), this,true)
        initView()
    }

    private fun initView(){
        binding.run {
            val manager=LinearLayoutManager(context)
            manager.orientation=LinearLayoutManager.HORIZONTAL
            recyclerColumn.layoutManager=manager
            recyclerColumn.adapter=columnAdapter

            val gridManager=GridLayoutManager(context,8)
            recyclerEmoji.layoutManager=gridManager
            recyclerEmoji.adapter=emojiAdapter

//            blurGroup.onClick {
//                itemBlock("")
//            }
        }
        initColumn()
    }


    @SuppressLint("NotifyDataSetChanged")
    private fun initColumn(){
        val c1= EmojiColumnBean("Emoji",true)
        val c2= EmojiColumnBean("Gif")
        val c3= EmojiColumnBean("OkGif")
        columnAdapter.setList(arrayListOf(c1,c2,c3))
        columnAdapter.setOnItemClickListener{_,_,position->
            clearSelect()
            columnAdapter.data[position].select=true
            columnAdapter.notifyDataSetChanged()
        }



        emojiAdapter.setList(ViewAction.emojiString.split(" ").toMutableList())

    }
    private var itemBlock:(emojiText:String)->Unit={}
    fun setOnEmojiSelect(block:(emojiText:String)->Unit){
        itemBlock=block
        emojiAdapter.setOnItemClickListener{_,_,position->
            block(emojiAdapter.data[position])
        }
    }

    private fun clearSelect(){
        columnAdapter.data.forEach {
            it.select=false
        }
    }
}