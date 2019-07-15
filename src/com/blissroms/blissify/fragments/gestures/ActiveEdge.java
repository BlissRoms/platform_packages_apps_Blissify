/*
 * Copyright (C) 2019 The Dirty Unicorns Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.blissroms.blissify.fragments.gestures;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.provider.Settings;
import android.support.v7.preference.PreferenceCategory;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v14.preference.SwitchPreference;

import com.android.internal.logging.nano.MetricsProto;
import com.android.internal.utils.ActionHandler;
import com.android.settings.R;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.smartnav.SimpleActionFragment;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import com.bliss.support.preferences.CustomSeekBarPreference;

import java.util.ArrayList;
import java.util.List;

public class ActiveEdge extends SimpleActionFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_SQUEEZE_SMART_ACTION = "squeeze_selection_smart_action";
    private static final String KEY_LONG_SQUEEZE_SMART_ACTION = "long_squeeze_selection_smart_action";

    private CustomSeekBarPreference mActiveEdgeSensitivity;
    private ListPreference mActiveEdgeActions;
    private SwitchPreference mActiveEdgeWake;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.active_edge);

        final ContentResolver resolver = getActivity().getContentResolver();


        int sensitivity = Settings.Secure.getIntForUser(resolver,
                Settings.Secure.ASSIST_GESTURE_SENSITIVITY, 2, UserHandle.USER_CURRENT);
        mActiveEdgeSensitivity = (CustomSeekBarPreference) findPreference("gesture_assist_sensitivity");
        mActiveEdgeSensitivity.setValue(sensitivity);
        mActiveEdgeSensitivity.setOnPreferenceChangeListener(this);

        mActiveEdgeWake = (SwitchPreference) findPreference("gesture_assist_wake");
        mActiveEdgeWake.setChecked((Settings.Secure.getIntForUser(resolver,
                Settings.Secure.ASSIST_GESTURE_WAKE_ENABLED, 1,
                UserHandle.USER_CURRENT) == 1));
        mActiveEdgeWake.setOnPreferenceChangeListener(this);

        onPreferenceScreenLoaded(null);
    }


    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference == mActiveEdgeSensitivity) {
            int val = (Integer) newValue;
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.ASSIST_GESTURE_SENSITIVITY, val,
                    UserHandle.USER_CURRENT);
            return true;
        } else if (preference == mActiveEdgeWake) {
            Settings.Secure.putIntForUser(getContentResolver(),
                    Settings.Secure.ASSIST_GESTURE_WAKE_ENABLED,
                    (Boolean) newValue ? 1 : 0,
                    UserHandle.USER_CURRENT);
            return true;
        }
        return false;
    }

    @Override
    protected ActionPreferenceInfo getActionPreferenceInfoForKey(String key) {
        if (key.equals(KEY_SQUEEZE_SMART_ACTION)) {
            return new ActionPreferenceInfo(getActivity(),
                    ActionPreferenceInfo.TYPE_SECURE,
                    ActionHandler.SYSTEMUI_TASK_NO_ACTION,
                    Settings.Secure.SQUEEZE_SELECTION_SMART_ACTIONS);
        } else if (key.equals(KEY_LONG_SQUEEZE_SMART_ACTION)) {
            return new ActionPreferenceInfo(getActivity(),
                    ActionPreferenceInfo.TYPE_SECURE,
                    ActionHandler.SYSTEMUI_TASK_NO_ACTION,
                    Settings.Secure.LONG_SQUEEZE_SELECTION_SMART_ACTIONS);
        }
        return null;
    }

    @Override
    protected ArrayList<String> getActionBlackListForPreference(String key) {
        if (key.equals(KEY_SQUEEZE_SMART_ACTION) || key.equals(KEY_LONG_SQUEEZE_SMART_ACTION)) {
            ArrayList<String> blacklist = new ArrayList();
            blacklist.add(ActionHandler.SYSTEMUI_TASK_BACK);
            blacklist.add(ActionHandler.SYSTEMUI_TASK_KILL_PROCESS);
            blacklist.add(ActionHandler.SYSTEMUI_TASK_EDITING_SMARTBAR);
            blacklist.add(ActionHandler.SYSTEMUI_TASK_WIFIAP);
            blacklist.add(ActionHandler.SYSTEMUI_TASK_MENU);
            blacklist.add(ActionHandler.SYSTEMUI_TASK_APP_SEARCH);
            return blacklist;
        }
        return null;
    }

    @Override
    public int getMetricsCategory() {
        return MetricsProto.MetricsEvent.BLISSIFY;
    }
}
