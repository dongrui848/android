package com.instcar.android.entry;

import com.mycommonlib.android.common.util.JSONUtils;
import com.mycommonlib.android.common.util.StringUtils;

public class NetDataEntry {
	public static String USER_STATE_RENZHENG="1";
	public static String USER_STATE_NOTRENZHENG="2";
	public static String USER_STATE_RENZHENGING="3";
	
	
	public String smsid="";//短信验证码获取smsid
	public String phone="";//短信验证码获取phone	
	public String uid="";//短信验证码获取phone	
	public String id="";
	public String name="";
	public String headpic="";
	public String status="";
	public String addtime="";
	public String modtime="";
	public String signature="";
	public String home_addr="";
	public String comp_addr="";
	public String show_home_addr="";
	public String show_comp_addr="";
	public String uploadfile="";
	public String ischeck="";//1:验证通过；2：未通过实名认证；3：正在认证中
	
	
	public NetDataEntry(String jsonString) {
		if(!StringUtils.isEmpty(jsonString)&&!"[]".equals(jsonString)){
			
			this.uid = JSONUtils.getString(jsonString, "uid", "");
			this.smsid = JSONUtils.getString(jsonString, "smsid", "");
			this.phone = JSONUtils.getString(jsonString, "phone", "");
			this.id = JSONUtils.getString(jsonString, "id", "");
			this.name = JSONUtils.getString(jsonString, "name", "");
			this.headpic = JSONUtils.getString(jsonString, "headpic", "");
			this.status = JSONUtils.getString(jsonString, "status", "");
			this.addtime = JSONUtils.getString(jsonString, "addtime", "");
			this.modtime = JSONUtils.getString(jsonString, "modtime", "");
			this.signature= JSONUtils.getString(jsonString, "signature", "");
			this.home_addr= JSONUtils.getString(jsonString, "home_addr", "");
			this.comp_addr= JSONUtils.getString(jsonString, "comp_addr", "");
			this.show_home_addr= JSONUtils.getString(jsonString, "show_home_addr", "");
			this.show_comp_addr= JSONUtils.getString(jsonString, "show_comp_addr", "");
			this.uploadfile= JSONUtils.getString(jsonString, "uploadfile", "");
		}else{

		}
		
	}

	@Override
	public String toString() {
		String s = "smsid="+this.smsid+"phone="+this.phone;
		return s;
	}
	
}
