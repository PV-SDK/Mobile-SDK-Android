package com.powervision.powersdk;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class FounctionListActivity extends AppCompatActivity {

    @BindView(R.id.mount)
    Button mount;
    @BindView(R.id.camera)
    Button camera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_founction);
        ButterKnife.bind(this);

    }

    @OnClick({R.id.mount, R.id.camera})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.mount:
                startActivity(new Intent(this,MainActivity.class));
                break;
            case R.id.camera:
                startActivity(new Intent(this,CameraActivity.class));
                break;
        }
    }

}
