/*
 * Copyright (C) 2015-2016 Willi Ye <williye97@gmail.com>
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
package com.grarak.kerneladiutor.utils.kernel.gpu;

import android.content.Context;

import com.grarak.kerneladiutor.R;
import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;
import com.grarak.kerneladiutor.utils.root.RootUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * Created by willi on 12.05.16.
 */
public class GPUFreq {

    private static GPUFreq sIOInstance;

    public static GPUFreq getInstance() {
        if (sIOInstance == null) {
            sIOInstance = new GPUFreq();
        }
        return sIOInstance;
    }

    private static final String BACKUP = "/data/.mtweaks/gpu_stock_voltage";

    private static final String GENERIC_GOVERNORS = "performance powersave ondemand simple conservative";

    private static final String CUR_KGSL2D0_QCOM_FREQ = "/sys/devices/platform/kgsl-2d0.0/kgsl/kgsl-2d0/gpuclk";
    private static final String MAX_KGSL2D0_QCOM_FREQ = "/sys/devices/platform/kgsl-2d0.0/kgsl/kgsl-2d0/max_gpuclk";
    private static final String AVAILABLE_KGSL2D0_QCOM_FREQS = "/sys/devices/platform/kgsl-2d0.0/kgsl/kgsl-2d0/gpu_available_frequencies";
    private static final String SCALING_KGSL2D0_QCOM_GOVERNOR = "/sys/devices/platform/kgsl-2d0.0/kgsl/kgsl-2d0/pwrscale/trustzone/governor";

    private static final String KGSL3D0_GPUBUSY = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpubusy";
    private static final String CUR_KGSL3D0_FREQ = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpuclk";
    private static final String MAX_KGSL3D0_FREQ = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/max_gpuclk";
    private static final String AVAILABLE_KGSL3D0_FREQS = "/sys/devices/platform/kgsl-3d0.0/kgsl/kgsl-3d0/gpu_available_frequencies";
    private static final String SCALING_KGSL3D0_GOVERNOR = "/sys/class/kgsl/kgsl-3d0/pwrscale/trustzone/governor";

    private static final String KGSL3D0_DEVFREQ_GPUBUSY = "/sys/class/kgsl/kgsl-3d0/gpubusy";
    private static final String CUR_KGSL3D0_DEVFREQ_FREQ = "/sys/class/kgsl/kgsl-3d0/gpuclk";
    private static final String MAX_KGSL3D0_DEVFREQ_FREQ = "/sys/class/kgsl/kgsl-3d0/max_gpuclk";
    private static final String MIN_KGSL3D0_DEVFREQ_FREQ = "/sys/class/kgsl/kgsl-3d0/devfreq/min_freq";
    private static final String AVAILABLE_KGSL3D0_DEVFREQ_FREQS = "/sys/class/kgsl/kgsl-3d0/gpu_available_frequencies";
    private static final String SCALING_KGSL3D0_DEVFREQ_GOVERNOR = "/sys/class/kgsl/kgsl-3d0/devfreq/governor";
    private static final String AVAILABLE_KGSL3D0_DEVFREQ_GOVERNORS = "/sys/class/kgsl/kgsl-3d0/devfreq/available_governors";

    private static final String CUR_OMAP_FREQ = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency";
    private static final String MAX_OMAP_FREQ = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_limit";
    private static final String AVAILABLE_OMAP_FREQS = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/frequency_list";
    private static final String SCALING_OMAP_GOVERNOR = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/governor";
    private static final String AVAILABLE_OMAP_GOVERNORS = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/governor_list";
    private static final String TUNABLES_OMAP = "/sys/devices/platform/omap/pvrsrvkm.0/sgxfreq/%s";

    private static final String CUR_TEGRA_FREQ = "/sys/kernel/tegra_gpu/gpu_rate";
    private static final String MAX_TEGRA_FREQ = "/sys/kernel/tegra_gpu/gpu_cap_rate";
    private static final String MIN_TEGRA_FREQ = "/sys/kernel/tegra_gpu/gpu_floor_rate";
    private static final String AVAILABLE_TEGRA_FREQS = "/sys/kernel/tegra_gpu/gpu_available_rates";

    private static final String CUR_POWERVR_FREQ = "/sys/devices/platform/dfrgx/devfreq/dfrgx/cur_freq";
    private static final String MAX_POWERVR_FREQ = "/sys/devices/platform/dfrgx/devfreq/dfrgx/max_freq";
    private static final String MIN_POWERVR_FREQ = "/sys/devices/platform/dfrgx/devfreq/dfrgx/min_freq";
    private static final String AVAILABLE_POWERVR_FREQS = "/sys/devices/platform/dfrgx/devfreq/dfrgx/available_frequencies";
    private static final String SCALING_POWERVR_GOVERNOR = "/sys/devices/platform/dfrgx/devfreq/dfrgx/governor";
    private static final String AVAILABLE_POWERVR_GOVERNORS = "/sys/devices/platform/dfrgx/devfreq/dfrgx/available_governors";

    private static final String MAX_S7_FREQ = "/sys/devices/14ac0000.mali/max_clock";
    private static final String MIN_S7_FREQ = "/sys/devices/14ac0000.mali/min_clock";
    private static final String CUR_S7_FREQ = "/sys/devices/14ac0000.mali/clock";
    private static final String AVAILABLE_S7_FREQS = "/sys/devices/14ac0000.mali/volt_table";
    private static final String AVAILABLE_S7_GOVERNORS = "/sys/devices/14ac0000.mali/dvfs_governor";
    private static final String TUNABLE_HIGHSPEED_CLOCK = "/sys/devices/14ac0000.mali/highspeed_clock";
    private static final String TUNABLE_HIGHSPEED_LOAD = "/sys/devices/14ac0000.mali/highspeed_load";
    private static final String TUNABLE_HIGHSPEED_DELAY = "/sys/devices/14ac0000.mali/highspeed_delay";

    private final List<String> mGpuBusys = new ArrayList<>();
    private final HashMap<String, Integer> mCurrentFreqs = new HashMap<>();
    private final HashMap<String, Integer> mMaxFreqs = new HashMap<>();
    private final HashMap<String, Integer> mMinFreqs = new HashMap<>();
    private static final HashMap<String, Integer> mAvailableFreqs = new HashMap<>();
    private final List<String> mScalingGovernors = new ArrayList<>();
    private final List<String> mAvailableGovernors = new ArrayList<>();
    private final List<String> mTunables = new ArrayList<>();

    {
        mGpuBusys.add(KGSL3D0_GPUBUSY);
        mGpuBusys.add(KGSL3D0_DEVFREQ_GPUBUSY);

        mCurrentFreqs.put(CUR_KGSL3D0_FREQ, 1000000);
        mCurrentFreqs.put(CUR_KGSL3D0_DEVFREQ_FREQ, 1000000);
        mCurrentFreqs.put(CUR_OMAP_FREQ, 1000000);
        mCurrentFreqs.put(CUR_TEGRA_FREQ, 1000000);
        mCurrentFreqs.put(CUR_POWERVR_FREQ, 1000);
        mCurrentFreqs.put(CUR_S7_FREQ, 1);

        mMaxFreqs.put(MAX_KGSL3D0_FREQ, 1000000);
        mMaxFreqs.put(MAX_KGSL3D0_DEVFREQ_FREQ, 1000000);
        mMaxFreqs.put(MAX_OMAP_FREQ, 1000000);
        mMaxFreqs.put(MAX_TEGRA_FREQ, 1000000);
        mMaxFreqs.put(MAX_POWERVR_FREQ, 1000);
        mMaxFreqs.put(MAX_S7_FREQ, 1);

        mMinFreqs.put(MIN_KGSL3D0_DEVFREQ_FREQ, 1000000);
        mMinFreqs.put(MIN_TEGRA_FREQ, 1000000);
        mMinFreqs.put(MIN_POWERVR_FREQ, 1000);
        mMinFreqs.put(MIN_S7_FREQ, 1);

        mAvailableFreqs.put(AVAILABLE_KGSL3D0_FREQS, 1000000);
        mAvailableFreqs.put(AVAILABLE_KGSL3D0_DEVFREQ_FREQS, 1000000);
        mAvailableFreqs.put(AVAILABLE_OMAP_FREQS, 1000000);
        mAvailableFreqs.put(AVAILABLE_TEGRA_FREQS, 1000000);
        mAvailableFreqs.put(AVAILABLE_POWERVR_FREQS, 1000);
        mAvailableFreqs.put(AVAILABLE_S7_FREQS, 1);

        mScalingGovernors.add(SCALING_KGSL3D0_GOVERNOR);
        mScalingGovernors.add(SCALING_KGSL3D0_DEVFREQ_GOVERNOR);
        mScalingGovernors.add(SCALING_OMAP_GOVERNOR);
        mScalingGovernors.add(SCALING_POWERVR_GOVERNOR);
        mScalingGovernors.add(AVAILABLE_S7_GOVERNORS);

        mAvailableGovernors.add(AVAILABLE_KGSL3D0_DEVFREQ_GOVERNORS);
        mAvailableGovernors.add(AVAILABLE_OMAP_GOVERNORS);
        mAvailableGovernors.add(AVAILABLE_POWERVR_GOVERNORS);
        mAvailableGovernors.add(AVAILABLE_S7_GOVERNORS);

        mTunables.add(TUNABLES_OMAP);
    }

    private String BUSY;
    private String CUR_FREQ;
    private int CUR_FREQ_OFFSET;
    private static List<Integer> AVAILABLE_FREQS;
    private static List<Integer> AVAILABLE_FREQS_SORT;
    private String MAX_FREQ;
    private int MAX_FREQ_OFFSET;
    private String MIN_FREQ;
    private int MIN_FREQ_OFFSET;
    private String GOVERNOR;
    private String[] AVAILABLE_GOVERNORS;
    private static int AVAILABLE_GOVERNORS_OFFSET;
    private String TUNABLES;

    private static String SPLIT_NEW_LINE = "\\r?\\n";
    private static String SPLIT_LINE = " ";
    private static Integer VOLT_OFFSET = 1000;

    private Integer[] AVAILABLE_2D_FREQS;

    private GPUFreq() {
        for (String file : mGpuBusys) {
            if (Utils.existFile(file)) {
                BUSY = file;
                break;
            }
        }

        for (String file : mCurrentFreqs.keySet()) {
            if (Utils.existFile(file)) {
                CUR_FREQ = file;
                CUR_FREQ_OFFSET = mCurrentFreqs.get(file);
                break;
            }
        }

        for (String file : mAvailableFreqs.keySet()) {
            if (Utils.existFile(file)) {
                String freqs[] = Utils.readFile(file).split(" ");
                AVAILABLE_FREQS = new ArrayList<>();
                for (String freq : freqs) {
                    if (!AVAILABLE_FREQS.contains(Utils.strToInt(freq))) {
                        AVAILABLE_FREQS.add(Utils.strToInt(freq));
                    }
                }
                AVAILABLE_GOVERNORS_OFFSET = mAvailableFreqs.get(file);
                Collections.sort(AVAILABLE_FREQS);
                break;
            }
        }

        for (String file : mMaxFreqs.keySet()) {
            if (Utils.existFile(file)) {
                MAX_FREQ = file;
                MAX_FREQ_OFFSET = mMaxFreqs.get(file);
                break;
            }
        }

        for (String file : mMinFreqs.keySet()) {
            if (Utils.existFile(file)) {
                MIN_FREQ = file;
                MIN_FREQ_OFFSET = mMinFreqs.get(file);
                break;
            }
        }

        for (String file : mScalingGovernors) {
            if (Utils.existFile(file)) {
                GOVERNOR = file;
                break;
            }
        }

        for (String file : mAvailableGovernors) {
            if (Utils.existFile(file)) {
                AVAILABLE_GOVERNORS = Utils.readFile(file).split(" ");
                break;
            }
        }
        if (AVAILABLE_GOVERNORS == null) {
            AVAILABLE_GOVERNORS = GENERIC_GOVERNORS.split(" ");
        }

        for (String tunables : mTunables) {
            if (Utils.existFile(Utils.strFormat(tunables, ""))) {
                TUNABLES = tunables;
                break;
            }
        }
    }

    public String getTunables(String governor) {
        return Utils.strFormat(TUNABLES, governor);
    }

    public boolean hasTunables(String governor) {
        return TUNABLES != null;
    }

    public void set2dGovernor(String value, Context context) {
        run(Control.write(value, SCALING_KGSL2D0_QCOM_GOVERNOR), SCALING_KGSL2D0_QCOM_GOVERNOR, context);
    }

    public String get2dGovernor() {
        return Utils.readFile(SCALING_KGSL2D0_QCOM_GOVERNOR);
    }

    public List<String> get2dAvailableGovernors() {
        return Arrays.asList(GENERIC_GOVERNORS.split(" "));
    }

    public boolean has2dGovernor() {
        return Utils.existFile(SCALING_KGSL2D0_QCOM_GOVERNOR);
    }

    public void set2dMaxFreq(int value, Context context) {
        run(Control.write(String.valueOf(value), MAX_KGSL2D0_QCOM_FREQ), MAX_KGSL2D0_QCOM_FREQ, context);
    }

    public int get2dMaxFreq() {
        return Utils.strToInt(Utils.readFile(MAX_KGSL2D0_QCOM_FREQ));
    }

    public boolean has2dMaxFreq() {
        return Utils.existFile(MAX_KGSL2D0_QCOM_FREQ);
    }

    public List<String> get2dAdjustedFreqs(Context context) {
        List<String> list = new ArrayList<>();
        for (int freq : get2dAvailableFreqs()) {
            list.add((freq / 1000000) + context.getString(R.string.mhz));
        }
        return list;
    }

    public List<Integer> get2dAvailableFreqs() {
        if (AVAILABLE_2D_FREQS == null) {
            if (Utils.existFile(AVAILABLE_KGSL2D0_QCOM_FREQS)) {
                String[] freqs = Utils.readFile(AVAILABLE_KGSL2D0_QCOM_FREQS).split(" ");
                AVAILABLE_2D_FREQS = new Integer[freqs.length];
                for (int i = 0; i < freqs.length; i++) {
                    AVAILABLE_2D_FREQS[i] = Utils.strToInt(freqs[i]);
                }
            }
        }
        if (AVAILABLE_2D_FREQS == null) return null;
        List<Integer> list = Arrays.asList(AVAILABLE_2D_FREQS);
        Collections.sort(list);
        return list;
    }

    public int get2dCurFreq() {
        return Utils.strToInt(Utils.readFile(CUR_KGSL2D0_QCOM_FREQ));
    }

    public boolean has2dCurFreq() {
        return Utils.existFile(CUR_KGSL2D0_QCOM_FREQ);
    }

    public void setGovernor(String value, Context context) {
        if (hasMaliGPU()) {
            switch (value){
                case "Default" :
                    run(Control.write("0", GOVERNOR), GOVERNOR, context);
                    break;
                case "Interactive" :
                    run(Control.write("1", GOVERNOR), GOVERNOR, context);
                    break;
                case "Static" :
                    run(Control.write("2", GOVERNOR), GOVERNOR, context);
                    break;
                case "Booster" :
                    run(Control.write("3", GOVERNOR), GOVERNOR, context);
                    break;
            }
        } else {
            run(Control.write(value, GOVERNOR), GOVERNOR, context);
        }
    }

    public List<String> getAvailableGovernors() {
        if (hasMaliGPU()) {
            String value = Utils.readFile(AVAILABLE_S7_GOVERNORS);
            if (!value.isEmpty()) {
                String[] lines = value.split("\\r?\\n");
                List<String> governors = new ArrayList<>();
                for (String line : lines) {
                    if (line.startsWith("[Current Governor]")){
                        break;
                    }
                    governors.add(line);
                }
                return governors;
            }
            return null;
        } else {
            return Arrays.asList(AVAILABLE_GOVERNORS);
        }
    }

    public String getGovernor() {
        if (hasMaliGPU()) {
            String value = Utils.readFile(AVAILABLE_S7_GOVERNORS);
            if (!value.isEmpty()) {
                String[] lines = value.split("\\r?\\n");
                String governor = "";
                for (String line : lines) {
                    if (line.startsWith("[Current Governor]")){
                        governor = line.replace("[Current Governor] ", "");;
                    }
                }
                return governor;
            }
            return null;
        } else {
            return Utils.readFile(GOVERNOR);
        }
    }

    public boolean hasGovernor() {
        if (hasMaliGPU()) {
            if (GOVERNOR == null) {
                for (String file : mScalingGovernors) {
                    if (Utils.existFile(file)) {
                        GOVERNOR = file;
                        return true;
                    }
                }
            }
        }
        return GOVERNOR != null;
    }

    public void setMinFreq(int value, Context context) {
        run(Control.write(String.valueOf(value), MIN_FREQ), MIN_FREQ, context);
    }

    public int getMinFreqOffset() {
        return MIN_FREQ_OFFSET;
    }

    public int getMinFreq() {
        return Utils.strToInt(Utils.readFile(MIN_FREQ));
    }

    public boolean hasMinFreq() {
        if (hasMaliGPU()) {
            if (MIN_FREQ == null) {
                for (String file : mMinFreqs.keySet()) {
                    if (Utils.existFile(file)) {
                        MIN_FREQ = file;
                        MIN_FREQ_OFFSET = mMinFreqs.get(file);
                        return true;
                    }
                }
            }
        }
        return MIN_FREQ != null;
    }

    public void setMaxFreq(int value, Context context) {
        run(Control.write(String.valueOf(value), MAX_FREQ), MAX_FREQ, context);
    }

    public int getMaxFreqOffset() {
        return MAX_FREQ_OFFSET;
    }

    public int getMaxFreq() {
        return Utils.strToInt(Utils.readFile(MAX_FREQ));
    }

    public boolean hasMaxFreq() {
        if (hasMaliGPU()) {
            if (MAX_FREQ == null) {
                for (String file : mMaxFreqs.keySet()) {
                    if (Utils.existFile(file)) {
                        MAX_FREQ = file;
                        MAX_FREQ_OFFSET = mMaxFreqs.get(file);
                        return true;
                    }
                }
            }
        }
        return MAX_FREQ != null;
    }

    public List<String> getAdjustedFreqs(Context context) {
        List<String> list = new ArrayList<>();
        for (int freq : getAvailableFreqs()) {
            list.add((freq / AVAILABLE_GOVERNORS_OFFSET) + context.getString(R.string.mhz));
        }
        return list;
    }

    public static List<Integer> getAvailableFreqs() {
        if (hasMaliGPU()){
                for (String file : mAvailableFreqs.keySet()) {
                    if (Utils.existFile(file)) {
                        String freqs[] = Utils.readFile(file).split("\\r?\\n");
                        AVAILABLE_FREQS = new ArrayList<>();
                        AVAILABLE_FREQS_SORT = new ArrayList<>();
                        for (String freq : freqs) {
                            String[] freqLine = freq.split(" ");
                            AVAILABLE_FREQS.add(Utils.strToInt(freqLine[0].trim()));
                            AVAILABLE_FREQS_SORT.add(Utils.strToInt(freqLine[0].trim()));
                        }
                        AVAILABLE_GOVERNORS_OFFSET = mAvailableFreqs.get(file);
                        break;
                    }
            }
            if (AVAILABLE_FREQS == null) return null;
            if (AVAILABLE_FREQS_SORT != null) {
                Collections.sort(AVAILABLE_FREQS_SORT);
            }
        }
        return AVAILABLE_FREQS;
    }

    public static List<Integer> getAvailableFreqsSort() {
        if (AVAILABLE_FREQS_SORT == null) return null;
        return AVAILABLE_FREQS_SORT;
    }

    public int getCurFreqOffset() {
        return CUR_FREQ_OFFSET;
    }

    public int getCurFreq() {
        return Utils.strToInt(Utils.readFile(CUR_FREQ));
    }

    public static boolean hasMaliGPU() {
        return Utils.existFile(AVAILABLE_S7_GOVERNORS);
    }

    public boolean hasCurFreq() {
        if (hasMaliGPU()) {
            if (CUR_FREQ == null) {
                for (String file : mCurrentFreqs.keySet()) {
                    if (Utils.existFile(file)) {
                        CUR_FREQ = file;
                        CUR_FREQ_OFFSET = mCurrentFreqs.get(file);
                        return true;
                    }
                }
            }
        }
        return CUR_FREQ != null;
    }

    public int getBusy() {
        String value = Utils.readFile(BUSY);
        float arg1 = Utils.strToFloat(value.split(" ")[0]);
        float arg2 = Utils.strToFloat(value.split(" ")[1]);
        return arg2 == 0 ? 0 : Math.round(arg1 / arg2 * 100f);
    }

    public boolean hasBusy() {
        if (hasMaliGPU()) {
            if (BUSY == null) {
                for (String file : mGpuBusys) {
                    if (Utils.existFile(file)) {
                        BUSY = file;
                        return true;
                    }
                }
            }
        }
        return BUSY != null;
    }

    public static int getHighspeedClock() {
        return Utils.strToInt(Utils.readFile(TUNABLE_HIGHSPEED_CLOCK));
    }

    public static void setHighspeedClock(String value, Context context) {
        run(Control.write(value, TUNABLE_HIGHSPEED_CLOCK), TUNABLE_HIGHSPEED_CLOCK, context);
    }

    public static boolean hasHighspeedClock() {
        return Utils.existFile(TUNABLE_HIGHSPEED_CLOCK);
    }

    public static int getHighspeedLoad() {
        return Utils.strToInt(Utils.readFile(TUNABLE_HIGHSPEED_LOAD));
    }

    public static void setHighspeedLoad(int value, Context context) {
        run(Control.write(String.valueOf(value), TUNABLE_HIGHSPEED_LOAD), TUNABLE_HIGHSPEED_LOAD, context);
    }

    public static boolean hasHighspeedLoad() {
        return Utils.existFile(TUNABLE_HIGHSPEED_LOAD);
    }

    public static int getHighspeedDelay() {
        return Utils.strToInt(Utils.readFile(TUNABLE_HIGHSPEED_DELAY));
    }

    public static void setHighspeedDelay(int value, Context context) {
        run(Control.write(String.valueOf(value), TUNABLE_HIGHSPEED_DELAY), TUNABLE_HIGHSPEED_DELAY, context);
    }

    public static boolean hasHighspeedDelay() {
        return Utils.existFile(TUNABLE_HIGHSPEED_DELAY);
    }

    public static void setVoltage(Integer freq, String voltage, Context context) {

        //freq = String.valueOf(freq);
        String volt = String.valueOf((int)(Utils.strToFloat(voltage) * VOLT_OFFSET));
        run(Control.write(freq + " " + volt, AVAILABLE_S7_FREQS), AVAILABLE_S7_FREQS + freq, context);
    }

    public static List<String> getStockVoltages() {
        String value = Utils.readFile(BACKUP);
        if (!value.isEmpty()) {
            String[] lines = value.split(SPLIT_NEW_LINE);
            List<String> voltages = new ArrayList<>();
            for (String line : lines) {
                String[] voltageLine = line.split(SPLIT_LINE);
                if (voltageLine.length > 1) {
                    voltages.add(String.valueOf(Utils.strToFloat(voltageLine[1].trim()) / VOLT_OFFSET));

                }
            }
            return voltages;
        }
        return null;
    }

    public static List<String> getVoltages() {
        String value = Utils.readFile(AVAILABLE_S7_FREQS);
        if (!value.isEmpty()) {
            String[] lines = value.split(SPLIT_NEW_LINE);
            List<String> voltages = new ArrayList<>();
            for (String line : lines) {
                String[] voltageLine = line.split(SPLIT_LINE);
                if (voltageLine.length > 1) {
                    voltages.add(String.valueOf(Utils.strToFloat(voltageLine[1].trim()) / VOLT_OFFSET));

                }
            }
            return voltages;
        }
        return null;
    }

    public static int getOffset () {
        return VOLT_OFFSET;
    }

    public static boolean hasVoltage() {
        return Utils.existFile(BACKUP);
    }

    public boolean supported() {
        if (!Utils.existFile(BACKUP)) {
            RootUtils.runCommand("cp " + AVAILABLE_S7_FREQS + " " + BACKUP);
        }
        return hasCurFreq()
                || (hasMaxFreq() && getAvailableFreqs() != null)
                || (hasMinFreq() && getAvailableFreqs() != null)
                || hasGovernor()
                || has2dCurFreq()
                || (has2dMaxFreq() && get2dAvailableFreqs() != null)
                || has2dGovernor();
    }

    private static void run(String command, String id, Context context) {
        Control.runSetting(command, ApplyOnBootFragment.GPU, id, context);
    }

}
