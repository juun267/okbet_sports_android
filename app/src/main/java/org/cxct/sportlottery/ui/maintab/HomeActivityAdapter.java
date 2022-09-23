package org.cxct.sportlottery.ui.maintab;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.youth.banner.adapter.BannerAdapter;
import com.youth.banner.listener.OnBannerListener;

import org.cxct.sportlottery.R;
import org.cxct.sportlottery.ui.game.publicity.PublicityPromotionItemData;

import java.util.List;

public class HomeActivityAdapter extends BannerAdapter<PublicityPromotionItemData, HomeActivityAdapter.BannerViewHolder> {

    OnBannerListener<PublicityPromotionItemData> bannerListener;

    public HomeActivityAdapter(List<PublicityPromotionItemData> mDatas) {
        super(mDatas);
    }

    @Override
    public void setOnBannerListener(OnBannerListener<PublicityPromotionItemData> listener) {
        super.setOnBannerListener(listener);
        this.bannerListener = listener;
    }

    @Override
    public BannerViewHolder onCreateHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_flipper, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindView(BannerViewHolder holder, PublicityPromotionItemData data, int position, int size) {
        holder.textView.setText(data.getTitle());
        holder.textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bannerListener.OnBannerClick(data, position);
            }
        });
    }

    class BannerViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public BannerViewHolder(@NonNull View view) {
            super(view);
            this.textView = view.findViewById(R.id.tv_marquee);
        }
    }
}