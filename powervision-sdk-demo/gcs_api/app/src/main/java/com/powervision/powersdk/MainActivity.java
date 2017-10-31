package com.powervision.powersdk;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.powervision.powersdk.core.PowerSDK;
import com.powervision.powersdk.factory.ConnectIpAndPortFactory;
import com.powervision.powersdk.factory.MountDeviceInterfaceFactory;
import com.powervision.powersdk.listener.ConnectListener;
import com.powervision.powersdk.listener.MountDeviceListener;
import com.powervision.powersdk.param.MountData;
import com.powervision.powersdk.param.mount.MountApiCanData;
import com.powervision.powersdk.param.mount.MountApiCanFilterParam;
import com.powervision.powersdk.utils.StandardDialogUtils;
import com.powervision.powersdk.utils.ToastUtil;
import com.tencent.bugly.crashreport.CrashReport;

import java.nio.charset.Charset;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    @BindView(R.id.connect_btn)
    Button connectBtn;
    @BindView(R.id.status_text)
    TextView statusText;
    @BindView(R.id.read_btn)
    Button readBtn;
    @BindView(R.id.clear_btn)
    Button clearBtn;
    @BindView(R.id.receive_content)
    TextView receiveContent;
    @BindView(R.id.send_btn)
    Button sendBtn;
    @BindView(R.id.send_content)
    EditText sendContent;
    @BindView(R.id.stop)
    Button stop;
    @BindView(R.id.port_spinner)
    Spinner portSpinner;
    @BindView(R.id.uart_bps_spinner)
    Spinner uartBpsSpinner;
    @BindView(R.id.uart_parity_spinner)
    Spinner uartParitySpinner;
    @BindView(R.id.uart_flowCtrl_spinner)
    Spinner uartFlowCtrlSpinner;
    @BindView(R.id.uart_layout)
    LinearLayout uartLayout;
    @BindView(R.id.can_bps_spinner)
    Spinner canBpsSpinner;
    @BindView(R.id.can_mode_spinner)
    Spinner canModeSpinner;
    @BindView(R.id.can_layout)
    LinearLayout canLayout;
    @BindView(R.id.i2c_mode_spinner)
    Spinner i2cModeSpinner;
    @BindView(R.id.i2c_speed_spinner)
    Spinner i2cSpeedSpinner;
    @BindView(R.id.i2c_mac_address)
    EditText i2cMacAddress;
    @BindView(R.id.i2c_layout)
    LinearLayout i2cLayout;
    @BindView(R.id.spi_mode_spinner)
    Spinner spiModeSpinner;
    @BindView(R.id.spi_speed_spinner)
    Spinner spiSpeedSpinner;
    @BindView(R.id.spi_effect_bit_spinner)
    Spinner spiEffectBitSpinner;
    @BindView(R.id.spi_data_size_spinner)
    Spinner spiDataSizeSpinner;
    @BindView(R.id.spi_crc_spinner)
    Spinner spiCrcSpinner;
    @BindView(R.id.spi_layout)
    LinearLayout spiLayout;
    @BindView(R.id.search_btn)
    Button searchBtn;
    @BindView(R.id.set_btn)
    Button setBtn;
    @BindView(R.id.uart_stopBits_spinner)
    Spinner uartStopBitsSpinner;
    @BindView(R.id.uart_dataBits_spinner)
    Spinner uartDataBitsSpinner;
    @BindView(R.id.set_port_btn)
    Button setPortBtn;
    @BindView(R.id.gpio_num_spinner)
    Spinner gpioNumSpinner;
    @BindView(R.id.gpio_low_last_time)
    EditText gpioLowLastTime;
    @BindView(R.id.gpio_periodRatio)
    EditText gpioPeriodRatio;
    @BindView(R.id.gpio_high_last_time)
    EditText gpioHighLastTime;
    @BindView(R.id.gpio_layout)
    LinearLayout gpioLayout;
    @BindView(R.id.can_filter_layout)
    LinearLayout canFilterLayout;
    @BindView(R.id.can_filter_serial_number)
    EditText canFilterSerialNumber;
    @BindView(R.id.can_filter_mode)
    EditText canFilterMode;
    @BindView(R.id.can_filter_match_id)
    EditText canFilterMatchId;
    @BindView(R.id.can_filter_hide_id)
    EditText canFilterHideId;
    @BindView(R.id.can_filter_id_type)
    EditText canFilterIdType;
    @BindView(R.id.can_filter_frame_type)
    EditText canFilterFrameType;
    @BindView(R.id.can_filter_enable)
    EditText canFilterEnable;
    @BindView(R.id.can_filter_delete_btn)
    Button canFilterDeleteBtn;
    @BindView(R.id.can_filter_delete_text)
    EditText canFilterDeleteText;

    private PowerSDK mPowerSDK;
    //1:UART 2:CAN 3:I2C 4:SPI
    private int currentPort = 0;

    private int currentUartBps = 0;
    private int currentUartDataBits = 0;
    private int currentUartStopBits = 0;
    private int currentUartParity = 0;
    private int currentUartFlowCtrl = 0;

    private int currentCanBps = 0;
    private int currentCanMode = 0;

    private int currentI2cMode = 0;
    private int currentI2cSpeed = 0;
    private int currentI2cAddress = 0;

    private int currentSpiBps = 0;
    private int currentSpiMode = 0;
    private int currentSpiFirstBit = 0;
    private int currentSpiDataSize = 0;
    private int currentSpiCrcEnable = 0;

    private int currentGpioDeviceNum = 0;

    //0,未连接；1，链路已连接；2，设备已连接；3，设备已断开；4，链路已断开
    private int mConnectStatus = 0;

    private int uartPortStatus = 0;
    private int canPortStatus = 0;
    private int i2cPortStatus = 0;
    private int spiPortStatus = 0;
    private int gpio1PortStatus = 0;
    private int gpio2PortStatus = 0;
    private int gpio3PortStatus = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        Log.e(TAG, "onCreate: ");

        CrashReport.initCrashReport(getApplicationContext(), "320a6b6bc5", true);

        initClickListener();

        initPortListener();
        initUartListener();
        initCanListener();
        initI2cListener();
        initSpiListener();
        initGpioListener();


        mPowerSDK = PowerSDK.getInstance();
        mPowerSDK.addConnectListener(simpleConnectListener);
        mPowerSDK.addMountDeviceStateListener(mountDeviceStateListener);

        mPowerSDK.addUartListener(uartListener);
        mPowerSDK.addCanListener(canListener);
        mPowerSDK.addI2cListener(i2cListener);
        mPowerSDK.addSpiListener(spiListener);
        mPowerSDK.addGpioListener(gpioListener);

        mPowerSDK.addInquireSoftVersionParamListener(inquireSoftVersionParamListener);
        mPowerSDK.addInquireHardVersionParamListener(inquireHardVersionParamListener);
        mPowerSDK.addCanFilterListener(canFilterListener);
    }

    private void initClickListener() {
        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPowerSDK();
            }
        });

        setBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectStatus != 2) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentPort == 0) {
                    setUartParam();
                } else if (currentPort == 1) {
                    setCanParam();
                } else if (currentPort == 2) {
                    setI2cParam();
                } else if (currentPort == 3) {
                    setSpiParam();
                } else if (currentPort == 4) {
                    setGpioParam();
                } else if (currentPort == 5) {
                    setCanFilterParam();
                }
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectStatus != 2) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentPort == 2) {
                    mPowerSDK.readI2cData(6, 0, 1);
                } else if (currentPort == 3) {
                    mPowerSDK.readSpiData(0, 3);
                }
            }
        });

        clearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                receiveContent.setText("");
            }
        });

        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectStatus != 2) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (currentPort == 0) {
                    mPowerSDK.queryUartParam();
                } else if (currentPort == 1) {
                    mPowerSDK.queryCanParam();
                } else if (currentPort == 2) {
                    mPowerSDK.queryI2cParam();
                } else if (currentPort == 3) {
                    mPowerSDK.querySpiParam();
                } else if (currentPort == 4) {
                    mPowerSDK.queryGpioParam(currentGpioDeviceNum);
                } else if (currentPort == 5) {
                    mPowerSDK.queryCanFilterParam();
                }
            }
        });

//        supportRequestWindowFeature()
        sendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectStatus != 2) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }

                String data = sendContent.getText().toString().trim();

                if (currentPort == 0) {
                    sendDataToUART(data);
                    Log.e(TAG, "send uart: " + data);
                } else if (currentPort == 1) {
                    sendDataToCAN(data);
                    Log.e(TAG, "send can: " + data);
                } else if (currentPort == 2) {
                    sendDataToI2C(data);
                    Log.e(TAG, "send i2c: " + data);
                } else if (currentPort == 3) {
                    sendDataToSPI(data);
                    Log.e(TAG, "send spi: " + data);
                } else if (currentPort == 4) {

                } else if (currentPort == 5) {

                }
            }
        });

        setPortBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mConnectStatus != 2) {
                    Toast.makeText(MainActivity.this, "设备未连接", Toast.LENGTH_SHORT).show();
                    return;
                }

                mPowerSDK.queryAllDevicePort();
            }
        });


    }

    private void initPortListener() {
        portSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "position: " + position);
                currentPort = position;
                changePort();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.e(TAG, "onNothingSelected: ");
            }
        });
    }

    private void initUartListener() {
        uartBpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentUartBps = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uartDataBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentUartDataBits = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uartStopBitsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentUartStopBits = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uartFlowCtrlSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentUartFlowCtrl = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        uartParitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentUartParity = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initCanListener() {
        canBpsSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCanBps = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        canModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentCanMode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initI2cListener() {
        i2cModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                currentI2cMode = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        i2cSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                currentI2cSpeed = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initSpiListener() {
        spiSpeedSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpiBps = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spiModeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpiMode = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spiDataSizeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpiDataSize = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spiEffectBitSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpiFirstBit = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        spiCrcSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentSpiCrcEnable = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    private void initGpioListener() {
        gpioNumSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                currentGpioDeviceNum = i;
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        canFilterDeleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                deleteGpioParam();
            }
        });
    }

    private void changePort() {
        switch (currentPort) {
            case 0:
                uartLayout.setVisibility(View.VISIBLE);
                canLayout.setVisibility(View.GONE);
                i2cLayout.setVisibility(View.GONE);
                spiLayout.setVisibility(View.GONE);
                gpioLayout.setVisibility(View.GONE);
                canFilterLayout.setVisibility(View.GONE);
                break;
            case 1:
                uartLayout.setVisibility(View.GONE);
                canLayout.setVisibility(View.VISIBLE);
                i2cLayout.setVisibility(View.GONE);
                spiLayout.setVisibility(View.GONE);
                gpioLayout.setVisibility(View.GONE);
                canFilterLayout.setVisibility(View.GONE);
                break;
            case 2:
                uartLayout.setVisibility(View.GONE);
                canLayout.setVisibility(View.GONE);
                i2cLayout.setVisibility(View.VISIBLE);
                spiLayout.setVisibility(View.GONE);
                gpioLayout.setVisibility(View.GONE);
                canFilterLayout.setVisibility(View.GONE);
                break;
            case 3:
                uartLayout.setVisibility(View.GONE);
                canLayout.setVisibility(View.GONE);
                i2cLayout.setVisibility(View.GONE);
                spiLayout.setVisibility(View.VISIBLE);
                gpioLayout.setVisibility(View.GONE);
                canFilterLayout.setVisibility(View.GONE);
                break;
            case 4:
                uartLayout.setVisibility(View.GONE);
                canLayout.setVisibility(View.GONE);
                i2cLayout.setVisibility(View.GONE);
                spiLayout.setVisibility(View.GONE);
                gpioLayout.setVisibility(View.VISIBLE);
                canFilterLayout.setVisibility(View.GONE);
                break;
            case 5:
                uartLayout.setVisibility(View.GONE);
                canLayout.setVisibility(View.GONE);
                i2cLayout.setVisibility(View.GONE);
                spiLayout.setVisibility(View.GONE);
                gpioLayout.setVisibility(View.GONE);
                canFilterLayout.setVisibility(View.VISIBLE);
                break;
        }

    }

    private void setUartParam() {
        mPowerSDK.setUartParam(currentUartBps, currentUartDataBits, currentUartStopBits, currentUartParity, currentUartFlowCtrl);
    }

    private void setCanParam() {
        mPowerSDK.setCanParam(currentCanBps, currentCanMode);
    }

    private void setI2cParam() {
        if (i2cMacAddress.length() == 0) {
            ToastUtil.showToast("地址不能为空");
            return;
        }
        String address = i2cMacAddress.getText().toString().trim();
        int addressInt = Integer.parseInt(address);
        mPowerSDK.setI2cParam(currentI2cSpeed, currentCanMode, addressInt);
    }

    private void setSpiParam() {
        mPowerSDK.setSpiParam(currentSpiBps, currentSpiMode, currentSpiFirstBit, currentSpiDataSize, currentSpiCrcEnable);
    }

    private void setGpioParam() {
//        mPowerSDK.setSpiParam(currentSpiBps, currentSpiMode, currentSpiFirstBit, currentSpiDataSize, currentSpiCrcEnable);
        String peridRadio = gpioPeriodRatio.getText().toString().trim();
        String lowLastTime = gpioLowLastTime.getText().toString().trim();
        String highLastTime = gpioHighLastTime.getText().toString().trim();

        if (peridRadio.length() == 0 || lowLastTime.length() == 0 || highLastTime.length() == 0) {
            ToastUtil.showToast("内容不能为空");
            return;
        }

        int peridRadioInt = Integer.parseInt(peridRadio);
        int lowLastTimeInt = Integer.parseInt(lowLastTime);
        int highLastTimeInt = Integer.parseInt(highLastTime);
        mPowerSDK.setGpioParam(currentGpioDeviceNum, peridRadioInt, lowLastTimeInt, highLastTimeInt);
    }

    private void setCanFilterParam() {
        String number = canFilterSerialNumber.getText().toString().trim();
        String mode = canFilterMode.getText().toString().trim();
        String matchId = canFilterMatchId.getText().toString().trim();
        String hideId = canFilterHideId.getText().toString().trim();
        String idType = canFilterIdType.getText().toString().trim();
        String frameType = canFilterFrameType.getText().toString().trim();
        String filterEnable = canFilterEnable.getText().toString().trim();

        int numberInt = 0;
        int modeInt = 0;
        int matchIdInt = 0;
        int hideIdInt = 0;
        int idTypeInt = 0;
        int frameTypeInt = 0;
        int filterEnableInt = 1;

        if (number.length() != 0) {
            numberInt = Integer.parseInt(number);
        }

        if (mode.length() != 0) {
            modeInt = Integer.parseInt(mode);
        }

        if (matchId.length() != 0) {
            matchIdInt = Integer.parseInt(matchId);
        }

        if (hideId.length() != 0) {
            hideIdInt = Integer.parseInt(hideId);
        }

        if (idType.length() != 0) {
            idTypeInt = Integer.parseInt(idType);
        }

        if (frameType.length() != 0) {
            frameTypeInt = Integer.parseInt(frameType);
        }

        if (filterEnable.length() != 0) {
            filterEnableInt = Integer.parseInt(filterEnable);
        }

        mPowerSDK.setCanFilterParam(numberInt, modeInt, matchIdInt, hideIdInt, idTypeInt, frameTypeInt, filterEnableInt);
    }

    private void deleteGpioParam() {
        String numberString = canFilterDeleteText.getText().toString().trim();
        if (numberString.length() == 0) {
            ToastUtil.showToast("内容不能为空");
            return;
        }

        int number = Integer.parseInt(numberString);

        mPowerSDK.deleteCanFilterParam(number);
    }

    private void startPowerSDK() {
        mPowerSDK.startConnectSDK(ConnectIpAndPortFactory.getEggConnectIpAndPortFactory());
        mPowerSDK.startRegisterConnectCallback();
        mPowerSDK.startRegisterMountCallback();
        mPowerSDK.startConnectChain();
    }

    private void stopPowerSDK() {
        mPowerSDK.startDisconnectDevice();
        mPowerSDK.startDisconnectChain();
        mPowerSDK.startDisconnectSDK();
        mPowerSDK.removeConnectListener(simpleConnectListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopPowerSDK();
    }

    ConnectListener.SimpleConnectListener simpleConnectListener = new ConnectListener.SimpleConnectListener() {

        @Override
        public void onChainConnected() {
            super.onChainConnected();
            mConnectStatus = 1;
            Log.e(TAG, "onChainConnected");
            mPowerSDK.startConnectDevice();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("链路已连接");
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.connecting));
                }
            });
        }

        @Override
        public void onDroneConnected() {
            super.onDroneConnected();
            Log.e(TAG, "onDroneConnected");
            mConnectStatus = 2;
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    statusText.setText("设备已连接");
                    statusText.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.black));
                }
            });
        }

        @Override
        public void onDeviceDisconnected() {
            super.onDeviceDisconnected();
            Log.e(TAG, "onDeviceDisconnected");
            mConnectStatus = 3;
        }

        @Override
        public void onChainDisconnected() {
            super.onChainDisconnected();
            Log.e(TAG, "onChainDisconnected");
            mConnectStatus = 4;
        }
    };


    MountDeviceListener.MountDeviceStateListener mountDeviceStateListener = new MountDeviceListener.MountDeviceStateListener() {
        @Override
        public void mountState(final int uart, final int can, final int i2c, final int spi, final int gpio1, final int gpio2, final int gpio3) {
            Log.e(TAG, "mountState uart: " + uart);
            Log.e(TAG, "mountState can: " + can);
            Log.e(TAG, "mountState i2c: " + i2c);
            Log.e(TAG, "mountState spi: " + spi);
            Log.e(TAG, "mountState gpio: " + gpio1);
            Log.e(TAG, "mountState gpio2: " + gpio2);
            Log.e(TAG, "mountState gpio3: " + gpio3);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    showPortDialog(uart, can, i2c, spi, gpio1, gpio2, gpio3);
                }
            });
        }

    };

    MountDeviceListener.CanFilterListener canFilterListener = new MountDeviceListener.CanFilterListener() {
        @Override
        public void canFilter(final MountApiCanFilterParam[] params) {
            Log.e(TAG, "canFilter: " + new Gson().toJson(params));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final StringBuilder sb = new StringBuilder();
                    for (MountApiCanFilterParam param : params) {
                        sb.append(new Gson().toJson(param));
                        sb.append("\n");
                    }
                    StandardDialogUtils.defaultDialog(MainActivity.this, sb.toString());
                }
            });
        }
    };

    MountDeviceListener.UartListener uartListener = new MountDeviceListener.UartListener() {
        @Override
        public void onUartParamSet(final int state, final int error) {
            Log.e(TAG, "onUartParamSet state: " + state);
            Log.e(TAG, "onUartParamSet error: " + error);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onUartParamInquire(final int bps, final int dataBits, final int stopBits, final int parity, final int flowCtrl) {
            Log.e(TAG, "onUartParamInquire bps: " + bps);
            Log.e(TAG, "onUartParamInquire dataBits: " + dataBits);
            Log.e(TAG, "onUartParamInquire stopBits: " + stopBits);
            Log.e(TAG, "onUartParamInquire parity: " + parity);
            Log.e(TAG, "onUartParamInquire flowCtrl: " + flowCtrl);




            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    final String[] uartBps = getResources().getStringArray(R.array.uart_bps);
                    final String[] uartStopBits = getResources().getStringArray(R.array.uart_stopBits);
                    final String[] uartControl = getResources().getStringArray(R.array.uart_flowCtrl);
                    final String[] uartBits = getResources().getStringArray(R.array.uart_dataBits);
                    final String[] canCheck = getResources().getStringArray(R.array.uart_parity);

                    StandardDialogUtils.defaultDialog(MainActivity.this,
                            "波特率：" + uartBps[bps] + "\n 停止位数：" + uartStopBits[stopBits] + "\n 流控：" + uartControl[flowCtrl] +
                                    "\n 数据位数：" + uartBits[dataBits] + "\n 校验：" + canCheck[parity]);
                }
            });
        }

        @Override
        public void onUartDataAskSend(final int state, final int error) {
            Log.e(TAG, "uart send type" + state);
            Log.e(TAG, "uart send error" + error);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onUartDataReceive(final MountData data) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receiveContent.setText("UART:::::::" + data.data);
                    StandardDialogUtils.defaultDialog(MainActivity.this, "UART:" + new Gson().toJson(data));
                }
            });
        }
    };



    MountDeviceListener.CanListener canListener = new MountDeviceListener.CanListener() {
        @Override
        public void onCanParamSet(final int state, final int error) {
            Log.e(TAG, "onCanParamSet state: " + state);
            Log.e(TAG, "onCanParamSet error: " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onCanParamInquire(final int bps, final int mode) {
            Log.e(TAG, "onCanParamInquire mode:" + mode);
            Log.e(TAG, "onCanParamInquire bps:" + bps);




            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String[] canBps = getResources().getStringArray(R.array.can_bps);
                    final String[] canModes = getResources().getStringArray(R.array.can_mode);
                    StandardDialogUtils.defaultDialog(MainActivity.this, "波特率：" + canBps[bps] + "\n 模式：" + canModes[mode]);
                }
            });
        }

        @Override
        public void onCanDataAskSend(final int state, final int error) {
            Log.e(TAG, "can send type" + state);
            Log.e(TAG, "can send error" + error);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onCanDataReceive(final MountApiCanData data) {
            Log.e(TAG, "CAN:::::::" + new Gson().toJson(data));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    StandardDialogUtils.defaultDialog(MainActivity.this, "CAN:" + new Gson().toJson(data));
                }
            });
        }
    };

    MountDeviceListener.I2cListener i2cListener = new MountDeviceListener.I2cListener() {
        @Override
        public void onI2cParamSet(final int state, final int error) {
            Log.e(TAG, "onI2cParamSet state: " + state);
            Log.e(TAG, "onI2cParamSet error: " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onI2cParamInquire(final int bps, final int mode, final int deviceAddr) {
            Log.e(TAG, "onI2cParamInquire mode:" + mode);
            Log.e(TAG, "onI2cParamInquire bps:" + bps);
            Log.e(TAG, "onI2cParamInquire deviceAddr:" + deviceAddr);



            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String[] i2cBps = getResources().getStringArray(R.array.i2c_bps);
                    final String[] i2cModes = getResources().getStringArray(R.array.i2c_mode);
                    StandardDialogUtils.defaultDialog(MainActivity.this, "波特率：" + i2cBps[bps] + "\n 模式：" + i2cModes[mode] + "\n 地址：" + deviceAddr);
                }
            });
        }

        @Override
        public void onI2cDataAskSend(final int state, final int error) {
            Log.e(TAG, "i2c send type" + state);
            Log.e(TAG, "i2c send error" + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onI2cDataRead(final MountData data) {
            Log.e(TAG, "onI2cDataRead: " + new Gson().toJson(data));

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receiveContent.setText("I2C:::::::" + new Gson().toJson(data));
                    StandardDialogUtils.defaultDialog(MainActivity.this, "读取I2C:" + new Gson().toJson(data));
                }
            });
        }
    };

    MountDeviceListener.SpiListener spiListener = new MountDeviceListener.SpiListener() {
        @Override
        public void onSpiParamSet(final int state, final int error) {
            Log.e(TAG, "onSpiParamSet state: " + state);
            Log.e(TAG, "onSpiParamSet error: " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onSpiParamInquire(final int bps, final int mode, final int firstBit, final int dataSize, final int crcEnable) {
            Log.e(TAG, "onSpiParamInquire bps:" + bps);
            Log.e(TAG, "onSpiParamInquire mode:" + mode);
            Log.e(TAG, "onSpiParamInquire firstBit:" + firstBit);
            Log.e(TAG, "onSpiParamInquire dataSize:" + dataSize);
            Log.e(TAG, "onSpiParamInquire crcEnable:" + crcEnable);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String[] spiBps = getResources().getStringArray(R.array.spi_bps);
                    final String[] spiModes = getResources().getStringArray(R.array.spi_mode);
                    final String[] spi_firstBit = getResources().getStringArray(R.array.spi_firstBit);
                    final String[] spi_dataSize = getResources().getStringArray(R.array.spi_dataSize);
                    final String[] spi_crcEnable = getResources().getStringArray(R.array.spi_crcEnable);

                    StandardDialogUtils.defaultDialog(MainActivity.this,
                            "模式：" + spiModes[mode] + "\n 速率：" + spiBps[bps] + "\n 有效位先发：" + spi_firstBit[firstBit] +
                                    "\n 数据大小：" + spi_dataSize[dataSize] + "\n CRC使能：" + spi_crcEnable[crcEnable]);
                }
            });
        }

        @Override
        public void onSpiDataAskSend(final int state, final int error) {
            Log.e(TAG, "spi send type" + state);
            Log.e(TAG, "spi send error" + error);

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }


        @Override
        public void onSpiDataRead(final MountData data) {
            Log.e(TAG, "onSpiDataRead: " + new Gson().toJson(data));
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    receiveContent.setText("SPI:::::::" + new Gson().toJson(data));
                    StandardDialogUtils.defaultDialog(MainActivity.this, "读取SPI:" + new Gson().toJson(data));
                }
            });
        }
    };


    MountDeviceListener.GpioListener gpioListener = new MountDeviceListener.GpioListener() {
        @Override
        public void onGpioParamSet(final int state, final int error) {
            Log.e(TAG, "onGpioParamSet state: " + state);
            Log.e(TAG, "onGpioParamSet error: " + error);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (state == 0) {
                        ToastUtil.showToast("成功： " + error);
                    } else {
                        ToastUtil.showToast("失败： " + error);
                    }
                }
            });
        }

        @Override
        public void onGpioParamInquire(final int deviceNumber, final int periodRatio, final int periodLow, final int periodHigh) {
            Log.e(TAG, "onGpioParamInquire deviceNumber:" + deviceNumber);
            Log.e(TAG, "onGpioParamInquire periodRatio:" + periodRatio);
            Log.e(TAG, "onGpioParamInquire periodLow:" + periodLow);
            Log.e(TAG, "onGpioParamInquire periodHigh:" + periodHigh);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    final String[] deviceNum = getResources().getStringArray(R.array.gpio_num);
                    StandardDialogUtils.defaultDialog(MainActivity.this,
                            "GPIO设备号：" + deviceNum[deviceNumber] + "\n 周期值分辨率：" + periodRatio + "\n 低电平持续时间：" + periodLow +
                                    "\n 高电平持续时间：" + periodHigh);
                }
            });
        }
    };


    MountDeviceListener.InquireHardVersionParamListener inquireHardVersionParamListener = new MountDeviceListener.InquireHardVersionParamListener() {
        @Override
        public void onHardVersionParamInquire(int state, int error) {
            Log.e(TAG, "uart send state" + state);
            Log.e(TAG, "uart send error" + error);
        }
    };

    MountDeviceListener.InquireSoftVersionParamListener inquireSoftVersionParamListener = new MountDeviceListener.InquireSoftVersionParamListener() {
        @Override
        public void onSoftVersionParamInquire(int state, int error) {
            Log.e(TAG, "uart send state" + state);
            Log.e(TAG, "uart send error" + error);
        }
    };


    //重写旋转时方法，不销毁activity
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void showPortDialog(final int uart, final int can, final int i2c, final int spi, final int gpio1, final int gpio2, final int gpio3) {

        uartPortStatus = uart;
        canPortStatus = can;
        i2cPortStatus = i2c;
        spiPortStatus = spi;
        gpio1PortStatus = gpio1;
        gpio2PortStatus = gpio2;
        gpio3PortStatus = gpio3;


        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        View view = LayoutInflater.from(this).inflate(R.layout.port_layout, null);
        builder.setView(view);
        builder.setTitle("当前端口状态");
        builder.setPositiveButton(R.string.text_sure, null);
        final AlertDialog dialog = builder.create();
        dialog.show();


        TextView uartStatusText = view.findViewById(R.id.uart_status_text);
        TextView canStatusText = view.findViewById(R.id.can_status_text);
        TextView i2cStatusText = view.findViewById(R.id.i2c_status_text);
        TextView spiStatusText = view.findViewById(R.id.spi_status_text);
        TextView gpio1StatusText = view.findViewById(R.id.gpio1_status_text);
        TextView gpio2StatusText = view.findViewById(R.id.gpio2_status_text);
        TextView gpio3StatusText = view.findViewById(R.id.gpio3_status_text);

        TextView uartOperateText = view.findViewById(R.id.uart_operate_text);
        TextView canOperateText = view.findViewById(R.id.can_operate_text);
        TextView i2cOperateText = view.findViewById(R.id.i2c_operate_text);
        TextView spiOperateText = view.findViewById(R.id.spi_operate_text);
        TextView gpio1OperateText = view.findViewById(R.id.gpio1_operate_text);
        TextView gpio2OperateText = view.findViewById(R.id.gpio2_operate_text);
        TextView gpio3OperateText = view.findViewById(R.id.gpio3_operate_text);


        if (uart == 0) {
            uartStatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            uartOperateText.setText(R.string.mount);
        } else {
            uartStatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            uartOperateText.setText(R.string.umount);
        }

        uartOperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uart == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getUartMountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getUartMountDeviceInterface());
                }

                dialog.dismiss();

            }
        });

        if (can == 0) {
            canStatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            canOperateText.setText(R.string.mount);
        } else {
            canStatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            canOperateText.setText(R.string.umount);
        }

        canOperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (can == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getCanMountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getCanMountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

        if (i2c == 0) {
            i2cStatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            i2cOperateText.setText(R.string.mount);
        } else {
            i2cStatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            i2cOperateText.setText(R.string.umount);
        }

        i2cOperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (i2c == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getI2cMountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getI2cMountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

        if (spi == 0) {
            spiStatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            spiOperateText.setText(R.string.mount);
        } else {
            spiStatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            spiOperateText.setText(R.string.umount);
        }

        spiOperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (spi == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getSpiMountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getSpiMountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

        if (gpio1 == 0) {
            gpio1StatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            gpio1OperateText.setText(R.string.mount);
        } else {
            gpio1StatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            gpio1OperateText.setText(R.string.umount);
        }

        gpio1OperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpio1 == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getGpio1MountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getGpio1MountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

        if (gpio2 == 0) {
            gpio2StatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            gpio2OperateText.setText(R.string.mount);
        } else {
            gpio2StatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            gpio2OperateText.setText(R.string.umount);
        }

        gpio2OperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpio2 == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getGpio2MountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getGpio2MountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

        if (gpio3 == 0) {
            gpio3StatusText.setTextColor(ContextCompat.getColor(this, R.color.red));
            gpio3OperateText.setText(R.string.mount);
        } else {
            gpio3StatusText.setTextColor(ContextCompat.getColor(this, R.color.connecting));
            gpio3OperateText.setText(R.string.umount);
        }

        gpio3OperateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (gpio3 == 0) {
                    mPowerSDK.mountDeviceInterface(MountDeviceInterfaceFactory.getGpio3MountDeviceInterface());
                } else {
                    mPowerSDK.umountDeviceInterface(MountDeviceInterfaceFactory.getGpio3MountDeviceInterface());
                }
                dialog.dismiss();
            }
        });

    }


    /**
     * 发送不加密的数据到Uart
     *
     * @param data
     */
    public void sendDataToUART(String data) {
        mPowerSDK.sendDataToUART(data.getBytes(Charset.defaultCharset()), data.getBytes(Charset.defaultCharset()).length, 0);
    }


    /**
     * 发送不加密的数据到CAN
     *
     * @param data
     */
    public void sendDataToCAN(String data) {
        mPowerSDK.sendDataToCAN(0, 0, 0, data.getBytes(Charset.defaultCharset()), data.getBytes(Charset.defaultCharset()).length, 0);
    }

    /**
     * 发送不加密的数据到I2C
     *
     * @param data
     */
    public void sendDataToI2C(String data) {
        mPowerSDK.sendDataToI2C(123, 0, data.getBytes(Charset.defaultCharset()), data.getBytes(Charset.defaultCharset()).length, 0);
    }

    /**
     * 发送不加密的数据到SPI
     *
     * @param data
     */
    public void sendDataToSPI(String data) {
        mPowerSDK.sendDataToSPI(data.getBytes(Charset.defaultCharset()), data.getBytes(Charset.defaultCharset()).length, 0);
    }
}
