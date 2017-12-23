package com.dell.treasure.share;


import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;

import com.dell.treasure.support.CommonUtils;

import java.util.HashMap;
import java.util.Map;

public abstract class ShareableActivity extends BaseActivity {

	private Dialog dialog;
	private String path;
	private String source;
	private String paramStr = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		if (null != intent && null != intent.getData()) {
			path = intent.getData().getPath();
		}
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		setIntent(intent);
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	/**
	 * 场景还原时调用
	 * @param res 场景还原时的参数
	 */
	public void onReturnSceneData(HashMap<String, Object> res) {
		Log.d("result", "onReturnSceneData: "+res.size());
		for (Map.Entry<String, Object> entry : res.entrySet()) {
			Log.d("result", "onReturnSceneData: "+entry.getKey() + " : " + entry.getValue());
		}

		if (res != null) {
			if (TextUtils.isEmpty(path)) {
				path = (String)res.get("path");
			}
			if (res.get("source") != null) {
				source = String.valueOf(res.get("source"));
			}
			if (res.get("params") != null) {
				HashMap<String, Object> params = (HashMap<String, Object>) res.get("params");
				for (Map.Entry<String, Object> entry : params.entrySet()) {
					paramStr += (entry.getKey() + " : " + entry.getValue() + "\r\n");
				}
			}
		}

//		if (dialog == null) {
//			dialog = CommonUtils.getDialog(ShareableActivity.this, path, source, paramStr);
//		}
//		if (!dialog.isShowing()) {
//			dialog.show();
//		}
	}

	/**
	 * 显示参数
	 */
	protected void showParamDialog() {
		if (dialog == null) {
			dialog = CommonUtils.getDialog(this, path, source, paramStr);
		}
		if (!dialog.isShowing()) {
			dialog.show();
		}
	}
}
