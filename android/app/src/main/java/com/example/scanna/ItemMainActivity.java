package com.example.scanna;


import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;

import com.example.scanna.scanner.ScannerActivity;
import com.example.scanna.uhf.UHFMain;
import com.port.Adapt;
import com.util.BaseActivity;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 
 * @author RFID_C Demo主菜单
 */
public class ItemMainActivity extends BaseActivity {

	GridView gridView;

	ArrayList<HashMap<String, Object>> listItemArrayList = new ArrayList<HashMap<String, Object>>();

	private static final int REQUEST_READ_PHONE_STATE = 1;

	private void checkPermission() {
		//检查权限（NEED_PERMISSION）是否被授权 PackageManager.PERMISSION_GRANTED表示同意授权
		if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE)
				!= PackageManager.PERMISSION_GRANTED) {
			//用户已经拒绝过一次，再次弹出权限申请对话框需要给用户一个解释
//			if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission
//					.WRITE_EXTERNAL_STORAGE)) {
//				ShowTip("请开通相关权限，否则无法正常使用本应用！");
//			}
			//申请权限
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
		} else {
			initView();

		}
	}

	void initView() {
		Adapt.init(this);
		Adapt.enablePauseInBackGround(this);
		listItemArrayList.clear();

		if (Adapt.getPropertiesInstance().support("UHF")) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", R.drawable.uhf);
			map.put("itemText", getString(R.string.btn_MainMenu_UHF));
			map.put("itemActivity", UHFMain.class);
			listItemArrayList.add(map);
		}


		if (Adapt.getPropertiesInstance().support("1DSCANNER")
				|| Adapt.getPropertiesInstance().support("2DScanner")) {
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", R.drawable.scan);
			map.put("itemText", getString(R.string.btn_MainMenu_Scan));
			map.put("itemActivity", ScannerActivity.class);
			listItemArrayList.add(map);
		}


		if (true) { // version
			HashMap<String, Object> map = new HashMap<String, Object>();
			map.put("itemImage", R.drawable.version);
			map.put("itemText", getString(R.string.btn_MainMenu_Version));
			map.put("itemActivity", null);
			listItemArrayList.add(map);
		}


		//生成适配器的ImageItem 与动态数组的元素相对应
		SimpleAdapter saImageItems = new SimpleAdapter(this,
				listItemArrayList,//数据来源
				R.layout.grid_item,//item的XML

				//动态数组与ImageItem对应的子项
				new String[]{"itemImage", "itemText"},

				//ImageItem的XML文件里面的一个ImageView,TextView ID
				new int[]{R.id.grid_item_image, R.id.grid_item_txt});
		//添加并且显示
		gridView.setAdapter(saImageItems);
		//添加消息处理
		gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				//Toast.makeText(getActivity(),name[position],Toast.LENGTH_LONG).show();
				if (isFastClick()) {
					return;
				}
				HashMap<String, Object> map = listItemArrayList.get(position);

				if (getString(R.string.btn_MainMenu_Version).equals(map.get("itemText"))) {
					GetVersion(view);
					return;
				}

				Intent intent = new Intent();
				intent.setClass(ItemMainActivity.this, (Class<?>) map.get("itemActivity"));

				startActivity(intent);
			}
		});

		showCustomBar(getString(R.string.tv_MainMenu_Title),
				getString(R.string.str_exit), null,
				R.drawable.left, 0,
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						Exit(v);
					}
				},
				null
		);

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// 创建
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.item_main);
		keepScreenOn(true);
		gridView = (GridView) findViewById(R.id.main_item_grid);

		ChangeLayout(getResources().getConfiguration());
		checkPermission();
	}

	private void ChangeLayout(Configuration newConfig) {
		if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) { //当前屏幕为横屏
			gridView.setNumColumns(4);

		} else { // 当前屏幕为竖屏
			gridView.setNumColumns(3);
		}
	}


	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		ChangeLayout(newConfig);
	}

	@Override
	protected void onPause() {
		DisposeAll();
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		DisposeAll();
		super.onDestroy();
	}

	public void GetVersion(View v) {

		String sdkVersion = Adapt.getVersion();

		super.showTip("APP:" + getVersion() + "\n" + "SDK:"
				+ sdkVersion);
	}

	/**
	 * 退出应用
	 */
	public void Exit(View v) {
		DisposeAll();
		ItemMainActivity.this.finish();
		System.exit(0);
	}

	/**
	 * 释放所有对象
	 */
	public void DisposeAll() {
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		if (requestCode == REQUEST_READ_PHONE_STATE) {
			initView();
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}
}
