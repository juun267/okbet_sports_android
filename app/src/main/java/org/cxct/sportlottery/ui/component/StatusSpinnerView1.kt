package org.cxct.sportlottery.ui.component;

import android.content.Context;
import android.media.Image;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import org.cxct.sportlottery.R;
import org.cxct.sportlottery.ui.common.StatusSheetData;

import java.util.ArrayList;
import java.util.List;

public class StatusSpinnerView1 extends FrameLayout {

    View contentView;
    TextView tvName;
    ImageView imgArrow;
    Spinner spStatus;
    List<StatusSheetData> dataList=new ArrayList<>();
    StatusSpinnerAdapter spinnerAdapter;

    public StatusSpinnerView1(@NonNull Context context) {
        super(context);
        init();
    }

    public StatusSpinnerView1(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public StatusSpinnerView1(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        contentView= LayoutInflater.from(getContext()).inflate(R.layout.view_status_spinner,null);
        addView(contentView);
        tvName=contentView.findViewById(R.id.tv_name);
        imgArrow=contentView.findViewById(R.id.img_arrow);
        spStatus=contentView.findViewById(R.id.sp_status);
        contentView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                spStatus.performClick();
            }
        });
    }
    public void setItems(List<StatusSheetData> dataList){
          this.dataList=dataList;
        if (dataList.size()>0){
            StatusSheetData first=dataList.get(0);
            first.setChecked(true);

        }
        spinnerAdapter=new StatusSpinnerAdapter(dataList);
        spStatus.setAdapter(spinnerAdapter);
        spStatus.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        });
    }

}
