package com.powervision.powersdk;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import butterknife.ButterKnife;

/**
 * Created by simon on 2017/12/15.
 */

public abstract class BaseActivity extends AppCompatActivity {

    /**
     * @return int layoutResID
     */
    protected abstract int setContentLayoutView();

//    protected abstract void initData();

    protected abstract void initListeners();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(setContentLayoutView());
        ButterKnife.bind(this);
        initListeners();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
