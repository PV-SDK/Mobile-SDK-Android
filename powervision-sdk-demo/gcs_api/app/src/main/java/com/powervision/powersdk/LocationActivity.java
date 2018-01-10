package com.powervision.powersdk;

import android.annotation.SuppressLint;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.powervision.powersdk.core.PowerSDK;
import com.powervision.powersdk.factory.ConnectIpAndPortFactory;
import com.powervision.powersdk.listener.ConnectListener;
import com.powervision.powersdk.listener.MissionListener;
import com.powervision.powersdk.listener.PositionListener;
import com.powervision.powersdk.listener.SystemStatusListener;
import com.powervision.powersdk.param.Attitude;
import com.powervision.powersdk.param.GlobalPositionIntParam;
import com.powervision.powersdk.param.GpsRawIntParam;
import com.powervision.powersdk.param.WaypointParam;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * Created by simon
 * <p>
 * 姿态获取 （xyz速度，俯仰，横滚，航向角度，角速度，对地高度，海拔高度）
 * gps 实时位置 gps 状态
 * 发送航点，执行航点飞行， 自动返航
 */

public class LocationActivity extends BaseActivity {
    public static final String TAG = LocationActivity.class.getSimpleName();

    @BindView(R.id.attitude_vx)
    TextView attitudeVx;
    @BindView(R.id.attitude_vy)
    TextView attitudeVy;
    @BindView(R.id.attitude_vz)
    TextView attitudeVz;
    @BindView(R.id.attitude_alt)
    TextView attitudeAlt;
    @BindView(R.id.attitude_relative_alt)
    TextView attitudeRelativeAlt;
    @BindView(R.id.attitude_relative_lat)
    TextView attitudeRelativeLat;
    @BindView(R.id.attitude_relative_lon)
    TextView attitudeRelativeLon;
    @BindView(R.id.attitude_pitch)
    TextView attitudePitch;
    @BindView(R.id.attitude_roll)
    TextView attitudeRoll;
    @BindView(R.id.attitude_yaw)
    TextView attitudeYaw;
    @BindView(R.id.excute_way_point)
    Button excuteWayPoint;

    @BindView(R.id.attitude)
    Button attitude;
    @BindView(R.id.gps_info)
    Button gpsInfo;
    @BindView(R.id.connect_status)
    Button connectStatus;
    @BindView(R.id.set_way_point)
    Button setWayPoint;
    @BindView(R.id.automatically_return)
    Button automaticallyReturn;
    @BindView(R.id.point_1)
    TextView point1;
    @BindView(R.id.point_2)
    TextView point2;
    @BindView(R.id.point_3)
    TextView point3;
    @BindView(R.id.point_4)
    TextView point4;
    @BindView(R.id.way_points_layout)
    LinearLayout wayPointsLayout;
    @BindView(R.id.satellite_num)
    TextView satelliteNum;
    @BindView(R.id.back_last_mode)
    Button backLastMode;

    private PowerSDK mPowerSDK;
    /**
     * 0未连接；
     * 1链路已连接；2设备已连接；
     * 3设备已断开；4链路已断开
     */
    private int connectStatusCode = 0;

    private GlobalPositionIntParam mCurrnetPoint;


    private WaypointParam[] waypointParamArray;
    /**
     * Longitude（east west orientation）1m：360°/31544206M=1.141255544679108e-5= 0.00001141
     * Latitude（north and south orientation）1m：360°/40030173M=8.993216192195822e-6=0.00000899
     */
    float lonPlus = 0.0002282f;//00001141 *20(m) = 0.0002282f
    float latPlus = 0.0001789f;//0.00000899 *20 (m)=0.0001789f
    private int flyControlMode = -1;

    private boolean wayPointSendSuccess = false;
    private boolean isInitConnect = true;


    private static final float progressive = 10000000.0f;
    private static final int SIMULATION_HEIGHT = 20;//模拟航点飞行高度(单位 m)


    @Override
    protected int setContentLayoutView() {
        return R.layout.activity_fly_control;
    }

    @Override
    protected void initListeners() {
        mPowerSDK = PowerSDK.getInstance();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mPowerSDK.addConnectListener(simpleConnectListener);
        //获取当前控制模式
        mPowerSDK.addModeChangedListener(mModeChangedListener);
        //切换上一次模式
        mPowerSDK.addBackToLastModeListener(mBackToLastModeListener);
        //航点发送状态监听器
        mPowerSDK.addMissionStatusListener(mMissionStatusListener);
        //航点运行 节点
        mPowerSDK.addMissionRunListener(mMissionRunListener);
        //开始航点飞行
        mPowerSDK.addStartWaypointListener(mStartWaypointListener);
    }

    @OnClick({R.id.connect_status, R.id.attitude, R.id.gps_info, R.id.set_way_point
            , R.id.automatically_return, R.id.excute_way_point, R.id.back_last_mode})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.connect_status:
                startConnect();
                break;
            case R.id.attitude:
                getAttitudeInfo();
                break;
            case R.id.gps_info:
                getGPSInfo();
                break;
            case R.id.set_way_point:
                setWayPoint();
                break;
            case R.id.excute_way_point:
                excutePoints();
                break;
            case R.id.back_last_mode:
                excuteBackLastMode();
                break;
            case R.id.automatically_return:
                setAutomaticallyReturn();
                break;
        }
    }

    /**
     * 返回上一次飞行模式
     */
    private void excuteBackLastMode() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPowerSDK.getArmStatus() == 0) {
            Toast.makeText(LocationActivity.this, R.string.please_unlock, Toast.LENGTH_SHORT).show();
            return;
        }
        mPowerSDK.setBackToLastMode();
    }


    private void excutePoints() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        //航点没有发送成功，航点不存在
        if (mCurrnetPoint == null || waypointParamArray == null || waypointParamArray.length <= 0 /*|| !wayPointSendSuccess*/) {
            Toast.makeText(LocationActivity.this, R.string.way_point_data_errro, Toast.LENGTH_SHORT).show();
            Log.d(TAG, " excutePoints()  way points data  error... ");
            return;
        }
        if (getFlyControlMode()) {
            //执行航点飞行
            int result = mPowerSDK.startExecuteWayPoints();
            Log.d(TAG, "excutePoints     flyControlMode =" + flyControlMode + "  result=" + result);
            Toast.makeText(LocationActivity.this, R.string.excute_way_point, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(LocationActivity.this, R.string.mode_not_exe, Toast.LENGTH_SHORT).show();
        }

    }

    private void setAutomaticallyReturn() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPowerSDK.getArmStatus() == 0) {
            Toast.makeText(LocationActivity.this, R.string.please_unlock, Toast.LENGTH_SHORT).show();
            return;
        }
        mPowerSDK.startAutomaticallyReturn();
        Toast.makeText(LocationActivity.this, R.string.automatic_return, Toast.LENGTH_SHORT).show();
    }

    private void setWayPoint() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mPowerSDK.getArmStatus() == 0) {
            Toast.makeText(LocationActivity.this, R.string.please_unlock, Toast.LENGTH_SHORT).show();
            return;
        }
        if (mCurrnetPoint == null) {
            //没有获取到 经纬度信息
            return;
        }
        setWayParam();
    }


    private void startConnect() {
        if (isInitConnect) {
            isInitConnect = false;
            startPowerSDK();
        }
    }

    MissionListener.MissionRunListener mMissionRunListener = new MissionListener.MissionRunListener() {

        @Override
        public void onMissionRunCurrent(int seq) {
            Log.d(TAG, " misson onMissionRunCurrent...  seq=" + seq);
        }

        @Override
        public void onMissionRunReached(final int seq) {
            Log.d(TAG, " misson onMissionRunReached ...  seq=" + seq);
        }
    };

    MissionListener.StartWaypointListener mStartWaypointListener = new MissionListener.StartWaypointListener() {

        @Override
        public void onWaypointStart() {
            Log.d(TAG, "onWaypointStart");
        }

        @Override
        public void onWaypointStop() {
            Log.d(TAG, "onWaypointStop");
        }

        @Override
        public void onWaypointTimeout() {
            Log.d(TAG, "onWaypointTimeout");
        }
    };

    ConnectListener.SimpleConnectListener simpleConnectListener = new ConnectListener.SimpleConnectListener() {
        @Override
        public void onChainConnected() {
            super.onChainConnected();
            Log.d(TAG, "onChainConnected");
            connectStatusCode = 1;
            mPowerSDK.startConnectDevice();
        }

        @Override
        public void onDroneConnected() {
            super.onDroneConnected();
            Log.d(TAG, "onDroneConnected");
            connectStatusCode = 2;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectStatus.setText(R.string.device_connected);
                    connectStatus.setTextColor(ContextCompat.getColor(LocationActivity.this, R.color.connecting));
                }
            });
        }

        @Override
        public void onDeviceDisconnected() {
            super.onDeviceDisconnected();
            Log.d(TAG, "onDeviceDisconnected");
            connectStatusCode = 3;
        }

        @Override
        public void onChainDisconnected() {
            super.onChainDisconnected();
            Log.d(TAG, "onChainDisconnected");
            connectStatusCode = 4;
        }
    };

    /**
     * 点击连接设备 ，执行设备连接
     */
    private void startPowerSDK() {
        mPowerSDK.startConnectSDK(ConnectIpAndPortFactory.getEyeConnectIpAndPortFactory());
        mPowerSDK.startRegisterConnectCallback();
        mPowerSDK.startConnectChain();
    }

    private void stopPowerSDK() {
        mPowerSDK.startDisconnectDevice();
        mPowerSDK.startDisconnectChain();
        mPowerSDK.removeConnectListener(simpleConnectListener);
    }

    private void getGPSInfo() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        mPowerSDK.addGpsRawIntListener(mGpsRawIntListener);
    }

    public void getAttitudeInfo() {
        if (connectStatusCode != 2) {
            Toast.makeText(LocationActivity.this, R.string.device_dis_connect, Toast.LENGTH_SHORT).show();
            return;
        }
        mPowerSDK.addAttitudeAndGroundspeedChangedListener(mAttitudeAndGroundspeedChangedListener);
        mPowerSDK.addPositionChangedListener(mPositionChangedListener);
    }


    /**
     * 发送模拟航点数据
     */
    public void setWayParam() {
        propareWayPoints();
        wayPointsLayout.setVisibility(View.VISIBLE);
        if (waypointParamArray != null && waypointParamArray.length > 0) {
            Log.d("my_info",
                    " 0 waypointParamArray[0].x =" + waypointParamArray[0].x +
                            "  waypointParamArray[0].y =" + waypointParamArray[0].y +
                            "  waypointParamArray[0].z =" + waypointParamArray[0].z +
                            "  waypointParamArray[0].seq =" + waypointParamArray[0].seq +
                            "  waypointParamArray[0].param1 =" + waypointParamArray[0].param1

                            + " 1 waypointParamArray[1].x =" + waypointParamArray[1].x +
                            "  waypointParamArray[1].y =" + waypointParamArray[1].y +
                            "  waypointParamArray[1].z =" + waypointParamArray[1].z +
                            "  waypointParamArray[1].seq =" + waypointParamArray[1].seq +
                            "  waypointParamArray[1].param1 =" + waypointParamArray[1].param1

                            + " 2 waypointParamArray[2].x =" + waypointParamArray[2].x +
                            "  waypointParamArray[2].y =" + waypointParamArray[2].y +
                            "  waypointParamArray[2].z =" + waypointParamArray[2].z +
                            "  waypointParamArray[2].seq =" + waypointParamArray[2].seq +
                            "  waypointParamArray[2].param1 =" + waypointParamArray[2].param1

                            + " 3 waypointParamArray[3].x =" + waypointParamArray[3].x +
                            "  waypointParamArray[3].y =" + waypointParamArray[3].y +
                            "  waypointParamArray[3].z =" + waypointParamArray[3].z +
                            "  waypointParamArray[3].seq =" + waypointParamArray[3].seq +
                            "  waypointParamArray[3].param1 =" + waypointParamArray[3].param1
            );

            //
            int result = mPowerSDK.startSetWayPointsParam(waypointParamArray);
            Log.d(TAG, "    onMission retult =" + result);
        }
    }

    /**
     * 获取当前经纬度，模拟四个航点
     * （The straight line distance between two destinations is not recommended for more than 900m.
     * 两个航点的直线距离建议不要超过900m. ）
     */
    private void propareWayPoints() {
        //设置航点，将当前获取的经纬度数据（经纬度正常取值范围内的值）
        waypointParamArray = new WaypointParam[]{
                new WaypointParam(3, mCurrnetPoint.lat / progressive + latPlus, mCurrnetPoint.lon / progressive, SIMULATION_HEIGHT, 0),
                new WaypointParam(3, mCurrnetPoint.lat / progressive + latPlus, mCurrnetPoint.lon / progressive + lonPlus, SIMULATION_HEIGHT, 1),
                new WaypointParam(3, mCurrnetPoint.lat / progressive, mCurrnetPoint.lon / progressive + lonPlus, SIMULATION_HEIGHT, 2),
                new WaypointParam(3, mCurrnetPoint.lat / progressive, mCurrnetPoint.lon / progressive, SIMULATION_HEIGHT, 3)
        };

        point1.setText(getString(R.string.point_content, waypointParamArray[0].x,
                waypointParamArray[0].y, waypointParamArray[0].z));
        point2.setText(getString(R.string.point_content, waypointParamArray[1].x,
                waypointParamArray[1].y, waypointParamArray[1].z));
        point3.setText(getString(R.string.point_content, waypointParamArray[2].x,
                waypointParamArray[2].y, waypointParamArray[2].z));
        point4.setText(getString(R.string.point_content, waypointParamArray[3].x,
                waypointParamArray[3].y, waypointParamArray[3].z));
    }


    private boolean getFlyControlMode() {
        Log.d(TAG, "get mode=" + flyControlMode);
        return (flyControlMode == SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_MANUAL
                || flyControlMode == SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_ALTCTL
                || flyControlMode == SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_POSCTL);
    }

    private void setFlyControlMode(int mode) {
        flyControlMode = mode;
        Log.d(TAG, "setFlyControlMode mode=" + mode);
    }

    PositionListener.AttitudeAndGroundspeedChangedListener mAttitudeAndGroundspeedChangedListener
            = new PositionListener.AttitudeAndGroundspeedChangedListener() {

        @Override
        public void onAttitudeAndGroundSpeedChanged(int status) {
            Attitude attitude = mPowerSDK.startGetAttitude();
            Message msg = new Message();
            msg.obj = attitude;
            msg.what = 0;
            mHandler.sendMessage(msg);
        }
    };

    PositionListener.PositionChangedListener mPositionChangedListener = new PositionListener.PositionChangedListener() {
        @Override
        public void onPositionChanged() {
            GlobalPositionIntParam mGlobalPositionIntParam = mPowerSDK.startGetGlobalPositionParam();
            if (mGlobalPositionIntParam != null) {

                Message msg = new Message();
                msg.obj = mGlobalPositionIntParam;
                msg.what = 1;
                mHandler.sendMessage(msg);
            }
        }
    };

    PositionListener.GpsRawIntListener mGpsRawIntListener = new PositionListener.GpsRawIntListener() {
        @Override
        public void onGPSRawIntChanged() {
            GpsRawIntParam gp = mPowerSDK.startGetGpsParam();
            final int num = gp.satellites_visible;
            Log.d(TAG, "   satellites  num=" + num);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    satelliteNum.setText(getString(R.string.attitude_satellites, num));
                }
            });
        }
    };

    SystemStatusListener.ModeChangedListener mModeChangedListener = new SystemStatusListener.ModeChangedListener() {
        @Override
        public void onModeChanged() {
            Log.d(TAG, " onModeChanged");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED);
        }

        @Override
        public void onModeChangedManual() {
            Log.d(TAG, " onModeChangedManual");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_MANUAL);
        }

        @Override
        public void onModeChangedAltctl() {
            Log.d(TAG, " onModeChangedAltctl");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_ALTCTL);
        }

        @Override
        public void onModeChangedPosctl() {
            Log.d(TAG, " onModeChangedPosctl");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_POSCTL);
        }

        @Override
        public void onModeChangedAutomission() {
            Log.d(TAG, " onModeChangedAutomission");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTOMISSION);
        }

        @Override
        public void onModeChangedAutoTakeoff() {
            Log.d(TAG, " onModeChangedAutoTakeoff");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTOTAKEOFF);
        }

        @Override
        public void onModeChangedAutoLand() {
            Log.d(TAG, " onModeChangedAutoLand");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTOLAND);
        }

        @Override
        public void onModeChangedAutoRtl() {
            Log.d(TAG, " onModeChangedAutoRtl");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTORTL);
        }

        @Override
        public void onModeChangedSuperSimple() {
            Log.d(TAG, " onModeChangedSuperSimple");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_SUPERSIMPLE);
        }

        @Override
        public void onModeChangedAutoCircle() {
            Log.d(TAG, " onModeChangedAutoCircle");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTOCIRCLE);
        }

        @Override
        public void onModeChangedFollowme() {
            Log.d(TAG, " onModeChangedFollowme");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_FOLLOWME);
        }

        @Override
        public void onModeChangedAutoLoiter() {
            Log.d(TAG, " onModeChangedAutoLoiter");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_AUTOLOITER);
        }

        @Override
        public void onModeChangedTimeout() {
            Log.d(TAG, " onModeChangedTimeout");
            setFlyControlMode(SystemStatusListener.ModeChangedListener.PVSDK_MODE_CHANGED_TIMEOUT);
        }
    };

    SystemStatusListener.BackToLastModeListener mBackToLastModeListener = new SystemStatusListener.BackToLastModeListener() {
        @Override
        public void onBackToLastModeSuccess() {
            Log.d(TAG, " onBackToLastModeSuccess");
        }

        @Override
        public void onBackToLastModeTimeout() {
            Log.d(TAG, " onBackToLastModeTimeout");
        }
    };


    MissionListener.MissionStatusListener mMissionStatusListener = new MissionListener.MissionStatusListener() {
        @Override
        public void onMissionSendTimeout() {
            Log.d(TAG, " onMissionSendTimeout");
        }

        @Override
        public void onMissionCurrent() {
            Log.d(TAG, " onMissionCurrent");
        }

        @Override
        public void onMissionClearSuccess() {
            Log.d(TAG, " onMissionClearSuccess");
        }

        @Override
        public void onMissionSendSuccess() {
            Log.d(TAG, " onMissionSendSuccess");
            //设置航点成功
            wayPointSendSuccess = !wayPointSendSuccess;
            mHandler.sendEmptyMessage(2);
        }

        @Override
        public void onMissionReceiveSuccess() {
            Log.d(TAG, " onMissionReceiveSuccess");
        }

        @Override
        public void onMissionSendFailed() {
            Log.d(TAG, " onMissionSendFailed");
        }
    };

    @Override
    protected void onDestroy() {
        stopPowerSDK();
        super.onDestroy();
    }


    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 0:
                    Attitude attitudeParam = (Attitude) msg.obj;

                    attitudePitch.setText(getString(R.string.attitude_pitch, attitudeParam.pitch / Math.PI * 180));
                    attitudeRoll.setText(getString(R.string.attitude_roll, attitudeParam.roll / Math.PI * 180));
                    attitudeYaw.setText(getString(R.string.attitude_yaw, attitudeParam.yaw / Math.PI * 180));
                    break;
                case 1:
                    GlobalPositionIntParam param = (GlobalPositionIntParam) msg.obj;
                    attitudeVx.setText(getString(R.string.attitude_vx, param.vx / 100));
                    attitudeVy.setText(getString(R.string.attitude_vy, param.vy / 100));
                    attitudeVz.setText(getString(R.string.attitude_vz, param.vz / 100));

                    attitudeAlt.setText(getString(R.string.attitude_alt, param.alt / 1000.0f));
                    attitudeRelativeAlt.setText(getString(R.string.attitude_relative_alt, param.relative_alt / 1000.0f));

                    attitudeRelativeLat.setText(getString(R.string.attitude_lat, param.lat / progressive));//纬度
                    attitudeRelativeLon.setText(getString(R.string.attitude_lon, param.lon / progressive));//经度

                    //点击设置航点参数，缓存当前 经纬度坐标
                    mCurrnetPoint = param;

                    break;
                case 2://航点发送成功
                    Log.d(TAG, "call back  send way ponit Success  ");
                    Toast.makeText(LocationActivity.this, R.string.fly_point_set_sukccess, Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    };

}
