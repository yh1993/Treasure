package com.dell.treasure.share;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.dell.treasure.R;

import java.util.HashMap;

public class InviteDetailActivity extends ShareableActivity {

	private TextView tvInviteTitle;
	private TextView tvInviteText;

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_invite_detail);

		tvInviteTitle = (TextView) findViewById(R.id.tv_invite_title);
		tvInviteText = (TextView) findViewById(R.id.tv_invite_text);
		tvInviteTitle.setText(String.format(getString(R.string.invite_register_success), ""));
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	public void onReturnSceneData(HashMap<String, Object> res) {
		super.onReturnSceneData(res);
		HashMap<String, Object> params = (HashMap<String, Object>) res.get("params");
		if (null != params) {
			if (params.containsKey("inviteID")) {
				tvInviteText.setText(String.format(getString(R.string.invite_register_person), params.get("inviteID")));
			}
			if (params.containsKey("name")) {
				tvInviteTitle.setText(String.format(getString(R.string.invite_register_success), params.get("name")));
			}
		}
	}
}
