package com.dell.treasure.support;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.dell.treasure.R;
import com.mob.moblink.MobLink;
import com.mob.tools.utils.ResHelper;

import java.io.File;
import java.io.FileOutputStream;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.onekeyshare.OnekeyShare;
import cn.sharesdk.onekeyshare.ShareContentCustomizeCallback;

public class CommonUtils {

	/**
	 * 标识, 连接是不是测试服
	 */
	public static final boolean DEBUGGABLE = MobLink.DEBUGGABLE;

	public static final String SHARE_URL;
	public static final String MAIN_PATH_ARR = "/demo/a";
	public static final String INVITE_PATH = "/params/invite";
	public static final String INVITE_SOURCE = "MobLinkDemo-Invite";

	static {
		if (DEBUGGABLE) {
//			SHARE_URL = "http://192.168.180.86:8000";
			SHARE_URL = "http://hgfdodo.win";
		} else {
//			SHARE_URL = "http://f.moblink.mob.com";
//			SHARE_URL = "http://39.106.142.138:8080";
			SHARE_URL = "http://hgfdodo.win";
		}
	}

	/**
	 * 创建一个自定义dialog，场景还原的参数展示dialog
	 * @param context
	 * @param path
	 * @param source
	 * @param params
	 * @return
	 */
	public static Dialog getDialog(Context context, String path, String source, String params) {
		final Dialog dialog = new Dialog(context, R.style.Dialog);
		dialog.setContentView(R.layout.dialog);
		if (!TextUtils.isEmpty(path)) {
			((TextView) dialog.findViewById(R.id.tv_dialog_path)).append("\r\n" + path);
		}
		if (!TextUtils.isEmpty(source)) {
			((TextView) dialog.findViewById(R.id.tv_dialog_source)).append("\r\n" + source);
		}
		if (!TextUtils.isEmpty(params)) {
			((TextView) dialog.findViewById(R.id.tv_dialog_param)).append("\r\n" + params);
		}
		dialog.findViewById(R.id.tv_dialog_close).setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/**
	 * 是否恢复场景的对话框
	 * @param context
	 * @param runnable
	 * @return
	 */
//	public static Dialog getRestoreSceneDialog(Context context, final Runnable runnable) {
//		final Dialog dialog = new Dialog(context, R.style.Dialog);
//		dialog.setContentView(R.layout.alert_dialog);
//		dialog.findViewById(R.id.dialog_btn_no).setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				dialog.dismiss();
//			}
//		});
//		dialog.findViewById(R.id.dialog_btn_yes).setOnClickListener(new View.OnClickListener() {
//			public void onClick(View v) {
//				if (runnable != null) {
//					runnable.run();
//				}
//				dialog.dismiss();
//			}
//		});
//		dialog.setCanceledOnTouchOutside(true);
//		return dialog;
//	}

	/**
	 * 获取mobid后才能分享的对话框
	 * @param context
	 * @return
	 */
	public static Dialog getMobIdDialog(Context context) {
		final Dialog dialog = new Dialog(context, R.style.Dialog);
		dialog.setContentView(R.layout.alert_dialog);
		dialog.findViewById(R.id.dialog_btn_no).setVisibility(View.GONE);
		dialog.findViewById(R.id.dialog_v_line).setVisibility(View.GONE);
		TextView tvTitle = (TextView) dialog.findViewById(R.id.tv_dialog_title);
		tvTitle.setText(R.string.please_get_mobid);
		TextView tvYes = (TextView) dialog.findViewById(R.id.dialog_btn_yes);
		tvYes.setText(R.string.close);
		tvYes.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
		dialog.setCanceledOnTouchOutside(true);
		return dialog;
	}

	/**
	 * 复制res中的图片到sdcard中
	 * @param context
	 * @param imgID
	 * @param imgName
	 * @return
	 */
	public static String copyImgToSD(Context context, int imgID, String imgName) {
		String imgPaht = "";
		Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), imgID);
		if (bitmap != null && !bitmap.isRecycled()) {
			String path = ResHelper.getImageCachePath(context);
			if (TextUtils.isEmpty(imgName)) {
				imgName = String.valueOf(System.currentTimeMillis());
			}
			File file = new File(path, imgName + ".jpg");
			if (file.exists()) {
				return file.getAbsolutePath();
			}
			try {
				FileOutputStream fos = new FileOutputStream(file);
				bitmap.compress(Bitmap.CompressFormat.JPEG, 80, fos);
				fos.flush();
				fos.close();
				imgPaht = file.getAbsolutePath();
			} catch (Throwable t) {
			}
		}
		return imgPaht;
	}

	/**
	 * 分享
	 * @param context
	 * @param title
	 * @param text
	 * @param url
	 * @param imgPath
	 */
	public static void showShare(Context context, String title, String text, String url, String imgPath) {
		OnekeyShare oks = new OnekeyShare();
		oks.setTitle(title);
		oks.setText(text);
		oks.setUrl(url);
		oks.setTitleUrl(url);
		oks.setImagePath(imgPath);
		oks.setShareContentCustomizeCallback(new ShareContentCustomizeCallback() {
			public void onShare(Platform platform, Platform.ShareParams paramsToShare) {
				if ("SinaWeibo".endsWith(platform.getName())) {
					String text = paramsToShare.getText() + paramsToShare.getUrl();
					paramsToShare.setText(text);
				} else if ("ShortMessage".endsWith(platform.getName())) {
					paramsToShare.setImagePath(null);
					String value = paramsToShare.getText();
					value += "\n";
					String url = paramsToShare.getUrl();
					value += url;
					paramsToShare.setText(value);

				}
			}
		});
		oks.show(context);
	}

}
