package com.nilin.easynote;

import java.util.ArrayList;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity {
	private ImageButton addnotes;
	private ListView travelslist;
	private DBManage dm = null;
	public Cursor cursor = null;
	private ListViewAdd adapter;// 数据源对象
	public static final String CHECK_STATE = "0";
	public static final String EDIT_STATE = "1";
	public static final String ALERT_STATE = "2";
	public long mExitTime;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		addnotes = (ImageButton) findViewById(R.id.addnotes);
		addnotes.setOnClickListener(new addListener());
		travelslist = (ListView) findViewById(R.id.travelslist);
		dm = new DBManage(this);
		initAdapter();
		travelslist.setAdapter(adapter);
		travelslist.setOnItemClickListener(new myOnItemClickListener());
		travelslist.setOnCreateContextMenuListener(new myOnCreateContextMenuListener());//设置长按监听器
	}

	class addListener implements OnClickListener {
		public void onClick(View v) {
			Intent intent = new Intent();
			intent.setClass(MainActivity.this, editnotes.class);
			intent.putExtra("state", EDIT_STATE);
			startActivity(intent);
			finish();
		}
	}

	public void initAdapter() {
		dm.open();//打开数据库操作对象
		cursor = dm.selectAll();//打开数据库操作对象
		cursor.moveToFirst();//将游标移动到第一条数据，使用前必须调用
		int count = cursor.getCount();//个数
		ArrayList<String> items = new ArrayList<String>();
		ArrayList<String> times = new ArrayList<String>();
		for (int i = 0; i < count; i++) {
			items.add(cursor.getString(cursor.getColumnIndex("title")));
			times.add(cursor.getString(cursor.getColumnIndex("time")));
			cursor.moveToNext();//将游标指向下一个
		}
		dm.close();//关闭数据操作对象
		adapter = new ListViewAdd(this, items, times);//创建数据源
	}

	public class myOnCreateContextMenuListener implements OnCreateContextMenuListener {

		public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
			final AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
			menu.setHeaderTitle("");
			//设置选项
			menu.add(0, 0, 0, "查看");
			menu.add(0, 1, 0, "编辑");
			menu.add(0, 2, 0, "删除");
		}
	}

	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo menuInfo = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
		dm.open();
		switch (item.getItemId()) {
			case 0://查看
				try {
					cursor.moveToPosition(menuInfo.position);
					Intent intent = new Intent();
					intent.putExtra("id", cursor.getString(cursor.getColumnIndex("_id")));
					intent.putExtra("title", cursor.getString(cursor.getColumnIndex("title")));
					intent.putExtra("time", cursor.getString(cursor.getColumnIndex("time")));
					intent.putExtra("content", cursor.getString(cursor.getColumnIndex("content")));
					intent.setClass(MainActivity.this, checknotes.class);
					MainActivity.this.startActivity(intent);
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;

			case 1://修改
				try {
					cursor.moveToPosition(menuInfo.position);
					//用于Activity之间的通讯
					Intent intent = new Intent();
					//通讯时的数据传送
					intent.putExtra("id", cursor.getString(cursor.getColumnIndex("_id")));
					intent.putExtra("state", ALERT_STATE);
					intent.putExtra("title", cursor.getString(cursor.getColumnIndex("title")));
					intent.putExtra("time", cursor.getString(cursor.getColumnIndex("time")));
					intent.putExtra("content", cursor.getString(cursor.getColumnIndex("content")));
					//设置并启动另一个指定的Activity
					intent.setClass(MainActivity.this, editnotes.class);
					MainActivity.this.startActivity(intent);
					finish();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
			case 2://删除
				try {
					cursor.moveToPosition(menuInfo.position);
					int i = dm.delete(Long.parseLong(cursor.getString(cursor.getColumnIndex("_id"))));//删除数据
					adapter.removeListItem(menuInfo.position);//删除数据
					adapter.notifyDataSetChanged();//通知数据源，数据已经改变，刷新界面
					dm.close();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
				break;
		}
		dm.close();
		return super.onContextItemSelected(item);
	}

	//短按，即点击
	public class myOnItemClickListener implements OnItemClickListener {
		public void onItemClick(AdapterView<?> arg0, View v, int position, long id) {
			cursor.moveToPosition(position);
			Intent intent = new Intent();
			intent.putExtra("state", CHECK_STATE);
			intent.putExtra("id", cursor.getString(cursor.getColumnIndex("_id")));
			intent.putExtra("title", cursor.getString(cursor.getColumnIndex("title")));
			intent.putExtra("content", cursor.getString(cursor.getColumnIndex("content")));
			intent.putExtra("time", cursor.getString(cursor.getColumnIndex("time")));
			intent.setClass(MainActivity.this, checknotes.class);
			MainActivity.this.startActivity(intent);
		}
	}


	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK) {
			if ((System.currentTimeMillis() - mExitTime) > 2000) {
				Toast.makeText(this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
				mExitTime = System.currentTimeMillis();
			}
			else
				finish();
		}
		return true;
	}
}
