/*
 * Copyright (C) 2015-2017 Willi Ye <williye97@gmail.com>
 *
 * This file is part of Kernel Adiutor.
 *
 * Kernel Adiutor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kernel Adiutor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kernel Adiutor.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package com.grarak.kerneladiutor.activities;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.ads.MobileAds;
import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.database.tools.profiles.Profiles;
import com.grarak.kerneladiutor.services.profile.Tile;
import com.grarak.kerneladiutor.utils.AppSettings;
import com.grarak.kerneladiutor.utils.Device;
import com.grarak.kerneladiutor.utils.Prefs;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.kernel.battery.Battery;
import com.grarak.kerneladiutor.utils.kernel.cpu.CPUBoost;
import com.grarak.kerneladiutor.utils.kernel.cpu.CPUFreq;
import com.grarak.kerneladiutor.utils.kernel.cpu.MSMPerformance;
import com.grarak.kerneladiutor.utils.kernel.cpu.Temperature;
import com.grarak.kerneladiutor.utils.kernel.cpuhotplug.Hotplug;
import com.grarak.kerneladiutor.utils.kernel.cpuhotplug.QcomBcl;
import com.grarak.kerneladiutor.utils.kernel.cpuvoltage.Voltage;
import com.grarak.kerneladiutor.utils.kernel.gpu.GPU;
import com.grarak.kerneladiutor.utils.kernel.gpu.GPUFreq;
import com.grarak.kerneladiutor.utils.kernel.io.IO;
import com.grarak.kerneladiutor.utils.kernel.ksm.KSM;
import com.grarak.kerneladiutor.utils.kernel.misc.Vibration;
import com.grarak.kerneladiutor.utils.kernel.screen.Screen;
import com.grarak.kerneladiutor.utils.kernel.sound.Sound;
import com.grarak.kerneladiutor.utils.kernel.thermal.Thermal;
import com.grarak.kerneladiutor.utils.kernel.wake.Wake;
import com.grarak.kerneladiutor.utils.root.RootUtils;

import com.sammy.etweaks.utils.kernel.cpuvoltage.VoltageCl0;
import com.sammy.etweaks.utils.kernel.cpuvoltage.VoltageCl1;
import com.smartpack.kernelmanager.utils.Wakelocks;

import com.sammy.etweaks.fragments.kernel.BusCamFragment;
import com.sammy.etweaks.fragments.kernel.BusDispFragment;
import com.sammy.etweaks.fragments.kernel.BusIntFragment;
import com.sammy.etweaks.fragments.kernel.BusMifFragment;
import com.sammy.etweaks.utils.kernel.bus.VoltageCam;
import com.sammy.etweaks.utils.kernel.bus.VoltageDisp;
import com.sammy.etweaks.utils.kernel.bus.VoltageInt;
import com.sammy.etweaks.utils.kernel.bus.VoltageMif;

import org.frap129.spectrum.Spectrum;

import java.lang.ref.WeakReference;

/**
 * Created by willi on 14.04.16.
 */
public class MainActivity extends BaseActivity {

    private TextView mRootAccess;
    private TextView mBusybox;
    private TextView mCollectInfo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /**
         * Initialize Spectrum Profiles & Wakelock Blocker
         */
        if (RootUtils.rootAccess()) {
            if (Spectrum.supported()) {
                int Profile = Utils.strToInt(Spectrum.getProfile());
                Prefs.saveInt("spectrum_profile", Profile, this);
            }
            if (Wakelocks.boefflawlsupported()) {
                Wakelocks.CopyWakelockBlockerDefault();
            }
        }

        // Check if exist /data/.mtweaks folder
        if (!Utils.existFile("/data/.mtweaks")) {
            RootUtils.runCommand("mkdir /data/.mtweaks");
        }

        // Check is kernel is changed
        String kernel_old = AppSettings.getString("kernel_version_old", "", this);
        String kernel_new = Device.getKernelVersion(true);

        if (!kernel_old.equals(kernel_new)) {
            AppSettings.saveString("kernel_version_old", kernel_new, this);
            AppSettings.saveBoolean("cl0_voltage_saved", false, this);
            AppSettings.saveBoolean("cl1_voltage_saved", false, this);
            AppSettings.saveBoolean("gpu_voltage_saved", false, this);
            AppSettings.saveBoolean("busMif_voltage_saved", false, this);
            AppSettings.saveBoolean("busInt_voltage_saved", false, this);
            AppSettings.saveBoolean("busDisp_voltage_saved", false, this);
            AppSettings.saveBoolean("busCam_voltage_saved", false, this);
        }

        // Save backup of Cluster0 stock voltages
        if (!Utils.existFile(VoltageCl0.BACKUP) || !AppSettings.getBoolean("cl0_voltage_saved", false, this) ){
            if (VoltageCl0.supported()){
                RootUtils.runCommand("cp " + VoltageCl0.CL0_VOLTAGE + " " + VoltageCl0.BACKUP);
                AppSettings.saveBoolean("cl0_voltage_saved", true, this);
            }
        }

        // Save backup of Cluster1 stock voltages
        if (!Utils.existFile(VoltageCl1.BACKUP) || !AppSettings.getBoolean("cl1_voltage_saved", false, this)){
            if (VoltageCl1.supported()){
                RootUtils.runCommand("cp " + VoltageCl1.CL1_VOLTAGE + " " + VoltageCl1.BACKUP);
                AppSettings.saveBoolean("cl1_voltage_saved", true, this);
            }
        }

        // Save backup of Bus Mif stock voltages
        if (!Utils.existFile(VoltageMif.BACKUP) || !AppSettings.getBoolean("busMif_voltage_saved", false, this)){
            if (VoltageMif.supported()){
                RootUtils.runCommand("cp " + VoltageMif.VOLTAGE + " " + VoltageMif.BACKUP);
                AppSettings.saveBoolean("busMif_voltage_saved", true, this);
            }
        }

        // Save backup of Bus Int stock voltages
        if (!Utils.existFile(VoltageInt.BACKUP) || !AppSettings.getBoolean("busInt_voltage_saved", false, this)){
            if (VoltageInt.supported()){
                RootUtils.runCommand("cp " + VoltageInt.VOLTAGE + " " + VoltageInt.BACKUP);
                AppSettings.saveBoolean("busInt_voltage_saved", true, this);
            }
        }

        // Save backup of Bus Disp stock voltages
        if (!Utils.existFile(VoltageDisp.BACKUP) || !AppSettings.getBoolean("busDisp_voltage_saved", false, this)){
            if (VoltageDisp.supported()){
                RootUtils.runCommand("cp " + VoltageDisp.VOLTAGE + " " + VoltageDisp.BACKUP);
                AppSettings.saveBoolean("busDisp_voltage_saved", true, this);
            }
        }

        // Save backup of Bus Cam stock voltages
        if (!Utils.existFile(VoltageCam.BACKUP) || !AppSettings.getBoolean("busCam_voltage_saved", false, this)){
            if (VoltageCam.supported()){
                RootUtils.runCommand("cp " + VoltageCam.VOLTAGE + " " + VoltageCam.BACKUP);
                AppSettings.saveBoolean("busCam_voltage_saved", true, this);
            }
        }

        // Save backup of GPU stock voltages
        if (!AppSettings.getBoolean("gpu_voltage_saved", false, this)) {
            if (GPUFreq.supported() && GPUFreq.hasVoltage()) {
                RootUtils.runCommand("cp " + GPUFreq.AVAILABLE_S7_FREQS + " " + GPUFreq.BACKUP);
                AppSettings.saveBoolean("gpu_voltage_saved", true, this);
            }
        }

        setContentView(R.layout.activity_main);

        View splashBackground = findViewById(R.id.splash_background);
        mRootAccess = findViewById(R.id.root_access_text);
        mBusybox = findViewById(R.id.busybox_text);
        mCollectInfo = findViewById(R.id.info_collect_text);

        /**
         * Hide huge banner in landscape mode
         */
        if (Utils.getOrientation(this) == Configuration.ORIENTATION_LANDSCAPE) {
            splashBackground.setVisibility(View.GONE);
        }

        if (savedInstanceState == null) {
            /*
             * Launch password activity when one is set,
             * otherwise run {@link CheckingTask}
             */
            String password;
            if (!(password = Prefs.getString("password", "", this)).isEmpty()) {
                Intent intent = new Intent(this, SecurityActivity.class);
                intent.putExtra(SecurityActivity.PASSWORD_INTENT, password);
                startActivityForResult(intent, 0);
            } else {
                new CheckingTask(this).execute();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 0) {
            if (resultCode == 1) {
                new CheckingTask(this).execute();
            } else {
                finish();
            }
        }
    }

    private void launch() {
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        if (getIntent().getExtras() != null) {
            intent.putExtras(getIntent().getExtras());
        }
        startActivity(intent);
        finish();
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private static class CheckingTask extends AsyncTask<Void, Integer, Void> {

        private WeakReference<MainActivity> mRefActivity;

        private boolean mHasRoot;
        private boolean mHasBusybox;

        private CheckingTask(MainActivity activity) {
            mRefActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            mHasRoot = RootUtils.rootAccess();
            publishProgress(0);

            if (mHasRoot) {
                mHasBusybox = RootUtils.busyboxInstalled();
                publishProgress(1);

                if (mHasBusybox) {
                    collectData();
                    publishProgress(2);
                }
            }
            return null;
        }

        /**
         * Determinate what sections are supported
         */
        private void collectData() {
            MainActivity activity = mRefActivity.get();
            if (activity == null) return;

            Battery.getInstance(activity);
            CPUBoost.getInstance();

            // Assign core ctl min cpu
            CPUFreq.getInstance(activity);

            Device.CPUInfo.getInstance();
            Device.Input.getInstance();
            Device.MemInfo.getInstance();
            Device.ROMInfo.getInstance();
            Device.TrustZone.getInstance();
            GPU.supported();
            Hotplug.supported();
            IO.getInstance();
            KSM.getInstance();
            MSMPerformance.getInstance();
            QcomBcl.supported();
            Screen.supported();
            Sound.getInstance();
            Temperature.getInstance(activity);
            Thermal.supported();
            Tile.publishProfileTile(new Profiles(activity).getAllProfiles(), activity);
            Vibration.getInstance();
            Voltage.getInstance();
            Wake.supported();

        }

        /**
         * Let the user know what we are doing right now
         *
         * @param values progress
         *               0: Checking root
         *               1: Checking busybox/toybox
         *               2: Collecting information
         */
        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);
            MainActivity activity = mRefActivity.get();
            if (activity == null) return;

            int red = ContextCompat.getColor(activity, R.color.red);
            int green = ContextCompat.getColor(activity, R.color.green);
            switch (values[0]) {
                case 0:
                    activity.mRootAccess.setTextColor(mHasRoot ? green : red);
                    break;
                case 1:
                    activity.mBusybox.setTextColor(mHasBusybox ? green : red);
                    break;
                case 2:
                    activity.mCollectInfo.setTextColor(green);
                    break;
            }
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            MainActivity activity = mRefActivity.get();
            if (activity == null) return;

            /*
             * If root or busybox/toybox are not available,
             * launch text activity which let the user know
             * what the problem is.
             */
            if (!mHasRoot || !mHasBusybox) {
                Intent intent = new Intent(activity, TextActivity.class);
                intent.putExtra(TextActivity.MESSAGE_INTENT, activity.getString(mHasRoot ?
                        R.string.no_busybox : R.string.no_root));
                intent.putExtra(TextActivity.SUMMARY_INTENT,
                        mHasRoot ? "https://play.google.com/store/apps/details?id=stericson.busybox" :
                                "https://www.google.com/search?site=&source=hp&q=root+"
                                        + Device.getVendor() + "+" + Device.getModel());
                activity.startActivity(intent);
                activity.finish();

                return;
            }

            // Initialize Google Ads
            if (Prefs.getBoolean("allow_ads", true, activity)) {
                MobileAds.initialize(activity, "ca-app-pub-7791710838910455~4931988555");
            }

            activity.launch();
        }
    }

}
