package org.cxct.sportlottery.view

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.widget.FrameLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import org.cxct.sportlottery.databinding.ViewChatEmojiBinding
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatColumnAdapter
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatEmojiAdapter
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatGifAdapter
import org.cxct.sportlottery.ui.chat.bean.EmojiColumnBean

class ChatEmojiView(context: Context, attrs: AttributeSet?): FrameLayout(context,attrs) {
    //表情类别
    private val columnAdapter= RecyclerChatColumnAdapter()

    //emoji adapter
    private val emojiAdapter= RecyclerChatEmojiAdapter()
    private val gridManager=GridLayoutManager(context,8)

    //picture adapter
    private val gifAdapter= RecyclerChatGifAdapter()
    private val gridManager4=GridLayoutManager(context,4)

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
            val item=columnAdapter.data[position]
            item.select=true
            columnAdapter.notifyDataSetChanged()
            when(item.name){
                "Emoji"->{
                    binding.recyclerEmoji.layoutManager=gridManager
                    binding.recyclerEmoji.adapter=emojiAdapter
                }
                else->{
                    binding.recyclerEmoji.layoutManager=gridManager4
                    binding.recyclerEmoji.adapter=gifAdapter
                }
            }
        }
        emojiAdapter.setList(ViewAction.emojiString.split(" ").toMutableList())
        initGifList()
    }

    //图片表情包
    private fun initGifList(){
        gifAdapter.setList(arrayListOf("","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","","",""))
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