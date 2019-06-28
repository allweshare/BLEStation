package com.wtx.blestation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CalendarContract;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import java.io.UnsupportedEncodingException;
import java.text.DecimalFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class DisplayView {

    private static DisplayView instance = null;
    private BluetoothDevice bleDev;
    private BluetoothGatt currGatt;
    private BluetoothGattService currService;
    private final Activity activity;
    private RecvData recvData = new RecvData();     //当前接收到的数据
    private boolean isConnect;
    private Handler updater = new Handler();
    private Timer updateTimer = new Timer();


    private DecimalFormat df_0 = new DecimalFormat("0.0");
    private DecimalFormat dd_00 = new DecimalFormat("00");
    private static int min_cache = Calendar.getInstance().get(Calendar.MINUTE);

    private TimerTask updateTask = new TimerTask() {

        @Override
        public void run() {
            updater.postDelayed(new Runnable() {
                @Override
                public void run() {
                    try {
                        //温度,检查是否有效
                        TextView txtTemp = (TextView) activity.findViewById(R.id.txt_temp);
                        if (txtTemp != null && recvData.getTemp() >= -40.0f && recvData.getTemp() <= 125.0f) {
                            txtTemp.setText("温度： " + df_0.format(recvData.getTemp()) + " ℃");
                        }
                        //湿度,检查是否有效
                        TextView txtHumd = (TextView) activity.findViewById(R.id.txt_humd);
                        if (txtHumd != null && recvData.getHumid() >= 0.0f && recvData.getHumid() <= 100.0f) {
                            txtHumd.setText("湿度： " + dd_00.format(recvData.getHumid()) + " %RH");
                        }
                        //气压,检查是否有效
                        TextView txtPress = (TextView) activity.findViewById(R.id.txt_press);
                        if (txtPress != null && recvData.getPress() >= 500.0f && recvData.getPress() <= 1500.0f) {
                            txtPress.setText("气压： " + df_0.format(recvData.getPress()) + " hPa");
                        }
                        //瞬时风
                        TextView txtWDirCurr = (TextView) activity.findViewById(R.id.txt_wdir_curr);
                        TextView txtWSpdCurr = (TextView) activity.findViewById(R.id.txt_wspd_curr);
                        if (txtWDirCurr != null && txtWSpdCurr != null && recvData.getWind_curr() != null) {
                            txtWDirCurr.setText("风向： " + recvData.getWind_curr().dir + " °");
                            txtWSpdCurr.setText("风速： " + df_0.format(recvData.getWind_curr().speed) + " m/s");
                        }
                        //一分钟风
                        TextView txtWDir1Min = (TextView) activity.findViewById(R.id.txt_wdir_1min);
                        TextView txtWSpd1Min = (TextView) activity.findViewById(R.id.txt_wspd_1min);
                        if (txtWDir1Min != null && txtWSpd1Min != null && recvData.getWind_1min() != null) {
                            txtWDir1Min.setText("风向： " + recvData.getWind_1min().dir + " °");
                            txtWSpd1Min.setText("风速： " + df_0.format(recvData.getWind_1min().speed) + " m/s");
                        }
                        //十分钟风
                        TextView txtWDir10Min = (TextView) activity.findViewById(R.id.txt_wdir_10min);
                        TextView txtWSpd10Min = (TextView) activity.findViewById(R.id.txt_wspd_10min);
                        if (txtWDir10Min != null && txtWSpd10Min != null && recvData.getWind_10min() != null) {
                            txtWDir10Min.setText("风向： " + recvData.getWind_10min().dir + " °");
                            txtWSpd10Min.setText("风速： " + df_0.format(recvData.getWind_10min().speed) + " m/s");
                        }
                        //雨量
                        TextView txtRain = (TextView) activity.findViewById(R.id.txt_rain);
                        if (txtRain != null && recvData.getRain() >= 0.0f) {
                            txtRain.setText("雨量： " + df_0.format(recvData.getRain()) + " mm");
                        }
                        //设备名称
                        TextView txtDevName = (TextView) activity.findViewById(R.id.txt_dev_name);
                        if (txtDevName != null && bleDev != null && bleDev.getName() != null) {
                            txtDevName.setText("设备名称： " + bleDev.getName());
                        }
                        //设备电压
                        TextView txtDevVtg = (TextView) activity.findViewById(R.id.txt_dev_vtg);
                        if (txtDevVtg != null && recvData.getVoltage() >= 3.3f) {
                            txtDevVtg.setText("设备电压： " + df_0.format(recvData.getVoltage()) + "V");
                        }
                        //---------------------------------------------------------------------------------------
                        if (min_cache != Calendar.getInstance().get(Calendar.MINUTE)) {
                            //存储分钟数据
                            Database dao = Database.getDao(activity.getBaseContext());
                            dao.insert(recvData);
                            Log.i("Print", "分钟数据存储: " + Calendar.getInstance().getTime().toLocaleString());
                        }
                        min_cache = Calendar.getInstance().get(Calendar.MINUTE);
                        //---------------------------------------------------------------------------------------
                    } catch (IllegalArgumentException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }, 100);
        }
    };

    private Runnable dispTask = new Runnable() {
        @Override
        public void run() {
            while (true) {
                try {
                    //Log.i("Print", "读取设备数据!");
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                //-----------------------------------------------------------------------
                if (currService != null && currGatt != null) {
                    int i = 0;
                    for (BluetoothGattCharacteristic charc : currService.getCharacteristics()) {
                        if (currGatt.readCharacteristic(charc)) {
                            //每次读取完一个特征数据必须延迟一段时间
                            try {
                                Thread.sleep(200);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            //读取特征数据
                            try {
                                String charcVal = new String(charc.getValue(), "ISO-8859-1");
                                //Log.i("Print", "读取结果: " + charcVal);
                                if (i == 0) {     //温度、湿度、气压
                                    try {
                                        recvData.setTemp(Float.parseFloat(charcVal.substring(0, 4)) / 10.0f);
                                    } catch (NumberFormatException fe) {
                                        recvData.setTemp(-99.0f);
                                        fe.printStackTrace();
                                    }
                                    try {
                                        recvData.setHumid(Float.parseFloat(charcVal.substring(5, 8)) / 10.0f);
                                    } catch (NumberFormatException fe) {
                                        recvData.setHumid(200.0f);
                                        fe.printStackTrace();
                                    }
                                    try {
                                        recvData.setPress(Float.parseFloat(charcVal.substring(9, 14)) / 10.0f);
                                    } catch (NumberFormatException fe) {
                                        recvData.setPress(0.0f);
                                        fe.printStackTrace();
                                    }
                                }
                                //-------------------------------------------------------
                                if (i == 1) {             //瞬时风
                                    try {
                                        RecvData.WindType windData = new RecvData.WindType();
                                        windData.dir = Integer.parseInt(charcVal.substring(0, 3));
                                        windData.speed = Float.parseFloat(charcVal.substring(4, 7)) / 10.0f;
                                        recvData.setWind_curr(windData);
                                    } catch (NumberFormatException fe) {
                                        recvData.setWind_curr(null);
                                        fe.printStackTrace();
                                    }
                                }
                                if (i == 2) {             //一分钟风
                                    try {
                                        RecvData.WindType windData = new RecvData.WindType();
                                        windData.dir = Integer.parseInt(charcVal.substring(0, 3));
                                        windData.speed = Float.parseFloat(charcVal.substring(4, 7)) / 10.0f;
                                        recvData.setWind_1min(windData);
                                    } catch (NumberFormatException fe) {
                                        recvData.setWind_1min(null);
                                        fe.printStackTrace();
                                    }
                                }
                                if (i == 3) {             //十分钟风
                                    try {
                                        RecvData.WindType windData = new RecvData.WindType();
                                        windData.dir = Integer.parseInt(charcVal.substring(0, 3));
                                        windData.speed = Float.parseFloat(charcVal.substring(4, 7)) / 10.0f;
                                        recvData.setWind_10min(windData);
                                    } catch (NumberFormatException fe) {
                                        recvData.setWind_10min(null);
                                        fe.printStackTrace();
                                    }
                                }
                                if (i == 4) {             //分钟雨量
                                    try {
                                        recvData.setRain(Float.parseFloat(charcVal) / 10.0f);
                                    } catch (NumberFormatException fe) {
                                        recvData.setRain(-1.0f);
                                        fe.printStackTrace();
                                    }
                                }
                                if (i == 5) {             //设备电压
                                    try {
                                        recvData.setVoltage(Float.parseFloat(charcVal.substring(2, 5)) / 10.0f);
                                    } catch (NumberFormatException fe) {
                                        recvData.setVoltage(0.0f);
                                        fe.printStackTrace();
                                    }
                                }
                            } catch (UnsupportedEncodingException e) {
                                e.printStackTrace();
                            }
                            //累计计数
                            i++;
                        }
                    }
                }
            }
        }
    };

    private DisplayView(final Activity act) {
        activity = act;
        //--------------------------------------------------------
        //自动启动
        new Thread(dispTask).start();
        updateTimer.schedule(updateTask, 4000, 2500);
    }

    private BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("Print", "启动服务发现:  " + currGatt.discoverServices());
            }
            super.onConnectionStateChange(gatt, status, newState);
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            Log.i("Print", "发现服务 !  服务数量: " + currGatt.getServices().size());
            if (status == BluetoothGatt.GATT_SUCCESS) {
                isConnect = true;
                //----------------------------------------------------------
                //遍历服务
                for (BluetoothGattService svc : currGatt.getServices()) {
                    if (svc.getUuid().toString().substring(4, 8).equalsIgnoreCase("fff0")) {
                        Log.i("Print", "Find Service: " + svc.getUuid());
                        currService = svc;
                    }
                }
            } else {
                Log.i("Print", "找不到服务 ! ");
            }
        }
    };

    /**
     * 开始读取蓝牙数据并实时显示
     * 这里应该避免重复建立蓝牙链接
     */
    public void display() {
        if (activity != null && bleDev != null) {
            if (currGatt == null) {       //防止重复链接
                currGatt = bleDev.connectGatt(activity.getBaseContext(), false, gattCallback);
                Log.i("Print", "开始绑定!");
            }
        } else {
            Log.i("Print", "没有注入参数!");
        }
    }

    /**
     * 关闭当前Gatt链接
     */
    public void close() {
        if (currGatt != null) {
            currGatt.close();
            currGatt = null;
            currService = null;
            isConnect = false;
            Log.i("Print", "关闭现有蓝牙连接!");
        }
    }

    public static synchronized DisplayView getController(Activity activity) {
        if (instance == null) {
            instance = new DisplayView(activity);
        }
        return instance;
    }

    public BluetoothDevice getBleDev() {
        return bleDev;
    }

    public void setBleDev(BluetoothDevice bleDev) {
        this.bleDev = bleDev;
    }

}
