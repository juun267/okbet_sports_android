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
import org.cxct.sportlottery.net.chat.data.ChatSticker
import org.cxct.sportlottery.net.chat.data.ChatStickerRow
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatColumnAdapter
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatEmojiAdapter
import org.cxct.sportlottery.ui.chat.adapter.RecyclerChatGifAdapter

class ChatEmojiView(context: Context, attrs: AttributeSet?): FrameLayout(context,attrs) {
    //表情类别
    private val columnAdapter= RecyclerChatColumnAdapter()

    //emoji adapter
    private val emojiAdapter= RecyclerChatEmojiAdapter()
    private val gridManager=GridLayoutManager(context,8)

    //gif适配器列表
    private val gifAdaptersList= arrayListOf<RecyclerChatGifAdapter>()
    private val gridManager4=GridLayoutManager(context,4)

    val binding:ViewChatEmojiBinding

    init {
        binding =ViewChatEmojiBinding.inflate(LayoutInflater.from(context), this,true)
        initView()
    }

    private fun initView(){
        binding.run {
            //横向表情栏目
            val manager=LinearLayoutManager(context)
            manager.orientation=LinearLayoutManager.HORIZONTAL
            recyclerColumn.layoutManager=manager
            recyclerColumn.adapter=columnAdapter

            //九宫格表情列表
            val gridManager=GridLayoutManager(context,8)
            recyclerEmoji.layoutManager=gridManager
            recyclerEmoji.adapter=emojiAdapter

        }
    }


    @SuppressLint("NotifyDataSetChanged")
    fun initColumn(data:List<ChatStickerRow>){
        //本地emoji数据
        val emojiRow=initEmojiData()
        emojiAdapter.setList(emojiRow.list)
        //排序拿到的表情列表
        val stickerList=data.toMutableList()
        //添加emoji到第一个
        stickerList.add(0,emojiRow)
        columnAdapter.setList(stickerList)

        //表情栏目  切换点击
        columnAdapter.setOnItemClickListener{_,_,position->
            clearSelect()
            val item=columnAdapter.data[position]
            //选中该类型
            item.select=true
            columnAdapter.notifyDataSetChanged()

            //更换表情列表适配器
            when(item.typeName){
                //本地emoji
                "Emoji"->{
                    binding.recyclerEmoji.layoutManager=gridManager
                    binding.recyclerEmoji.adapter=emojiAdapter
                }
                else->{
                    //配置的图片
                    if(position>gifAdaptersList.size-1){
                        return@setOnItemClickListener
                    }
                    binding.recyclerEmoji.layoutManager=gridManager4
                    binding.recyclerEmoji.adapter=gifAdaptersList[position]
                }
            }
        }

        initGifList(stickerList)
    }

    //初始化emoji数据
    private fun initEmojiData():ChatStickerRow{
        val emojiRow=ChatStickerRow("Emoji", arrayListOf(),true)
        val emojiStringList=ViewAction.emojiString.split(" ").toMutableList()
        emojiStringList.forEach {
            val temp=ChatSticker(-1,"Emoji", it,-100,-1)
            emojiRow.list.add(temp)
        }
        return emojiRow
    }

    //图片表情包
    private fun initGifList(data:List<ChatStickerRow>){
        gifAdaptersList.clear()
        data.forEach {row->
            val adapter=RecyclerChatGifAdapter()
            adapter.setList(row.list.sortedBy { it.sort })
            gifAdaptersList.add(adapter)
        }
    }

    //点击emoji表情
    fun setOnEmojiSelect(block:(emojiText:String)->Unit){
        emojiAdapter.setOnItemClickListener{_,_,position->
            block(emojiAdapter.data[position].url)
        }
    }

    //点击发送图片表情
    fun setOnPictureSelect(block:(picturePath:String)->Unit){
        gifAdaptersList.forEach { adapter->
            adapter.setOnItemClickListener{_,_,position->
                block(adapter.data[position].url)
            }
        }
    }

    private fun clearSelect(){
        columnAdapter.data.forEach {
            it.select=false
        }
    }
}