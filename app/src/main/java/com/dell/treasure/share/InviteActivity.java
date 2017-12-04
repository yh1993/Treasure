package com.dell.treasure.share;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.support.CommonUtils;
import com.dell.treasure.support.CurrentUser;
import com.mob.moblink.ActionListener;
import com.mob.moblink.MobLink;

import java.util.HashMap;

public class InviteActivity extends BaseActivity{
	private static final String TAG = "InviteActivity";
	private TextView tvShare;
	private Button btnShare;

	private CurrentUser user;
	private String userId;
	private String mobID;
	private HashMap<Integer, String> mobIdCache; //mobID缓存

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invite);

		tvShare = (TextView) findViewById(R.id.tv_share);
		btnShare = (Button) findViewById(R.id.btn_share);

		tvShare.setOnClickListener(this);
		btnShare.setOnClickListener(this);

		user = CurrentUser.getOnlyUser();
		userId = user.getUserId();
		mobIdCache = new HashMap<Integer, String>();
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_share:
			case R.id.btn_share: {
				//分享
//				getUserIDToShare();
				share();
			} break;
			default: {
				super.onClick(v);
			} break;
		}
	}

//	private String getInviteTitleStr() {
//		String userId = CurrentUser.getOnlyUser().getUserId();
//		inviteID = (int)((Math.random() * 9 + 1) * 100000);
//		String format = getString(R.string.invite_user_title);
//		return  String.format(format, String.valueOf(inviteID));
//	}

//	private void getUserIDToShare() {
//		if (mobIdCache.containsKey(userId)) {
//			mobID = String.valueOf(mobIdCache.get(userId));
//			if (!TextUtils.isEmpty(mobID)) {
//				share();
//				return;
//			}
//		}
//		HashMap<String, Object> params = new HashMap<String, Object>();
//		params.put("inviteID", userId);
//		MobLink.getMobID(params, CommonUtils.INVITE_PATH, CommonUtils.INVITE_SOURCE, new ActionListener() {
//			public void onResult(HashMap<String, Object> params) {
//				if (params != null && params.containsKey("mobID")) {
//					mobID = String.valueOf(params.get("mobID"));
//					mobIdCache.put(userId, mobID);
//					Log.i(TAG, "Get mobID success ==>> " + mobID);
//				} else {
//					Toast.makeText(InviteActivity.this, "Get MobID Failed!", Toast.LENGTH_SHORT).show();
//				}
//				share();
//			}
//
//			public void onError(Throwable t) {
//				if (t != null) {
//					Toast.makeText(InviteActivity.this, "error = " + t.getMessage(), Toast.LENGTH_SHORT).show();
//				}
//				share();
//			}
//		});
//	}

	private void share() {
//		String shareUrl = CommonUtils.SHARE_URL + CommonUtils.INVITE_PATH;
		String shareUrl = "mlink://treasure.com"+ CommonUtils.INVITE_PATH;
		if (!TextUtils.isEmpty(userId)) {
			shareUrl += "?userId=" + userId;
		}
		String title = getString(R.string.invite_share_titel);
		String text = getString(R.string.share_text);
		String imgPath = CommonUtils.copyImgToSD(this, R.mipmap.ic_launcher , "invite");
		CommonUtils.showShare(this, title, text, shareUrl, imgPath);
	}

}
