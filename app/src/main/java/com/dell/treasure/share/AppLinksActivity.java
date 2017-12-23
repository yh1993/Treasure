package com.dell.treasure.share;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

public class AppLinksActivity extends BaseActivity {

	private static final String SCHEME_SCHEME = "mlink";
	private static final String SCHEME_HOST = "com.dell.treasure";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Intent intent = getIntent();
		handleIntentAndFinish(intent);
	}

	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		handleIntentAndFinish(intent);
	}

	private void handleIntentAndFinish(Intent intent) {
		Uri uri = intent.getData();
		Uri.Builder builder = uri.buildUpon();
		builder.scheme(SCHEME_SCHEME);
		builder.authority(SCHEME_HOST);
		uri = builder.build();
		intent = new Intent(Intent.ACTION_VIEW);
		intent.setData(uri);

		// 这里一定要先finish掉当前，然后再开启相应activity。(否则singleTop不起作用)
		finish();
		startActivity(intent);
	}
}
