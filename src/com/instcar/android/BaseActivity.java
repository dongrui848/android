package com.instcar.android;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.instcar.android.config.Config;
import com.instcar.android.config.HandleConfig;
import com.instcar.android.service.CommonService;
import com.instcar.android.service.UploadImageService;
import com.instcar.android.thread.AuthPhoneThread;
import com.instcar.android.thread.CommonThread;
import com.instcar.android.thread.GetCarThread;
import com.instcar.android.thread.GetPhoneCodeThread;
import com.instcar.android.thread.GetUserInfoThread;
import com.instcar.android.thread.LoginThread;
import com.instcar.android.thread.UserRegisterThread;
import com.instcar.android.util.ApplicationVar;
import com.instcar.android.util.MyLog;
import com.instcar.android.util.SdCard;
import com.mycommonlib.android.common.util.ImageUtils;
import com.mycommonlib.android.common.util.StringUtils;
import com.mycommonlib.android.common.util.ToastUtils;

/**
 * 
 * @dujie
 * 
 */
public class BaseActivity extends Activity {
	ProgressDialog progressDialog;
	public Handler mHandler;
	ImageButton btn_left;
	ImageButton btn_right;
	TextView navbar;
	TextView navbar2;

	public ApplicationVar av;

	public Drawable image;
	public String filename;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		av = (ApplicationVar) getApplication();
	}

	public void showToastError(String message) {
		ToastUtils.show(this, message);
	}

	public void setEditStatus(EditText e, int status) {

		if (status == 1) {
			e.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.ic_agree_ok, 0);
		} else {
			e.setCompoundDrawablesWithIntrinsicBounds(0, 0,
					R.drawable.ic_agree_no, 0);
		}
	}

	public void showProcessDialog(String message) {
		dismissProcessDialog();
		progressDialog = ProgressDialog.show(this, "请稍后", message, true, false);
	}

	public void dismissProcessDialog() {
		if (progressDialog != null && progressDialog.isShowing()) {
			progressDialog.dismiss();
		}

	}

	public void initBaseNav(){
		
		try {
			btn_right = (ImageButton) findViewById(R.id.btn_right);
			btn_left = (ImageButton) findViewById(R.id.btn_left);
			navbar  =(TextView) findViewById(R.id.text_top_bar);
			navbar2  =(TextView) findViewById(R.id.top_bar_tip);
			
			btn_left.setOnClickListener(new OnClickListener() {
				
				@Override
				public void onClick(View v) {
					// TODO Auto-generated method stub
					BaseActivity.this.finish();
				}
			});
		} catch (Exception e) {

		}
		
	}
	/*
	 * 用户注册
	 */
	public void userRegister(String phoneNum, String smsid, String authcode,
			String password) {
		Map<String, String> param = new HashMap<String, String>();
		// smsid="11632456";

		param.put("phone", phoneNum);
		param.put("password", password);
		param.put("authcode", authcode);
		param.put("smsid", smsid);
		CommonService service = new CommonService(
				Config.apiserveruserregister());
		service.setParam(param);
		service.setAv(av);
		UserRegisterThread thread = new UserRegisterThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	public void getAuthCode(String phoneNum) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("phone", phoneNum);
		CommonService service = new CommonService(
				Config.apiserverusergetauthcode());
		service.setParam(param);
		service.setAv(av);
		GetPhoneCodeThread thread = new GetPhoneCodeThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	public void AuthPhone(String phoneNum) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("phone", phoneNum);
		CommonService service = new CommonService(
				Config.apiuserCheckUserPhone());
		service.setParam(param);
		service.setAv(av);
		AuthPhoneThread phoneThread = new AuthPhoneThread();
		phoneThread.setHandle(mHandler);
		phoneThread.setService(service);
		new Thread(phoneThread).start();

	}

	public void login(String phone, String password) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("phone", phone);
		param.put("password", password);

		CommonService service = new CommonService(Config.apiserveruserlogin());
		service.setParam(param);
		service.setAv(av);
		LoginThread thread = new LoginThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	public void userdetail(String uid) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("id", uid);

		CommonService service = new CommonService(Config.apiserveruserdetail());
		service.setParam(param);
		service.setAv(av);
		GetUserInfoThread thread = new GetUserInfoThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	// 获取用户汽车列表
	public void getcar(String car_id) {
		Map<String, String> param = new HashMap<String, String>();
		param.put("car_id", car_id);

		CommonService service = new CommonService(Config.apiserverusergetcars());
		service.setParam(param);
		service.setAv(av);
		GetCarThread thread = new GetCarThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	// 增加汽车
	public void useredit(String key,String value) {
		Map<String, String> param = new IdentityHashMap<String, String>();
		param.put(key, value);
		param.put(key, value);
		param.put(key, value);
		
		CommonService service = new CommonService(Config.apiserveruseredit());
		service.setParam(param);
		service.setAv(av);
		CommonThread thread = new CommonThread();
		thread.setwhat(HandleConfig.EDITUSERINFO);
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();
		
	}
	// 增加汽车
	public void addcar(String car_id, String license, String car1, String car2) {
		Map<String, String> param = new IdentityHashMap<String, String>();
		param.put("car_id", car_id);
		param.put("license", license);
		param.put("cars[]", car1);
		param.put("cars[]", car1);

		MyLog.d(car1);
		MyLog.d(car2);
		CommonService service = new CommonService(Config.apiserveruseraddcar());
		service.setParam(param);
		service.setAv(av);
		CommonThread thread = new CommonThread();
		thread.setwhat(HandleConfig.ADDCAR);
		thread.setHandle(mHandler);
		thread.setService(service);
		new Thread(thread).start();

	}

	// 用户实名认证
	public void realnamerequest(String id_cards1, String id_cards2) {
		Map<String, String> param = new IdentityHashMap<String, String>();
		param.put("id_cards[]", id_cards1);
		param.put("id_cards[]", id_cards2);

		// 需要修改1
		CommonService service = new CommonService(
				Config.apiserveruserrealnamerequest());
		service.setParam(param);
		service.setAv(av);
		CommonThread thread = new CommonThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		// 需要修改2
		thread.setwhat(HandleConfig.REALNAMEREQUEST);
		new Thread(thread).start();

	}

	// 图片上传
	public void imageupload(String type, String user_id, Bitmap photo) {
		final Map<String, String> params = new HashMap<String, String>();
		params.put("user_id", user_id);
		params.put("type", type);



		// 需要修改1
		UploadImageService service = new UploadImageService(
				Config.apiserverimageupload());
		service.params = params;
		service.photo =photo;
		service.setAv(av);

		CommonThread thread = new CommonThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		// 需要修改2
		thread.setwhat(HandleConfig.UPLOADIMAGE);
		new Thread(thread).start();

	}
	// 用户实名认证
	public void requestcarxilie(String name) {
		name =name.replace("_a.jpg", "");
		Map<String, String> param = new IdentityHashMap<String, String>();
		param.put("aliasname", name);

		// 需要修改1
		CommonService service = new CommonService(
				Config.apiservercarlist());
		service.setParam(param);
		service.setAv(av);
		CommonThread thread = new CommonThread();
		thread.setHandle(mHandler);
		thread.setService(service);
		// 需要修改2
		thread.setwhat(HandleConfig.CARLIST);
		new Thread(thread).start();

	}
	
	public Drawable getUrlImage(final String url){
		try {
			
	
		  filename=""; 
		try {
			 filename = URLEncoder.encode(url,"UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(SdCard.iSFileExit(SdCard.getUserIconPath()+filename)){
			image = Drawable.createFromPath(SdCard.getUserIconPath()+filename);
			
		}else{
			
			Thread t = new Thread(new Runnable() {
				public  Drawable da=null;
				@Override
				public void run() {
					image= ImageUtils.getDrawableFromUrl(url, 5000);
					SdCard.savePicture(SdCard.getUserIconPath(), image, filename);
					
					mHandler.sendEmptyMessage(HandleConfig.SETHEADIMG);
				}
			});
			t.start();
		}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return image;
		
		
	}
	//根据name 获取car的icon
	public void getCariconByname(){
		
		
	}


}
