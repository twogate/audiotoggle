package com.twogate.audiotoggle;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;


import android.content.Context;
import android.media.AudioManager;
import android.media.AudioDeviceCallback;
import android.media.AudioDeviceInfo;
import android.util.Log;

public class AudioTogglePlugin extends CordovaPlugin {
	private AudioManager audioManager;
	private static final String TAG = "AudioTogglePlugin";
	private int oldAudioMode;
	private boolean oldIsSpeakerPhoneOn;

	@Override
	public void onDestroy() {
		Log.i(TAG, "DESTROY");

		// 最初のオーディオ状態をリストア
		audioManager.setSpeakerphoneOn(oldIsSpeakerPhoneOn);
		audioManager.setMode(oldAudioMode);
	}

	public void updateDevices(boolean isNewDevices, AudioDeviceInfo[] devices) {
		if (isNewDevices) {
			Log.i(TAG, "new devices!");
			// 新規デバイス接続の通知であれば そのデバイスリストをみてヘッドフォンがあればそれにルーティング
			if (this.isContainsHeadphone(devices)) {
				this.switchToDefault();
			} else {
				this.switchToEarpiece();
			}
		} else {
			Log.i(TAG, "discon!!");
			// デバイス接続解除であれば 現状をみて適切なデバイスにルーティング
			this.changeRoute();
		}
	}

	@Override
	public boolean execute(String action, JSONArray args,
			CallbackContext callbackContext) throws JSONException {
		this.setAutoRoute();
		return true;
	}

	private void switchToEarpiece() {
        Log.i(TAG, "-> Earpiece");

		audioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
		audioManager.setSpeakerphoneOn(false);
	}

	private void switchToDefault() {
        Log.i(TAG, "-> Default");

		audioManager.setMode(AudioManager.MODE_NORMAL);
		audioManager.setSpeakerphoneOn(false);
	}

	// https://stackoverflow.com/a/45726363
	private boolean isContainsHeadphone(AudioDeviceInfo[] devices) {
		for (int i = 0; i < devices.length; i++) {
			AudioDeviceInfo device = devices[i];
			if (!device.isSink()) // 出力デバイスでなければ
				continue;
			if (device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADPHONES
			        || device.getType() == AudioDeviceInfo.TYPE_WIRED_HEADSET
					|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
					|| device.getType() == AudioDeviceInfo.TYPE_BLUETOOTH_SCO
					|| device.getType() == AudioDeviceInfo.TYPE_USB_HEADSET) {
				return true;
			}
		}
		return false;
	}

	// https://stackoverflow.com/a/45726363
	private boolean isHeadsetPluggedIn() {
		AudioDeviceInfo[] devices = audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
		return isContainsHeadphone(devices);
	}

	// 現状のデバイスリストから適切なものを選択
	private void changeRoute() {
		if (this.isHeadsetPluggedIn()) {
			Log.i(TAG, "Plugged In!");

			this.switchToDefault();
		} else {
			Log.i(TAG, "Not Plugged In!");
			this.switchToEarpiece();
		}
	}

	public void setAutoRoute() {
		audioManager =
				(AudioManager) webView.getContext().getSystemService(Context.AUDIO_SERVICE);

		oldAudioMode = audioManager.getMode();
		oldIsSpeakerPhoneOn = audioManager.isSpeakerphoneOn();

		AudioDeviceCallback mAudioDeviceCallback = createAudioDeviceCallback();

		mAudioDeviceCallback.onAudioDevicesAdded(audioManager.getDevices(AudioManager.GET_DEVICES_OUTPUTS));
		audioManager.registerAudioDeviceCallback(mAudioDeviceCallback, null);

		this.changeRoute();
	}

	private AudioDeviceCallback createAudioDeviceCallback() {
		return new AudioDeviceCallback() {

			@Override
			public void onAudioDevicesAdded(AudioDeviceInfo[] addedDevices) {
				updateDevices(true, addedDevices);
			}

			@Override
			public void onAudioDevicesRemoved(AudioDeviceInfo[] removedDevices) {
				updateDevices(false, removedDevices);
			}
		};
	}
}
