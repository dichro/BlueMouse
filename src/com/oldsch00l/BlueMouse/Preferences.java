package com.oldsch00l.BlueMouse;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.PreferenceActivity;

public class Preferences extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	public static final String KEY_UPDATE_INTERVAL = "update_interval";
	public static final String CONNECT_LIST = "force_connection";

	private EditTextPreference mEditPrefUpdateInterval;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preference);

		mEditPrefUpdateInterval = (EditTextPreference)getPreferenceScreen().findPreference(KEY_UPDATE_INTERVAL);
		doBindService();
	}

//	private String getStringResourceByName(String aString)
//	{
//	  String packageName = "com.oldsch00l.BlueMouse";
//	  int resId = getResources().getIdentifier(aString, "string", packageName);
//	  return getString(resId);
//	}

	@Override
	protected void onPause() {
		super.onPause();

		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
	}

	@Override
	protected void onResume() {
		super.onResume();

		SharedPreferences sp = getPreferenceScreen().getSharedPreferences();
		mEditPrefUpdateInterval.setSummary(sp.getString(KEY_UPDATE_INTERVAL, "2000") + " ms");

		getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

		BluetoothAdapter ba = BluetoothAdapter.getDefaultAdapter();
		Set<BluetoothDevice> devices = ba.getBondedDevices();
		int size = devices.size();
		List<CharSequence> names = new ArrayList<CharSequence>(size);
		List<CharSequence> values = new ArrayList<CharSequence>(size);
		names.add("Disabled");
		values.add("disabled");
		for (BluetoothDevice dev : devices) {
			names.add(dev.getName());
			values.add(dev.getAddress());
		}
		ListPreference connections = (ListPreference)getPreferenceScreen().findPreference(CONNECT_LIST);
		connections.setEntries(names.toArray(new CharSequence[size]));
		connections.setEntryValues(values.toArray(new CharSequence[size]));
	}

	@Override
	public void onDestroy() {
		doUnbindService();
		super.onDestroy();
	}

	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		if(key.equals(KEY_UPDATE_INTERVAL)) {
			mEditPrefUpdateInterval.setSummary(sharedPreferences.getString(KEY_UPDATE_INTERVAL, "2000") + " ms");
		}
		if (key.equals(CONNECT_LIST) && mSerialService != null) {
			mSerialService.connect(sharedPreferences.getString(CONNECT_LIST,
					"disabled"));
		}
	}

	private BlueMouseService mSerialService = null;
	private boolean mIsBound;
	private ServiceConnection mServiceCon = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mSerialService = ((BlueMouseService.BluetoothSerialBinder) service)
					.getService();
		}

		@Override
		public void onServiceDisconnected(ComponentName name) {
			mSerialService = null;
		}
	};

	void doBindService() {
		// Establish a connection with the service. We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		bindService(new Intent(this, BlueMouseService.class), mServiceCon,
				Context.BIND_AUTO_CREATE);
		mIsBound = true;
	}

	void doUnbindService() {
		if (mIsBound) {
			// Detach our existing connection.
			unbindService(mServiceCon);
			stopService(new Intent(this, BlueMouseService.class));
			mIsBound = false;
		}
	}
}
