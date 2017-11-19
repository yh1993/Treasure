package com.dell.treasure;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;


/**
 * Created by DELL on 2017/7/27.
 */

public class HongbaoActivity extends AppCompatActivity {
    private LinearLayout mVisibleLayout;
    private ImageView mImageView;
    private TextView mTextView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hongbao);

        mVisibleLayout =(LinearLayout)findViewById(R.id.total_layout);
        mVisibleLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                finish();
                return true;
            }
        });

        mImageView=(ImageView)findViewById(R.id.image_hongbao);
        mImageView.setEnabled(true);
        mImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mTextView=(TextView)findViewById(R.id.text_hongbao);
        double hongbao=getIntent().getDoubleExtra("hongbao",0.0);
        mTextView.setText("￥"+hongbao);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
