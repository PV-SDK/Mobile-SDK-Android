package com.powervision.powersdk;

import android.annotation.SuppressLint;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.powervision.powersdk.callback.CameraCallback;
import com.powervision.powersdk.callback.RemoteControlCallback;
import com.powervision.powersdk.callback.SystemStatusCallback;
import com.powervision.powersdk.core.PowerSDK;
import com.powervision.powersdk.factory.ConnectIpAndPortFactory;
import com.powervision.powersdk.listener.ConnectListener;
import com.powervision.powersdk.param.BatteryStatusNotifyParam;
import com.powervision.powersdk.param.camera.CameraParams;
import com.powervision.powersdk.param.camera.PVParameter;
import com.vxfly.vflibrary.video.VFSurfaceView;
import com.vxfly.vflibrary.video.VFVideo;
import com.vxfly.vflibrary.video.VFVideoListener;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class CameraActivity extends AppCompatActivity {
    private static final String TAG = CameraActivity.class.getSimpleName();

    @BindView(R.id.camera_video_view)
    VFSurfaceView mVideoSurfaceView;
    @BindView(R.id.camera_mode)
    Button cameraMode;
    @BindView(R.id.camera_action)
    Button cameraAction;
    @BindView(R.id.camera_settings)
    Button cameraSettings;
    @BindView(R.id.camera_conn)
    Button cameraConn;
    @BindView(R.id.control_top)
    Button controlTop;
    @BindView(R.id.contral_left)
    Button contralLeft;
    @BindView(R.id.contral_right)
    Button contralRight;
    @BindView(R.id.contral_bottom)
    Button contralBottom;

    @BindView(R.id.camera_setting_layout)
    ScrollView cameraSettingLayout;
    @BindView(R.id.battery_info)
    TextView batteryInfo;
    @BindView(R.id.battery_temperature)
    TextView batteryTemperature;
    @BindView(R.id.battery_electricity)
    TextView batteryElectricity;
    @BindView(R.id.battery_capacity)
    TextView batteryCapacity;
    @BindView(R.id.battery_cycles)
    TextView batteryCycles;

    @BindView(R.id.control_mode)
    Button controlMode;
    @BindView(R.id.remote_control_japan)
    Button remoteControlJapan;
    @BindView(R.id.remote_control_america)
    Button remoteControlAmerica;
    @BindView(R.id.remote_control_layout)
    LinearLayout remoteControlLayout;

    @BindView(R.id.iso_spinner)
    Spinner isoSpinner;
    @BindView(R.id.aperture_spinner)
    Spinner apertureSpinner;
    @BindView(R.id.ev_spinner)
    Spinner evSpinner;
    @BindView(R.id.shutter_speed_spinner)
    Spinner shutterSpeedSpinner;
    @BindView(R.id.record_shutter_speed_spinner)
    Spinner recordShutterSpeedSpinner;
    @BindView(R.id.single_shot_spinner)
    Spinner singleShotSpinner;
    @BindView(R.id.continuous_shot_spinner)
    Spinner continuousShotSpinner;
    @BindView(R.id.timing_shot_spinner)
    Spinner timingShotSpinner;
    @BindView(R.id.photo_size_spinner)
    Spinner photoSizeSpinner;
    @BindView(R.id.photo_quality_spinner)
    Spinner photoQualitySpinner;
    @BindView(R.id.record_spinner)
    Spinner recordSpinner;
    @BindView(R.id.white_balance_spinner)
    Spinner whiteBalanceSpinner;
    @BindView(R.id.brightness_spinner)
    Spinner brightnessSpinner;
    @BindView(R.id.saturation_spinner)
    Spinner saturationSpinner;
    @BindView(R.id.contrast_spinner)
    Spinner contrastSpinner;
    @BindView(R.id.metering_mode_spinner)
    Spinner meteringModeSpinner;
    @BindView(R.id.af_mode_spinner)
    Spinner afModeSpinner;
    @BindView(R.id.osd_spinner)
    Spinner osdSpinner;
    @BindView(R.id.image_sharpness_spinner)
    Spinner imageSharpnessSpinner;

    @BindView(R.id.take_picture_capacity)
    Button takePictureCapacity;
    @BindView(R.id.reset)
    Button reset;
    @BindView(R.id.format_sd_card)
    Button formatSdCard;
    @BindView(R.id.camera_setting_hide)
    Button cameraSettingHide;
    @BindView(R.id.set_white_balance)
    Button setWhiteBalance;
    @BindView(R.id.white_balance_value)
    TextView whiteBalanceValue;

    private boolean isRecording = false;
    boolean isInitSpinner = true;
    private VFVideo mVideo;

    private String mVideoUrl = "";
    /**
     * default is record video mode
     */
    private boolean isRecordMode = true;
    private boolean isAutoBalance = true;
    private PowerSDK mPowerSDK;
    private Handler mHandler = new MyHandler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);
        ButterKnife.bind(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPowerSDK = PowerSDK.getInstance();
        initVideoView();
        initCameraSettingView();
    }

    /**
     * 初始化视频流
     */
    private void initVideoView() {
        try {
            mVideo = new VFVideo(mVideoSurfaceView, false);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "init vide view is error ... e=" + e.toString());
        }
        mVideo.SetVideoListener(mVideoListener);
        mVideo.SetCloseAfterLostFrame(60);
        //url 视频流的路径可(为文件路径或者null),timeout(毫秒)
        mVideo.OpenVideo(mVideoUrl, 2000, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mVideo != null) {
            mVideo.CloseVideo();
            mVideo.SetVideoListener(null);
        }
        stopPowerSDK();
    }

    /**
     * start connetion device
     */
    private void startConnection() {
        // ip = "192.168.1.12"; port = 20002;
        mPowerSDK.startConnectSDK(ConnectIpAndPortFactory.getEggConnectIpAndPortFactory());
        mPowerSDK.addConnectListener(simpleConnectListener);
        mPowerSDK.startRegisterConnectCallback();
        mPowerSDK.startConnectChain();

        //set camera  listener
        mPowerSDK.setCameraListener(mCameraListener);
        mPowerSDK.setCameraParamListener(mCameraParamListener);
    }

    private void stopPowerSDK() {
        mPowerSDK.startDisconnectDevice();
        mPowerSDK.startDisconnectChain();
        mPowerSDK.removeConnectListener(simpleConnectListener);
    }

    /**
     * camera setting views
     */
    private void initCameraSettingView() {
        isoSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_ISO, position, 51, 70);
                Log.e(TAG, "isoSpinner ... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        apertureSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_APE_V, position, 51, 70);
                Log.e(TAG, "apertureSpinner ... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        evSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                String[] evArray = CameraActivity.this.getResources().getStringArray(R.array.PV_CAM_EXP_MU_VALUE);

                int pos = Integer.valueOf(evArray[position]);
                mPowerSDK.setParameter(CameraParams.PV_CAM_EXP_MU, Float.intBitsToFloat(pos));
                Log.e(TAG, "evSpinner ... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        shutterSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_P_S_S, position, 51, 71);
                Log.e(TAG, "shutterSpeedSpinner ... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });


        singleShotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_SM, position, 54, 54);
                Log.e(TAG, " singleShotSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recordShutterSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_V_S_S, position, 51, 61);
                Log.e(TAG, " recordShutterSpeedSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        continuousShotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_SS, position, 51, 53);
                Log.e(TAG, " continuousShotSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        timingShotSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_D_TIME, position, 1, 2);
                Log.e(TAG, " timingShotSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        photoSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_P_SIZE, position, 51, 55);
                Log.e(TAG, " photoSizeSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        photoQualitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_P_Q, position, 51, 54);
                Log.e(TAG, " photoQualitySpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        recordSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_V_SIZE, position, 51, 65);
                Log.e(TAG, "recordSpinner ... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        whiteBalanceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_WB, position, 51, 52);
                getWhiteBalanceMode(position);
                Log.e(TAG, " whiteBalanceSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        brightnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_BN_V, position, 51, 52);
                Log.e(TAG, " brightnessSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        saturationSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_SATUR_V, position, 51, 52);
                Log.e(TAG, " saturationSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        contrastSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_CONTRA_V, position, 51, 52);
                Log.e(TAG, " contrastSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        meteringModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_LT, position, 51, 53);
                Log.e(TAG, " meteringModeSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        afModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_AF_MODE, position, 51, 52);
                Log.e(TAG, " afModeSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        osdSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_OSD_ON, position, 51, 52);
                Log.e(TAG, " osdSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        imageSharpnessSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isInitSpinner) return;
                position = setSpinnerIndex(CameraParams.PV_CAM_ACUT_V, position, 0, 2);
                Log.e(TAG, " imageSharpnessSpinner... position=" + position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private int setSpinnerIndex(String paramId, int position, int minValue, int maxValue) {
        position += minValue;
        if (position > maxValue) {
            position = maxValue;
        }
        if (position < minValue) {
            position = minValue;
        }
        mPowerSDK.setParameter(paramId, Float.intBitsToFloat(position));
        return position;
    }

    ConnectListener.SimpleConnectListener simpleConnectListener = new ConnectListener.SimpleConnectListener() {
        @Override
        public void onChainConnected() {
            super.onChainConnected();
            Log.e(TAG, "onChainConnected");
            mPowerSDK.startConnectDevice();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraConn.setTextColor(ContextCompat.getColor(CameraActivity.this, R.color.connecting));
                }
            });
        }

        @Override
        public void onDroneConnected() {
            super.onDroneConnected();
            Log.e(TAG, "onDroneConnected");
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mPowerSDK.requestParameter(CameraParams.PV_CAM_STAT);
                }
            });
        }

        @Override
        public void onDeviceDisconnected() {
            super.onDeviceDisconnected();
            Log.e(TAG, "onDeviceDisconnected");
        }

        @Override
        public void onChainDisconnected() {
            super.onChainDisconnected();
            Log.e(TAG, "onChainDisconnected");
        }
    };

    @OnClick({R.id.camera_mode, R.id.camera_action, R.id.camera_settings, R.id.control_top,
            R.id.contral_left, R.id.contral_right, R.id.contral_bottom, R.id.camera_conn,
            R.id.battery_info, R.id.control_mode, R.id.remote_control_japan, R.id.remote_control_america,
            R.id.take_picture_capacity, R.id.reset, R.id.format_sd_card,
            R.id.camera_setting_hide, R.id.set_white_balance})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.camera_mode:
                switchCameraMode();
                break;
            case R.id.camera_action:
                startRecodOrTakePhoto();
                break;
            case R.id.camera_settings:
                startCameraSetting();
                break;
            case R.id.contral_left:
                controlGimbal(0);
                break;
            case R.id.control_top:
                controlGimbal(1);
                break;
            case R.id.contral_right:
                controlGimbal(2);
                break;
            case R.id.contral_bottom:
                controlGimbal(3);
                break;
            case R.id.camera_conn:
                startConnection();
                break;
            case R.id.battery_info:
                startGetBatteryInfo();
                break;
            case R.id.control_mode:
                getRemoteControllerMode();
                break;
            case R.id.remote_control_japan://
                setRemoteControlMode(1);
                break;
            case R.id.remote_control_america:
                setRemoteControlMode(2);
                break;
            case R.id.take_picture_capacity://剩余拍照数量
                mPowerSDK.requestParameter(PVParameter.PV_CAM_SD_PLEFT);
                break;
            case R.id.reset://恢复出厂设置
                mPowerSDK.resetToCameraFactory(7);//
                break;
            case R.id.format_sd_card://格式化sdk卡
                mPowerSDK.formatSD(6);
                break;
            case R.id.camera_setting_hide:
                hideCameraSetting();
                break;
            case R.id.set_white_balance:
                setWhiteBalance();
                break;
        }
    }


    /**
     * 设置白平衡值(手动)
     */
    private void setWhiteBalance() {
        if (isAutoBalance) return;
        else {
            //白平衡 手动设置 具体值 （-100~ 100）
            mPowerSDK.setParameter(CameraParams.PV_CAM_WB_V, Float.intBitsToFloat(50));//自动切换模式   错误
        }
    }

    private void getWhiteBalanceMode(int mode) {
        if (mode == 51) {
            isAutoBalance = true;
        } else {
            isAutoBalance = false;
        }
    }

    /**
     * 获取遥控器控制模式（日本手/美国手）
     */
    private void getRemoteControllerMode() {
        mPowerSDK.setRemoteControlParamListener(remoteControlParamListener);
        mPowerSDK.requestParameter(PVParameter.PV_RC_MODE);
    }

    /**
     * 设置遥控器控制模式
     * <p>
     * 设置成功后在{@link RemoteControlCallback.RemoteControlParamListener#onRemoteControlSetSuccess}回调
     *
     * @param type 控制模式类型（1日本手，2美国手）
     */
    private void setRemoteControlMode(int type) {
        mPowerSDK.setParameter(PVParameter.PV_RC_MODE, type);//callback remoteControlParamListener
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!mVideo.IsPlaying()) {
            mVideo.Play();
            mVideo.GLResume();
        }
    }

    /**
     * 设获取电池信息
     * <p>
     * 设置成功后在{@link SystemStatusCallback.BatteryStatusListener#onBatteryStatus}回调
     */
    private void startGetBatteryInfo() {
        mPowerSDK.setBatteryStatusListener(batteryStatusListener);
    }

    private int[] voltages = null;
    private int batteryRemaining;
    private int temperature;

    @SuppressLint("SetTextI18n")
    private void setBatteryInfo() {
        batteryCapacity.setText(getString(R.string.capacity) + voltages[5] + getString(R.string.capacity_unit));
        batteryCycles.setText(getString(R.string.cycles) + voltages[4]);
        batteryElectricity.setText(getString(R.string.electricity) + batteryRemaining + getString(R.string.percentage));
        batteryTemperature.setText(getString(R.string.temperature) + temperature + getString(R.string.temperature_tag));
    }


    /**
     * 控制云台移动
     *
     * @param direction (value = { 0,1,2,3} left,top ,right ,bottom )
     */
    private void controlGimbal(int direction) {
        mPowerSDK.setGimbalOrientationDirection(direction);
        mHandler.sendEmptyMessageDelayed(0, 800);

    }


    class MyHandler extends Handler {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0://reset  control
                    mPowerSDK.stopGimbalOrientation();
                    break;
                case 1://getbattery info
                    setBatteryInfo();
                    break;
                case 2://get rock mode
                    setRemoteControlModeViews(msg.arg1);
                    break;
                default:
                    break;
            }
        }
    }


    private void setRemoteControlModeViews(int type) {
        if (remoteControlLayout.getVisibility() == View.GONE) {
            remoteControlLayout.setVisibility(View.VISIBLE);
        }
        if (type == 1) {//遥控器 日本手控制模式
            remoteControlJapan.setTextColor(getResources().getColor(R.color.connecting));
            remoteControlAmerica.setTextColor(getResources().getColor(R.color.black));
            remoteControlJapan.setClickable(false);
            remoteControlAmerica.setClickable(true);
        } else {//遥控器 美国手控制模式
            remoteControlAmerica.setTextColor(getResources().getColor(R.color.connecting));
            remoteControlJapan.setTextColor(getResources().getColor(R.color.black));
            remoteControlAmerica.setClickable(false);
            remoteControlJapan.setClickable(true);
        }
    }

    /**
     * requset camera all params. show camera setting views
     */
    private void startCameraSetting() {
        //获取相机设置的所有参数
        if (isInitSpinner) {
            cameraSettingLayout.setVisibility(View.VISIBLE);
            isInitSpinner = false;
        } else if (!isShowCameraViews) {
            cameraSettingLayout.setVisibility(View.VISIBLE);
            return;
        }
        mPowerSDK.requestParameter(CameraParams.PV_CAM_REQ_ALL);
    }

    private boolean isShowCameraViews = true;

    private void hideCameraSetting() {
        isShowCameraViews = false;
        cameraSettingLayout.setVisibility(View.GONE);
    }


    private void startRecodOrTakePhoto() {
        if (!isRecordMode) {
            mPowerSDK.stillCapture();
        } else {
            if (isRecording) {
                mPowerSDK.recStop();
            } else {
                mPowerSDK.recStart();
            }
        }
    }

    private void changeViews() {
        isRecordMode = false;
        cameraAction.setText(getString(R.string.camera_take_photo));
    }

    private void switchCameraMode() {
        if (isRecording) {
            return;
        }
        if (isRecordMode) {
            mPowerSDK.stillCaptureMode();
        } else {
            mPowerSDK.recMode();
        }
    }


    RemoteControlCallback.RemoteControlParamListener remoteControlParamListener = new RemoteControlCallback.RemoteControlParamListener() {

        @Override
        public void onRemoteControlSetSuccess(String paramID) {
            switch (paramID) {
                case PVParameter.PV_RC_MODE://设置 遥控器模式成功
                    int mRockerType = (int) mPowerSDK.getParameter(paramID);
                    Message msg = new Message();
                    msg.what = 2;
                    msg.arg1 = mRockerType;
                    mHandler.sendMessage(msg);
                    break;
            }
        }

        @Override
        public void onRemoteControlSetTimeout(String paramID) {
            switch (paramID) {
                case PVParameter.PV_RC_MODE:
                    break;
            }
        }

        @Override
        public void onRemoteControlGetSuccess(String paramID) {
            switch (paramID) {
                case PVParameter.PV_RC_MODE://获取遥控器 控制模式 成功
                    int mRockerType = (int) mPowerSDK.getParameter(paramID);
                    Message msg = new Message();
                    msg.what = 2;
                    msg.arg1 = mRockerType;
                    mHandler.sendMessage(msg);
                    break;
            }
        }

        @Override
        public void onRemoteControlGetTimeout(String paramID) {
        }
    };

    boolean isNeedReconnect = true;
    VFVideoListener mVideoListener = new VFVideoListener() {
        @Override
        public void onVFVideoWillOpen() {
            Log.e(TAG, "  onVFVideoDidOpen");
        }

        @Override
        public void onVFVideoDidOpen(int i) {
            switch (i) {
                case VFVideo.VF_Success:
                    isNeedReconnect = false;
                    break;
                case VFVideo.VF_HasVideo:
                    if (isNeedReconnect) {
                        openVideoInSubThread();
                    }
                    break;
                case VFVideo.VF_Failed:
                    if (isNeedReconnect) {
                        openVideoInSubThread();
                    }
                    break;
                default:
                    openVideoInSubThread();
            }
            Log.e(TAG, "   onVFVideoDidOpen ");
        }

        @Override
        public void onVFVideoWillDrawFrame() {
        }

        @Override
        public void onVFVideoDidDrawFrame(int result) {
            switch (result) {
                case VFVideo.VF_Success://解码并渲染视频帧成功
//                    isRecording = true;
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",解码并渲染视频帧成功");
                    break;
                case VFVideo.VF_BrokenFrame://解码成功但视频帧不完整，该帧被正常渲染
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",解码成功但视频帧不完整，该帧被正常渲染");
                    break;
                case VFVideo.VF_SkipFrame://解码成功但视频帧不完整，该帧已被屏蔽
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",解码成功但视频帧不完整，该帧已被屏蔽");
                    break;
                case VFVideo.VF_RecvTimeout://超时未收到帧信息
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",超时未收到帧信息");
                    break;
                case VFVideo.VF_RtspClosed://RTSP连接已关闭
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",RTSP连接已关闭");
                    break;
                case VFVideo.VF_NoVideo://没有打开视频
                    Log.e(TAG, "onVFVideoDidDrawFrame: result=" + result + ",IsPlaying=" + mVideo.IsPlaying() + "没有打开视频");
                    break;
                default:
            }
        }

        @Override
        public void onVFVideoWillClose() {
            Log.e(TAG, "      ...onVFVideoWillClose ");
        }

        @Override
        public void onVFVideoDidClose(int i) {
            Log.e(TAG, "      ...onVFVideoWillClose  i=" + i);
        }

        @Override
        public void onVFVideoWillStartRecord() {
            Log.e(TAG, "      ...onVFVideoWillStartRecord ");
        }

        @Override
        public void onVFVideoDidStartRecord(int i) {
            switch (i) {
                case VFVideo.VF_Success://成功开始视频录像
                    Log.e(TAG, "      ...onVFVideoDidStartRecord  success ");
                    break;
                case VFVideo.VF_NoVideo://没有视频正在播放
                    break;
                case VFVideo.VF_InvalidFile://缺少文件保存路径
                    break;
                case VFVideo.VF_Failed://未知原因视频录像失败
                    Log.e(TAG, "      ...onVFVideoDidStartRecord  failed ");
                    break;
            }
        }

        @Override
        public void onVFVideoWillStopRecord() {
            Log.e(TAG, "      ...onVFVideoWillStopRecord ");
        }

        @Override
        public void onVFVideoDidStopRecord(int i) {
            Log.e(TAG, "      ...onVFVideoDidStopRecord ");
        }

        @Override
        public void onVFVideoWillTakeScreenShot() {
            Log.e(TAG, "      ...onVFVideoWillTakeScreenShot ");
        }

        @Override
        public void onVFVideoDidTakeScreenShot(int i) {
        }
    };

    private void openVideoInSubThread() {
        Log.e(TAG, "  openVideoInSubThread...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                mVideo.OpenVideo(mVideoUrl, 2000, false);
            }
        }).start();
    }

    CameraCallback.CameraParamListener mCameraParamListener = new CameraCallback.CameraParamListener() {

        @Override
        public void onCameraParamSetSuccess(String paramId) {
            Log.e(TAG, "...onCameraParamSetSuccess ");
            onUpdateCamerData(paramId);
        }

        @Override
        public void onCameraParamSetTimeout(String paramId) {
            Log.e(TAG, "...onCameraParamSetTimeout ");
        }

        @Override
        public void onCameraParamGetSuccess(String paramId) {
            Log.e(TAG, "...onCameraParamGetSuccess paramId=" + paramId);
            onUpdateCamerData(paramId);
        }

        @Override
        public void onCameraParamGetTimeout(String paramId) {

            Log.e(TAG, "...onCameraParamGetTimeout paramId=" + paramId);
        }
    };

    private void onUpdateCamerData(String paramId) {
        if (!TextUtils.isEmpty(paramId)) {
            if (paramId.equals(CameraParams.PV_CAM_ISO)) {
                upDateSpinnerDate(51, 70, paramId, isoSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_APE_V)) {
                upDateSpinnerDate(51, 70, paramId, apertureSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_EXP_MU)) {
                if (isInitSpinner) return;
                int paramsPosition = Float.floatToRawIntBits(mPowerSDK.getParameter(paramId));// 4,14,24
                String[] evArray = CameraActivity.this.getResources().getStringArray(R.array.PV_CAM_EXP_MU_VALUE);
                String s = String.valueOf(paramsPosition);
                int index = 0;
                if (evArray.length > 1) {
                    for (int i = 0; i < evArray.length; i++) {
                        if (evArray[i].equals(s)) {
                            index = i;
                            break;
                        }
                    }
                }
                final int finalIndex = index;
                Log.e("simon","。。。index ="+index );
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        evSpinner.setSelection(finalIndex, true);
                    }
                });
            } else if (paramId.equals(CameraParams.PV_CAM_P_S_S)) {
                upDateSpinnerDate(51, 71, paramId, shutterSpeedSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_V_S_S)) {
                upDateSpinnerDate(51, 61, paramId, recordShutterSpeedSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_SM)) {
                upDateSpinnerDate(51, 51, paramId, singleShotSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_SS)) {
                upDateSpinnerDate(51, 53, paramId, continuousShotSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_D_TIME)) {
                upDateSpinnerDate(1, 2, paramId, timingShotSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_P_SIZE)) {
                upDateSpinnerDate(51, 55, paramId, photoSizeSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_P_Q)) {
                upDateSpinnerDate(51, 54, paramId, photoQualitySpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_V_SIZE)) {//视频分辨率
                upDateSpinnerDate(51, 65, paramId, recordSpinner);
            } else if (paramId.equals(CameraParams.PV_CAM_WB)) {
                upDateSpinnerDate(51, 52, paramId, whiteBalanceSpinner);
            } else if (paramId.equals(CameraParams.PV_CAM_WB_V)) {//手动设置白平衡值
                final int whiteBalance = Float.floatToRawIntBits(mPowerSDK.getParameter(paramId)) - 100;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        whiteBalanceValue.setText(getResources().getString(R.string.white_balance_value)
                                + String.valueOf(whiteBalance));
                    }
                });
            } else if (paramId.equals(CameraParams.PV_CAM_BN_V)) {
                upDateSpinnerDate(51, 52, paramId, brightnessSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_SATUR_V)) {
                upDateSpinnerDate(51, 52, paramId, saturationSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_CONTRA_V)) {
                upDateSpinnerDate(51, 52, paramId, contrastSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_LT)) {
                upDateSpinnerDate(51, 53, paramId, meteringModeSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_AF_MODE)) {

                upDateSpinnerDate(51, 52, paramId, afModeSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_OSD_ON)) {
                upDateSpinnerDate(51, 52, paramId, osdSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_ACUT_V)) {
                upDateSpinnerDate(0, 2, paramId, imageSharpnessSpinner);

            } else if (paramId.equals(CameraParams.PV_CAM_SD_PLEFT)) {
                final int paramsPosition = Float.floatToRawIntBits(mPowerSDK.getParameter(paramId));
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        takePictureCapacity.setText(String.valueOf(paramsPosition));
                    }
                });
            } else if (paramId.equals(CameraParams.FACTORY_RESET)) {
                Log.e(TAG, " FACTORY_RESET ...ok");

            } else if (paramId.equals(CameraParams.SD_FORMAT)) {
                Log.e(TAG, " SD_FORMAT ...ok");
            }
        } else if (paramId.equals(CameraParams.PV_CAM_STAT)) {
            //相机状态：，1：拍照模式  2：录像模式  3：录像中  4：error
            int mCameraStatus = Float.floatToRawIntBits(mPowerSDK.getParameter(paramId));
            if (paramId.equals("PV_CAM_STAT")) {
                switch (mCameraStatus) {
                    case 1:
                        Log.e(TAG, "photo mode");
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                changeViews();
                            }
                        });
                        break;
                    case 2://没有录像 默认
                        Log.e(TAG, "recod mode");
                        break;
                    case 3://remoteControl
                        isRecording = true;
                        Log.e(TAG, "recoding");
                        break;
                    case 4:
                        Log.e(TAG, "PV_CAM_STAT  error ...");
                        break;
                    default:
                        break;

                }
            }
        }
    }

    /**
     * minValue and maxValue ,see res/values/arrays, camera setting.
     * The range of the corresponding instruction
     *
     * @param minValue    对应的接口设置起始值
     * @param maxValue    对应接口的最大设置值
     * @param paramId     设置camera对应的请求指令
     * @param tempSpinner 对应的spinner
     */
    private void upDateSpinnerDate(int minValue, int maxValue, String paramId,
                                   final Spinner tempSpinner) {
        int paramsPosition = Float.floatToRawIntBits(mPowerSDK.getParameter(paramId));
        if (paramsPosition > maxValue) {
            paramsPosition = maxValue;
        }
        if (paramsPosition < minValue) {
            paramsPosition = minValue;
        }
        Log.e(TAG, "onCameraParamGetSuccess   paramsPosition=" + paramsPosition);
        final int index = paramsPosition - minValue;
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                tempSpinner.setSelection(index, true);
            }
        });
    }


    CameraCallback.CameraListener mCameraListener = new CameraCallback.CameraListener() {

        @Override
        public void onCameraExists() {
            Log.e(TAG, "...onCameraExists ... ");
        }

        @Override
        public void onCameraRecStopEnd() {
            Log.e(TAG, "...onCameraRecStopEnd ... ");
            isRecording = false;
        }

        @Override
        public void onCameraRecRecing() {
            isRecording = true;
            Log.e(TAG, "...onCameraRecRecing ... ");

        }

        @Override
        public void onCameraRecStartError() {
            Log.e(TAG, "...onCameraRecStartError ... ");
        }

        @Override
        public void onCameraRecStopError() {
            Log.e(TAG, "...onCameraRecStopError ... ");
        }

        @Override
        public void onCameraRecMode() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraAction.setText(getString(R.string.camera_record));
                    isRecordMode = !isRecordMode;
                }
            });
            Log.e(TAG, "...onCameraRecMode ... ");
        }

        @Override
        public void onCameraRecModeError() {
            Log.e(TAG, "...onCameraRecModeError ... ");
        }

        @Override
        public void onCameraStillCaptureEnd() {
            Log.e(TAG, "...onCameraStillCaptureEnd ... ");
        }

        @Override
        public void onCameraStillCaptureing() {
            Log.e(TAG, "...onCameraStillCaptureing ... ");
        }

        @Override
        public void onCameraStillCaptureError() {
            Log.e(TAG, "...onCameraStillCaptureError ... ");
        }

        @Override
        public void onCameraStillCaptureMode() {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    cameraAction.setText(getString(R.string.camera_take_photo));
                    isRecordMode = !isRecordMode;
                }
            });
            Log.e(TAG, "...onCameraStillCaptureMode ... ");
        }

        @Override
        public void onCameraStillCaptureModeError() {
        }

        @Override
        public void onCameraFormatSDSuccess() {
        }

        @Override
        public void onCameraFormatSDFailed() {
        }

        @Override
        public void onCameraResetToCameraFactorySuccess() {
        }

        @Override
        public void onCameraResetToCameraFactoryFailed() {
        }

        @Override
        public void onCameraRecSettingSuccess() {
        }

        @Override
        public void onCameraRecSettingFailed() {
        }

        @Override
        public void onCameraCaptureSettingSuccess() {
        }

        @Override
        public void onCameraCaptureSettingFailed() {
        }

        @Override
        public void onCameraPictureSettingSuccess() {
        }

        @Override

        public void onCameraPictureSettingFailed() {
        }

        @Override
        public void onCameraCameraFeedBackTimeout() {
        }
    };

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    SystemStatusCallback.BatteryStatusListener batteryStatusListener = new SystemStatusCallback.BatteryStatusListener() {
        @Override
        public void onBatteryStatus(BatteryStatusNotifyParam param) {
            voltages = param.voltages;
            batteryRemaining = param.battery_remaining;
            temperature = param.temperature;
            mHandler.sendEmptyMessage(1);
        }
    };

}
