package com.wtx.blestation;

import android.app.Activity;
import android.util.Log;
import android.view.Gravity;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

public class HistoryView {
    //固定没页显示数据的条目
    private static final int ROW_COUNT = 20;

    private static HistoryView instance = null;
    private final Activity activity;

    private HistoryView(final Activity act) {
        activity = act;
    }

    /**
     * 绑定事件
     * 防止重复绑定
     */
    public void bindEvent() {
        TableLayout table_history = (TableLayout) activity.findViewById(R.id.table_history);
        if(table_history != null){
            if(table_history.getChildCount() == 1){
                for(int i=0;i<ROW_COUNT;i++){
                    //添加一行空记录
                    appendRow(table_history);
                }
            }
            Log.i("Print","table_history.getChildCount() = "+table_history.getChildCount());
        }

    }

    private void appendRow(TableLayout table){
        TableRow curr_row = new TableRow(activity.getBaseContext());
        //时间
        TextView cell_time = new TextView(activity.getBaseContext());
        cell_time.setBackgroundResource(R.drawable.table_cell);
        cell_time.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_time.setText("----");
        curr_row.addView(cell_time);
        //温度
        TextView cell_temp = new TextView(activity.getBaseContext());
        cell_temp.setBackgroundResource(R.drawable.table_cell);
        cell_temp.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_temp.setText("-----");
        curr_row.addView(cell_temp);
        //湿度
        TextView cell_humd = new TextView(activity.getBaseContext());
        cell_humd.setBackgroundResource(R.drawable.table_cell);
        cell_humd.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_humd.setText("----");
        curr_row.addView(cell_humd);
        //气压
        TextView cell_press = new TextView(activity.getBaseContext());
        cell_press.setBackgroundResource(R.drawable.table_cell);
        cell_press.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_press.setText("------");
        curr_row.addView(cell_press);
        //一分钟风
        TextView cell_w1m = new TextView(activity.getBaseContext());
        cell_w1m.setBackgroundResource(R.drawable.table_cell);
        cell_w1m.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_w1m.setText("--- / ---");
        curr_row.addView(cell_w1m);
        //十分钟风
        TextView cell_wtm = new TextView(activity.getBaseContext());
        cell_wtm.setBackgroundResource(R.drawable.table_cell);
        cell_wtm.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_wtm.setText("--- / ---");
        curr_row.addView(cell_wtm);
        //雨量
        TextView cell_rain = new TextView(activity.getBaseContext());
        cell_rain.setBackgroundResource(R.drawable.table_cell);
        cell_rain.setGravity(Gravity.CENTER_HORIZONTAL);
        cell_rain.setText("----");
        curr_row.addView(cell_rain);
        //添加行
        curr_row.setMinimumHeight(30);
        table.addView(curr_row);
    }

    public static synchronized HistoryView getController(Activity activity) {
        if (instance == null) {
            instance = new HistoryView(activity);
        }
        return instance;
    }
}
