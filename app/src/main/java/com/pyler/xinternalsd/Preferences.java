package com.pyler.xinternalsd;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.EditTextPreference;
import android.preference.MultiSelectListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
//主页面：设置页 通过SharedPreferences保存配置
public class Preferences extends Activity {
    public static Context context;
    public static SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getApplicationContext();
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new Settings()).commit();
    }

    @SuppressWarnings("deprecation")
    public static class Settings extends PreferenceFragment {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            getPreferenceManager()
                    .setSharedPreferencesMode(MODE_WORLD_READABLE);
            addPreferencesFromResource(R.xml.preferences);
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
            PreferenceCategory appSettings = (PreferenceCategory) findPreference("app_settings");
            Preference externalSdCardFullAccess = findPreference("external_sdcard_full_access");
            EditTextPreference internalSdPath = (EditTextPreference) findPreference("internal_sdcard_path");
//            Preference includeSystemApps = findPreference("include_system_apps");

            String internalSd = prefs.getString("internal_sdcard_path", "/sdcard/Android/XInternalSDFile");
            if (!internalSd.isEmpty()) {
                internalSd = Common.appendFileSeparator(internalSd);
                internalSdPath.setSummary(internalSd);
                internalSdPath.setText(internalSd);
            }

            internalSdPath.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(
                        Preference preference, Object newValue) {
                    String newPath = (String) newValue;
                    if (newPath.isEmpty()) {
                        newPath = getString(R.string.enter_internal_sdcard_path);
                        preference.setSummary(newPath);
                    } else {
                        newPath = Common.appendFileSeparator(newPath);
                    }
                    preference.setSummary(newPath);
                    return true;
                }
            });

//            includeSystemApps
//                    .setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                        @Override
//                        public boolean onPreferenceChange(
//                                Preference preference, Object newValue) {
//                            reloadAppsList();
//                            return true;
//                        }
//                    });
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                externalSdCardFullAccess.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object newValue) {
                        Toast.makeText(context, R.string.reboot_required, Toast.LENGTH_LONG).show();
                        return true;
                    }
                });
            }

//            reloadAppsList();

            String customInternalSd = prefs.getString("internal_sdcard_path",
                    "");
            if (!customInternalSd.isEmpty()) { // not empty
                internalSdPath.setSummary(customInternalSd);
            } else { // empty, try to detect it
                String externalSd = null;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    File[] dirs = context.getExternalMediaDirs();
                    for (File dir : dirs) {
                        if (dir == null || !dir.exists()) {
                            continue;
                        }
                        if (Environment.isExternalStorageRemovable(dir)) {
                            String absolutePath = dir.getAbsolutePath();
                            int end = absolutePath.indexOf("/Android/");
                            externalSd = absolutePath.substring(0, end);
                        }
                    }
                } else {
                    String externalStorage = System.getenv("SECONDARY_STORAGE");
                    if (externalStorage != null && !externalStorage.isEmpty()) {
                        externalSd = externalStorage.split(":")[0];
                    }
                }

                if (externalSd != null && !externalSd.isEmpty()) {
                    externalSd = Common.appendFileSeparator(externalSd);
                    internalSdPath.setSummary(externalSd);
                    internalSdPath.setText(externalSd);
                    prefs.edit().putString("internal_sdcard_path", externalSd).apply();
                }
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                appSettings.removePreference(externalSdCardFullAccess);
            }

//            Preference showAppIcon = findPreference("show_app_icon");
//            showAppIcon.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
//                @Override
//                public boolean onPreferenceChange(
//                        Preference preference, Object newValue) {
//                    PackageManager packageManager = context.getPackageManager();
//                    int state = (boolean) newValue ? PackageManager.COMPONENT_ENABLED_STATE_ENABLED
//                            : PackageManager.COMPONENT_ENABLED_STATE_DISABLED;
//                    String settings = BuildConfig.APPLICATION_ID + ".Settings";
//                    ComponentName alias = new ComponentName(context, settings);
//                    packageManager.setComponentEnabledSetting(alias, state,
//                            PackageManager.DONT_KILL_APP);
//                    return true;
//                }
//            });
        }

        @Override
        public void onPause() {
            super.onPause();

            // Set preferences file permissions to be world readable
            File prefsDir = new File(getActivity().getApplicationInfo().dataDir, "shared_prefs");
            File prefsFile = new File(prefsDir, getPreferenceManager().getSharedPreferencesName() + ".xml");
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false);
            }
        }

//        public void reloadAppsList() {
//            new LoadApps().execute();
//        }

//        public boolean isAllowedApp(ApplicationInfo appInfo) {
//            boolean includeSystemApps = prefs.getBoolean("include_system_apps",
//                    false);
//            if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0
//                    && !includeSystemApps) {
//                return false;
//            }
//
//            if (Arrays.asList(Common.MTP_APPS).contains(appInfo.packageName)) {
//                return false;
//            }
//            return true;
//        }
//
//        public class LoadApps extends AsyncTask<Void, Void, Void> {
////            MultiSelectListPreference enabledApps = (MultiSelectListPreference) findPreference("enable_for_apps");
////            MultiSelectListPreference disabledApps = (MultiSelectListPreference) findPreference("disable_for_apps");
//            List<CharSequence> appNames = new ArrayList<>();
//            List<CharSequence> packageNames = new ArrayList<>();
//            PackageManager pm = context.getPackageManager();
//            List<ApplicationInfo> packages = pm
//                    .getInstalledApplications(PackageManager.GET_META_DATA);
//
//            @Override
//            protected void onPreExecute() {
//                enabledApps.setEnabled(false);
////                disabledApps.setEnabled(false);
//            }
//
//            @Override
//            protected Void doInBackground(Void... arg0) {
//                List<String[]> sortedApps = new ArrayList<>();
//
//                for (ApplicationInfo app : packages) {
//                    if (isAllowedApp(app)) {
//                        sortedApps.add(new String[]{
//                                app.packageName,
//                                app.loadLabel(pm)
//                                        .toString()});
//                    }
//                }
//
//                Collections.sort(sortedApps, new Comparator<String[]>() {
//                    @Override
//                    public int compare(String[] entry1, String[] entry2) {
//                        return entry1[1].compareToIgnoreCase(entry2[1]);
//                    }
//                });
//
//                for (int i = 0; i < sortedApps.size(); i++) {
//                    appNames.add(sortedApps.get(i)[1] + "\n" + "(" + sortedApps.get(i)[0] + ")");
//                    packageNames.add(sortedApps.get(i)[0]);
//                }
//
//                return null;
//            }
//
//            @Override
//            protected void onPostExecute(Void result) {
//                CharSequence[] appNamesList = appNames
//                        .toArray(new CharSequence[appNames.size()]);
//                CharSequence[] packageNamesList = packageNames
//                        .toArray(new CharSequence[packageNames.size()]);
//
//                enabledApps.setEntries(appNamesList);
//                enabledApps.setEntryValues(packageNamesList);
//                enabledApps.setEnabled(true);
////                disabledApps.setEntries(appNamesList);
////                disabledApps.setEntryValues(packageNamesList);
////                disabledApps.setEnabled(true);
//
//                Preference.OnPreferenceClickListener listener = new Preference.OnPreferenceClickListener() {
//                    @Override
//                    public boolean onPreferenceClick(Preference preference) {
//                        ((MultiSelectListPreference) preference).getDialog().getWindow().setLayout(WindowManager.LayoutParams.FILL_PARENT, WindowManager.LayoutParams.FILL_PARENT);
//                        return false;
//                    }
//                };
//
//                enabledApps.setOnPreferenceClickListener(listener);
////                disabledApps.setOnPreferenceClickListener(listener);
//            }
//        }

    }
}
