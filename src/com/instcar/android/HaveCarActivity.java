package com.instcar.android;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.baidu.mapapi.BMapManager;
import com.baidu.mapapi.map.MapController;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.OverlayItem;
import com.baidu.platform.comapi.basestruct.GeoPoint;
import com.baidu.voicerecognition.android.Candidate;
import com.baidu.voicerecognition.android.VoiceRecognitionClient;
import com.baidu.voicerecognition.android.VoiceRecognitionClient.VoiceClientStatusChangeListener;
import com.baidu.voicerecognition.android.VoiceRecognitionConfig;
import com.instcar.android.adapter.PointListAdapter;
import com.instcar.android.config.HandleConfig;
import com.instcar.android.entry.NetDataEntry;
import com.instcar.android.entry.NetEntry;
import com.instcar.android.util.Config;

/**
 * 演示MapView的基本用法
 */
public class HaveCarActivity extends MapBaseActivity {

	final static String TAG = "MainActivity";
	/**
	 * MapView 是地图主控件
	 */
	/**
	 * 用MapController完成地图控制
	 */

	TextView textview_start;
	EditText edittext_end;
	ImageButton button_voice;
	ImageButton button_onsearch;
	ImageButton mylocation;

	ImageButton button_sub;
	ImageButton button_add;
	ListView pointlist;
	PointListAdapter pointListAdapter;

	private View view;

	private MyVoiceRecogListener voiceListener;
	private VoiceRecognitionConfig config;
	private VoiceRecognitionClient mASREngine;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBMapMan = new BMapManager(getApplication());
		mBMapMan.init(null);
		// 注意：请在试用setContentView前初始化BMapManager对象，否则会报错
		setContentView(R.layout.have_car_main);
		mMapView = (MapView) findViewById(R.id.bmapView);
		mMapView.setBuiltInZoomControls(false);
		mMapView.setOnTouchListener(new OnTouchListener() {

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				pointlist.setVisibility(View.GONE);
				return false;
			}
		});
		// 设置启用内置的缩放控件
		MapController mMapController = mMapView.getController();
		// 得到mMapView的控制权,可以用它控制和驱动平移和缩放
		GeoPoint point = new GeoPoint((int) (39.915 * 1E6),
				(int) (116.404 * 1E6));
		// 用给定的经纬度构造一个GeoPoint，单位是微度 (度 * 1E6)
		mMapController.setCenter(point);// 设置地图中心点
		mMapController.setZoom(12);// 设置地图zoom级别
		initview();
		initVoice();
		initHandle();
		List<NetDataEntry> list = new ArrayList<NetDataEntry>();
		pointListAdapter = new PointListAdapter(this, list);
		pointlist.setAdapter(pointListAdapter);
		;
		initOverLay();// 初始化图层
		initPopOverLay();
		initPopView();// 初始化弹出图层
		initLocationSet();
		// getCurrentLocation();

	}

	void startVoice() {
		mASREngine.startVoiceRecognition(voiceListener, config);
	}

	void initVoice() {
		voiceListener = new MyVoiceRecogListener();
		mASREngine = VoiceRecognitionClient.getInstance(this);
		mASREngine.setTokenApis("8MAxI5o7VjKSZOKeBzS4XtxO",
				"Ge5GXVdGQpaxOmLzc8fOM8309ATCz9Ha");
		config = new VoiceRecognitionConfig();
		config.setProp(Config.CURRENT_PROP);
		config.setLanguage(Config.getCurrentLanguage());
		config.enableVoicePower(Config.SHOW_VOL); // 音量反馈。
		if (Config.PLAY_START_SOUND) {
			// config.enableBeginSoundEffect(R.raw.bdspeech_recognition_start);
			// // 设置识别开始提示音
		}
		if (Config.PLAY_END_SOUND) {
			// config.enableEndSoundEffect(R.raw.bdspeech_speech_end); //
			// 设置识别结束提示音
		}
		config.setSampleRate(VoiceRecognitionConfig.SAMPLE_RATE_8K); // 设置采样率,需要与外部音频一致

		// mASREngine.startVoiceRecognition(voiceListener, config);

	}

	void initHandle() {
		mHandler = new Handler() {
			@Override
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case HandleConfig.QUERYPOINT:
					NetEntry entry = decodePointList(msg.getData().getString(
							"data"));

					if (NetEntry.CODESUCESS.equals(entry.status)) {

						if (entry.netentry.list.size() > 0) {// 获取到数据了 条数大于0
							pointlist.setVisibility(View.VISIBLE);
							pointListAdapter
									.updateListView(entry.netentry.list);// 刷新列表
							addpointToview(entry.netentry);

						}

					} else {

						showToastError(entry.msg);
					}

					break;

				case HandleConfig.RECEIVELOCATION:
					textview_start.setText(currentLocation.getStreet());
					GeoPoint pt3 = new GeoPoint(
							(int) (currentLocation.getLatitude() * 1E6),
							(int) (currentLocation.getLongitude() * 1E6));
					mMapView.getController().animateTo(pt3);
					break;
				default:
					break;
				}

			}

		};

	}

	private void addpointToview(NetDataEntry entry) {
		try {

			if (entry != null && entry.list.size() > 0) {
				mOverlay.removeAll();
				mItems.clear();
				mCurItem = null;
				mStartItem = null;
				mEndItem = null;
				for (int i = 0; i < entry.list.size(); i++) {
					NetDataEntry data = entry.list.get(i);

					GeoPoint p4 = new GeoPoint(
							(int) (Double.valueOf(data.lat) * 1E6),
							(int) (Double.valueOf(data.lng) * 1E6));
					OverlayItem item1 = new OverlayItem(p4, data.name, "");
					item1.setMarker(getResources().getDrawable(
							R.drawable.point_empty));
					mOverlay.addItem(item1);
					mItems.add(item1);

				}

				refreshMapView();
			}

		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}

	void initview() {
		navbar = (TextView) findViewById(R.id.text_top_bar);
		navbar.setText("我有车");
		btn_right = (ImageButton) findViewById(R.id.btn_right);
		btn_right.setVisibility(View.GONE);
		btn_left = (ImageButton) findViewById(R.id.btn_left);
		btn_left.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				HaveCarActivity.this.finish();
			}
		});
		textview_start = (TextView) findViewById(R.id.textview_start);
		edittext_end = (EditText) findViewById(R.id.edittext_end);
		edittext_end.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO Auto-generated method stub
				queryPiont(s.toString(), "");

			}

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO Auto-generated method stub

			}
		});

		button_voice = (ImageButton) findViewById(R.id.button_voice);
		button_voice.setVisibility(View.VISIBLE);

		button_voice.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setstatus(1);

				startVoice();
			}
		});
		button_onsearch = (ImageButton) findViewById(R.id.button_onsearch);
		button_onsearch.setVisibility(View.GONE);

		mylocation = (ImageButton) findViewById(R.id.button_mylocation);
		mylocation.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				getCurrentLocation();
			}
		});
		button_sub = (ImageButton) findViewById(R.id.button_sub);
		button_sub.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMapView.getController().zoomOut();
			}
		});
		button_add = (ImageButton) findViewById(R.id.button_add);
		button_add.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				mMapView.getController().zoomIn();
			}
		});
		view = findViewById(R.id.mapzhezhao);
		view.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				setstatus(1);
			}
		});

		pointlist = (ListView) findViewById(R.id.listview_point);

	}

	public void setstatus(int status) {
		switch (status) {
		case 1:
			button_voice.setVisibility(View.GONE);
			button_onsearch.setVisibility(View.VISIBLE);
			edittext_end.setVisibility(View.VISIBLE);
			view.setVisibility(View.GONE);
			break;
		case 2:

			break;

		default:
			break;
		}

	}

	@Override
	protected void onPause() {
		/**
		 * MapView的生命周期与Activity同步，当activity挂起时需调用MapView.onPause()
		 */
		mMapView.onPause();
		super.onPause();
	}

	@Override
	protected void onResume() {
		/**
		 * MapView的生命周期与Activity同步，当activity恢复时需调用MapView.onResume()
		 */
		mMapView.onResume();
		super.onResume();
	}

	@Override
	protected void onDestroy() {
		/**
		 * MapView的生命周期与Activity同步，当activity销毁时需调用MapView.destroy()
		 */
		mMapView.destroy();
		super.onDestroy();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		mMapView.onSaveInstanceState(outState);

	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mMapView.onRestoreInstanceState(savedInstanceState);
	}

	/*
	 * 返回当前位置的string
	 */
	public String getLocationString() {

		return "西二旗大厦";
	}

	public void addPoint() {

		// GeoPoint p2 = new GeoPoint ((int)(mLat2*1E6),(int)(mLon2*1E6));
		// OverlayItem item2 = new OverlayItem(p2,"覆盖物2","");
		// item2.setMarker(getResources().getDrawable(R.drawable.icon_markb));
	}

	/**
	 * 重写用于处理语音识别回调的监听器
	 */
	class MyVoiceRecogListener implements VoiceClientStatusChangeListener {

		@Override
		public void onClientStatusChange(int status, Object obj) {
			switch (status) {
			// 语音识别实际开始，这是真正开始识别的时间点，需在界面提示用户说话。
			case VoiceRecognitionClient.CLIENT_STATUS_START_RECORDING:
				showProcessDialog("语音输入中..");
				showToastError("语音识别实际开始");
				break;
			// 检测到语音起点
			case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_START:
				showToastError("检测到语音起点");
				break;
			// 已经检测到语音终点，等待网络返回
			case VoiceRecognitionClient.CLIENT_STATUS_SPEECH_END:
				showToastError("已经检测到语音终点，等待网络返回");
				break;
			// 语音识别完成，显示obj中的结果
			case VoiceRecognitionClient.CLIENT_STATUS_FINISH:
				dismissProcessDialog();
				updateRecognitionResult(obj);

				break;
			// 处理连续上屏
			case VoiceRecognitionClient.CLIENT_STATUS_UPDATE_RESULTS:
				// showToastError("处理连续上屏");
				break;
			// 用户取消
			case VoiceRecognitionClient.CLIENT_STATUS_USER_CANCELED:
				showToastError("用户取消");
				break;
			default:
				break;
			}

		}

		@Override
		public void onError(int errorType, int errorCode) {
			String s = getString(R.string.error_occur,
					Integer.toHexString(errorCode));
			dismissProcessDialog();
			showToastError(errorCode + "处理错误" + s);
		}

		@Override
		public void onNetworkStatusChange(int status, Object obj) {
			// 这里不做任何操作不影响简单识别
		}
	}

	private void updateRecognitionResult(Object result) {
		if (result != null && result instanceof List) {
			List results = (List) result;
			if (results.size() > 0) {
				if (results.get(0) instanceof List) {
					List<List<Candidate>> sentences = (List<List<Candidate>>) result;
					StringBuffer sb = new StringBuffer();
					for (List<Candidate> candidates : sentences) {
						if (candidates != null && candidates.size() > 0) {
							sb.append(candidates.get(0).getWord());
						}
					}
					edittext_end.setText(sb.toString());
				} else {
					edittext_end.setText(results.get(0).toString());
				}
			}
		}
	}

	/**
	 * 刷新图层， 判断当前的start end 是否为空 刷新，并且给出路线
	 * 
	 */
	/**
	 * 初始化 弹出框
	 */
	void initPopView() {
		popview = getLayoutInflater().inflate(R.layout.dialog_map_select, null);
		popviewtitle = (TextView) popview.findViewById(R.id.map_select_title);

		popviewbuttonstart = (Button) popview
				.findViewById(R.id.map_select_button_start);
		popviewbuttonstart.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub]
				if (mStartItem != null) {
					mStartItem.setMarker(getResources().getDrawable(
							R.drawable.point_empty));
					mOverlay.updateItem(mStartItem);
				}
				if (mEndItem != null && mEndItem == mCurItem) {// 如果终点也被选为起点了，则终点取消
					mEndItem = null;
				}
				mStartItem = mCurItem;
				textview_start.setText(mStartItem.getTitle());
				
				pop.hidePop();
				refreshMapView();
			}
		});
		popviewbuttonend = (Button) popview
				.findViewById(R.id.map_select_button_end);
		popviewbuttonend.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub

				if (mEndItem != null) {
					mEndItem.setMarker(getResources().getDrawable(
							R.drawable.point_empty));
					mOverlay.updateItem(mEndItem);
				}
				if (mStartItem != null && mStartItem == mCurItem) {// 如果终点也被选为起点了，则终点取消
					mStartItem = null;
					
				}
				mEndItem = mCurItem;
				pop.hidePop();
				refreshMapView();

			}
		});

	}

	void refreshMapView() {

		// 判断起点终点是否为空，如果不为空，就展现之，
		if (mStartItem != null) {
			mStartItem.setMarker(getResources().getDrawable(
					R.drawable.point_start));
			mOverlay.updateItem(mStartItem);
		}
		if (mEndItem != null) {
			mEndItem.setMarker(getResources().getDrawable(R.drawable.point_end));
			mOverlay.updateItem(mEndItem);
		}

		mMapView.refresh();

	}

}
