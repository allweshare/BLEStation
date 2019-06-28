package com.wtx.blestation;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class ScanerView {
    private static ScanerView instance = null;
    private final Activity activity;
    private BLEDevice cacheBLEDev;      //当前的BLE设备
    private Handler hStart, hStop;
    private ListView stationList;
    private List<BLEDevice> bleDevices = new ArrayList<BLEDevice>();
    private ArrayAdapter<BLEDevice> bleListAdapter;
    private BluetoothAdapter.LeScanCallback onBLECallback = new BluetoothAdapter.LeScanCallback() {
        @Override
        public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
            if (device != null && device.getName() != null) {
                Log.i("Print", "扫描到新的蓝牙设备: " + device.getName() + "; Addr: " + device.getAddress() + "; RSSI: " + rssi);
                //添加找到的设备节点
                BLEDevice dev = new BLEDevice(device, rssi);
                if (bleListAdapter != null && dev != null) {
                    bleListAdapter.add(dev);
                }
            }
        }
    };

    private ScanerView(Activity act) {
        activity = act;
        //----------------------------------------------------------------------
        //这里的代码只会执行一次
        Handler lazyInit = new Handler();
        lazyInit.postDelayed(new Runnable() {
            @Override
            public void run() {
                final Button btnRescan = activity.findViewById(R.id.btn_rescan);
                if (btnRescan != null) {
                    btnRescan.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            btnRescan.setEnabled(false);
                            TextView txtCurrBle = activity.findViewById(R.id.txt_curr_ble);
                            txtCurrBle.setText("当前连接: ///");
                            DisplayView controller = DisplayView.getController(null);
                            controller.close();
                            scan_all_dev();
                        }
                    });
                }
            }
        }, 3000);
    }

    /**
     * 开始扫描低功耗设备
     */
    public void scan_all_dev() {
        //扫描时先确保所有蓝牙链接已经断开
        if (activity == null) {
            Log.i("Print", "没有传入Activity !");
        }
        //----------------------------------------------------------------------
        //先检查当前的软件和硬件环境是否支持低功耗蓝牙
        if (!activity.getBaseContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(activity.getBaseContext(), R.string.not_support, Toast.LENGTH_SHORT).show();
        } else {
            Log.i("Print", "Support BLE 4.0 !");
        }
        //----------------------------------------------------------------------
        //初始化蓝牙适配器
        BluetoothManager bluetoothManager =
                (BluetoothManager) activity.getBaseContext().getSystemService(Context.BLUETOOTH_SERVICE);
        final BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();
        //----------------------------------------------------------------------
        //检查用户据是否启用蓝牙功能
        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {
            Intent enableBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            //如果不在Activity中调用startActivity()方法必须向Intent传入FLAG_ACTIVITY_NEW_TASK
            enableBluetooth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            activity.getBaseContext().startActivity(enableBluetooth);
        } else {
            Log.i("Print", "Bluetooth is ready !");
        }
        //----------------------------------------------------------------------
        //检索BLE设备
        hStart = new Handler();
        hStart.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Print", "Scaner Start !");
                bleListAdapter = new ArrayAdapter<BLEDevice>(activity.getBaseContext(), android.R.layout.simple_expandable_list_item_1, bleDevices);
                stationList = (ListView) activity.findViewById(R.id.list_sta);
                stationList.setAdapter(bleListAdapter);
                stationList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        MainActivity mainActivity = (MainActivity) activity;
                        DisplayView displayView = DisplayView.getController(null);
                        //向第二个视图传递蓝牙的设备对象
                        cacheBLEDev = bleListAdapter.getItem(position);
                        displayView.setBleDev(cacheBLEDev.device);
                        TextView txtCurrBle = activity.findViewById(R.id.txt_curr_ble);
                        txtCurrBle.setText("当前连接: " + cacheBLEDev.device.getName());
                        //使用平滑滚动的效果切换至第二个视图
                        mainActivity.getViewPager().setCurrentItem(1, true);
                    }
                });

                bleDevices.clear();
                bluetoothAdapter.startLeScan(onBLECallback);

                Button btnRescan = activity.findViewById(R.id.btn_rescan);
                if (btnRescan != null) {
                    btnRescan.setEnabled(false);
                }
            }
        }, 1000);
        hStop = new Handler();
        hStop.postDelayed(new Runnable() {
            @Override
            public void run() {
                Log.i("Print", "Scaner Stop !");
                bluetoothAdapter.stopLeScan(onBLECallback);
                Button btnRescan = activity.findViewById(R.id.btn_rescan);
                if (btnRescan != null) {
                    btnRescan.setEnabled(true);
                }
            }
        }, 5000);
    }

    public static synchronized ScanerView getController(Activity activity) {
        if (instance == null) {
            instance = new ScanerView(activity);
        }
        return instance;
    }

    public class BLEDevice {
        private BluetoothDevice device;
        private int rssi;

        public BLEDevice(BluetoothDevice dev, int rssi) {
            this.device = dev;
            this.rssi = rssi;
        }

        @Override
        public String toString() {
            return "NAME: " + this.device.getName() + "\t\tRSSI: " + rssi;
        }
    }
}
