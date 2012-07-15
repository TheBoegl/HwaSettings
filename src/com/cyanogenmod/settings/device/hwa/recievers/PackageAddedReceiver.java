package com.cyanogenmod.settings.device.hwa.recievers;

import java.io.File;
import java.util.Arrays;
import com.cyanogenmod.settings.device.hwa.PackageListProvider;

import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;

public class PackageAddedReceiver extends BroadcastReceiver {

	private static final String TAG = "PackagesMonitor";
	private ContentResolver mContentResolver;
	private String mPackageName;
	private PackageManager mPackageManager;

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "Received broadcast : " + intent.toString());
		mContentResolver = context.getContentResolver();
		mPackageName = intent.getDataString().split(":")[1];
		mPackageManager = context.getPackageManager();
		new AddPackage().execute();
	}

	private class AddPackage extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... params) {
			ContentValues values = new ContentValues();
			File denyFolder = new File("/data/local/hwui.deny");
			if (!denyFolder.exists()) {
				denyFolder.mkdirs();
			}
			File[] files = denyFolder.listFiles();
			String[] packageBlacklist = new String[files.length];
			for (int i = 0; i < files.length; i++) {
				packageBlacklist[i] = files[i].getName();
			}
			boolean hwaIsDiabled = false;
			if (Arrays.asList(packageBlacklist).contains(mPackageName))
				hwaIsDiabled = true;
			ApplicationInfo info = null;
			try {
				info = mPackageManager.getApplicationInfo(mPackageName,
						PackageManager.GET_META_DATA);
			} catch (NameNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
			values.put(PackageListProvider.APPLICATION_LABEL,
					(String) mPackageManager.getApplicationLabel(info));
			values.put(PackageListProvider.PACKAGE_NAME, mPackageName);
			values.put(PackageListProvider.HWA_DISABLED,
					String.valueOf(hwaIsDiabled));
			mContentResolver.insert(Uri.withAppendedPath(
					PackageListProvider.PACKAGE_URI, mPackageName),
					values);
			return null;
		}
	}

}