package com.blissroms.blissify.stats.models;

import android.content.Context;
import android.os.SystemProperties;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.blissroms.blissify.stats.Constants;

public class StatsData {

    private String device;
    private String model;
    private String blissVersion;
    private String buildType;
    private String countryCode;
    private String buildDate;
    private String androidVersion;

    public String getDevice() {
        return SystemProperties.get(Constants.KEY_DEVICE);
    }

    public void setDevice(String device) {
        this.device = TextUtils.isEmpty(device) ? "unknown" : device;
    }

    public String getModel() {
        return SystemProperties.get(Constants.KEY_MODEL);
    }

    public void setModel(String model) {
        this.model = TextUtils.isEmpty(model) ? "unknown" : model;
    }

    public String getBlissVersion() {
        return Constants.KEY_BLISS_VERSION;
    }

    public void setBlissVersion(String blissVersion) {
        this.blissVersion = TextUtils.isEmpty(blissVersion) ? "unknown" : blissVersion;
    }

    public String getBuildType() {
        return SystemProperties.get(Constants.KEY_BUILD_TYPE);
    }

    public void setBuildType(String buildType) {
        this.buildType = TextUtils.isEmpty(buildType) ? "unknown" : buildType;
    }

    public String getAndroidVersion() {
        return SystemProperties.get(Constants.KEY_ANDROID_VERSION);
    }

    public void setAndroidVersion(String androidVersion) {
        this.androidVersion = TextUtils.isEmpty(androidVersion) ? "unknown" : androidVersion;
    }

    public String getCountryCode(Context context) {
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return tm.getNetworkCountryIso();
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = TextUtils.isEmpty(countryCode) ? "unknown" : countryCode;
    }

    public String getBuildDate() {
        return SystemProperties.get(Constants.KEY_BUILD_DATE);
    }

    public void setBuildDate(String buildDate) {
        this.buildDate = TextUtils.isEmpty(buildDate) ? "unknown" : buildDate;
    }
}
