package com.wtx.blestation;

import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager viewPager;
    private List<View> viewList;
    private View view_scaner, view_display, view_history, view_setting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Log.i("Print", "初始化Main视图 !");

        viewPager = (ViewPager) findViewById(R.id.viewpaper);
        LayoutInflater inflater = getLayoutInflater();
        view_scaner = inflater.inflate(R.layout.activity_scaner, null);
        view_display = inflater.inflate(R.layout.activity_display, null);
        view_history = inflater.inflate(R.layout.activity_history, null);
        view_setting = inflater.inflate(R.layout.activity_setting, null);

        viewList = new ArrayList<View>();
        viewList.add(view_scaner);
        viewList.add(view_display);
        viewList.add(view_history);
        viewList.add(view_setting);

        //将当前的Activity实例进行依赖注入
        ScanerView.getController(this);
        DisplayView.getController(this);
        HistoryView.getController(this);

        viewPager.setAdapter(new FlowPagerAdapter(4));
        viewPager.setCurrentItem(2);

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                Log.i("Print", "切换到了新的一页: " + position);
                /**
                 * 第一视图用于扫描附近的蓝牙气象站
                 */
                if (position == 0) {
//                    ScanerView controller = ScanerView.getController(null);
//                    controller.scan_all_dev();
                }
                /**
                 * 第二个视图用于实时接收并显示指定气象站的数据
                 */
                if (position == 1) {
                    DisplayView controller = DisplayView.getController(null);
                    controller.display();
                }
                /**
                 * 第三个视图用于查询历史数据
                 */
                if (position == 2) {
                    HistoryView controller = HistoryView.getController(null);
                    controller.bindEvent();
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });
        //调用切换方法
        viewPager.setCurrentItem(0);
        //-----------------------------------------------------------------------

    }

    /**
     * 关闭APP时关闭蓝牙连接
     */
    @Override
    protected void onDestroy() {
        DisplayView controller = DisplayView.getController(null);
        controller.close();
        Log.i("Print", "蓝牙连接已关闭!");
        super.onDestroy();
    }

    public ViewPager getViewPager() {
        return viewPager;
    }

    public class FlowPagerAdapter extends PagerAdapter {
        private final int ITEM_COUNT;

        public FlowPagerAdapter(int count) {
            ITEM_COUNT = count;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == object;
        }

        @Override
        public int getCount() {
            return ITEM_COUNT;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView(viewList.get(position));
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            container.addView(viewList.get(position));
            return viewList.get(position);
        }
    }
}
