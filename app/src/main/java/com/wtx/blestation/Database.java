package com.wtx.blestation;

import android.content.Context;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.IOException;

public class Database {

    private Context context;
    private volatile static Database dao;
    private File fileDB = null;

    private static final String DB_DIR = "PTB_Station";
    private static final String DB_NAME = "mdata.db";

    private static final String sql_create_table = "create table mdatas (" +
                    "[DTIME] datetime primary key asc," +
                    "[TEMP] float," +
                    "[HUMD] float," +
                    "[PRES] float," +
                    "[W1MD] smallint," +
                    "[W1MS] float," +
                    "[WTMD] smallint," +
                    "[WTMS] float," +
                    "[RAIN] float," +
                    "[VOTG] float);";

    private Database(Context ctx) {
        context = ctx;
        //dbHelper = new DBHelper(context);
        //---------------------------------------------
        //检查并创建数据库目录
        File dbDir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + DB_DIR);
        Log.i("Print", "Get Dir: " + dbDir.getAbsolutePath());
        if (!dbDir.exists()) {
            dbDir.mkdir();
        }
        //检查并创建数据库文件
        fileDB = new File(dbDir.getAbsolutePath() + File.separatorChar + DB_NAME);
        if (!fileDB.exists()) {
            try {
                fileDB.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //检查并创建数据库表
        SQLiteDatabase conn = SQLiteDatabase.openOrCreateDatabase(fileDB,null);
        conn.execSQL(sql_create_table);
        conn.close();
    }

    /**
     * 添加一条记录
     */
    public void insert(RecvData data) {
        final String sql = "insert into mdatas ([DTIME],[TEMP],[HUMD],[PRES],[W1MD],[W1MS],[WTMD],[WTMS],[RAIN],[VOTG]) values" +
                "(substr(datetime('now','localtime'),0,17)||':00',?,?,?,?,?,?,?,?,?);";
        try {
            //SQLiteDatabase conn = dbHelper.getWritableDatabase();
            SQLiteDatabase conn = SQLiteDatabase.openOrCreateDatabase(fileDB,null);
            if(conn == null){
                Log.i("Print", "连接没有打开!");
            }
            SQLiteStatement stmt = conn.compileStatement(sql);
            stmt.bindDouble(1, data.getTemp());       //温度
            stmt.bindDouble(2, data.getHumid());       //湿度
            stmt.bindDouble(3, data.getPress());       //气压
            //一分钟风
            stmt.bindLong(4, data.getWind_1min().dir);
            stmt.bindDouble(5, data.getWind_1min().speed);
            //十分钟风
            stmt.bindLong(6, data.getWind_10min().dir);
            stmt.bindDouble(7, data.getWind_10min().speed);
            stmt.bindDouble(8, data.getRain());      //雨量
            stmt.bindDouble(9, data.getVoltage());   //电压
            //执行插入
            stmt.executeInsert();
            //关闭当前连接
            conn.close();
        } catch (SQLException e) {
            //e.printStackTrace();
            Log.i("Print", "Insert Error : " + e.getMessage());
        }
    }

    public static Database getDao(Context ctx) {
        Database inst = dao;
        if (inst == null) {
            synchronized (Database.class) {
                inst = dao;
                if (inst == null) {
                    inst = new Database(ctx);
                    dao = inst;
                }
            }
        }
        return inst;
    }

}
