package com.dell.treasure.share;

import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dell.treasure.R;
import com.dell.treasure.support.CommonUtils;
import com.mob.moblink.ActionListener;
import com.mob.moblink.MobLink;

import java.util.HashMap;

public class InviteActivity extends BaseActivity{
	private static final String TAG = "InviteActivity";
	private TextView tvTitle;
	private TextView tvShare;
	private TextView tvUserID;
	private ImageView ivInvite;
	private TextView tvInviteTitle;
	private TextView tvInviteText;
	private Button btnShare;

	private int inviteID;
	private String mobID;
	private HashMap<Integer, String> mobIdCache; //mobID缓存

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invite);

		tvTitle = (TextView) findViewById(R.id.tv_title);
		tvShare = (TextView) findViewById(R.id.tv_share);
		tvUserID = (TextView) findViewById(R.id.tv_user_id);
		ivInvite = (ImageView) findViewById(R.id.iv_invite);
		tvInviteTitle = (TextView) findViewById(R.id.tv_invite_title);
		tvInviteText = (TextView) findViewById(R.id.tv_invite_text);
		btnShare = (Button) findViewById(R.id.btn_share);

		tvShare.setOnClickListener(this);
		btnShare.setOnClickListener(this);

		tvUserID.setText(Html.fromHtml(getInviteTitleStr()));
		tvInviteText.setText(Html.fromHtml(getString(R.string.invite_user_content)));

		mobIdCache = new HashMap<Integer, String>();
	}

	public void onClick(View v) {
		switch (v.getId()) {
			case R.id.tv_share:
			case R.id.btn_share: {
				//分享
				getMobIDToShare();
			} break;
			default: {
				super.onClick(v);
			} break;
		}
	}

	private String getInviteTitleStr() {
		inviteID = (int)((Math.random() * 9 + 1) * 100000);
		String format = getString(R.string.invite_user_title);
		return  String.format(format, String.valueOf(inviteID));
	}

	private void getMobIDToShare() {
		if (mobIdCache.containsKey(inviteID)) {
			mobID = String.valueOf(mobIdCache.get(inviteID));
			if (!TextUtils.isEmpty(mobID)) {
				share();
				return;
			}
		}
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("inviteID", inviteID);
		MobLink.getMobID(params, CommonUtils.INVITE_PATH, CommonUtils.INVITE_SOURCE, new ActionListener() {
			public void onResult(HashMap<String, Object> params) {
				if (params != null && params.containsKey("mobID")) {
					mobID = String.valueOf(params.get("mobID"));
					mobIdCache.put(inviteID, mobID);
					Log.i(TAG, "Get mobID success ==>> " + mobID);
				} else {
					Toast.makeText(InviteActivity.this, "Get MobID Failed!", Toast.LENGTH_SHORT).show();
				}
				share();
			}

			public void onError(Throwable t) {
				if (t != null) {
					Toast.makeText(InviteActivity.this, "error = " + t.getMessage(), Toast.LENGTH_SHORT).show();
				}
				share();
			}
		});
	}

	private void share() {
		String shareUrl = CommonUtils.SHARE_URL + CommonUtils.INVITE_PATH;
		if (!TextUtils.isEmpty(mobID)) {
			shareUrl += "?mobid=" + mobID;
		}
		String title = getString(R.string.invite_share_titel);
		String text = getString(R.string.share_text);
		String imgPath = CommonUtils.copyImgToSD(this, R.drawable.demo_share_invite , "invite");
		CommonUtils.showShare(this, title, text, shareUrl, imgPath);
	}

}
