package com.sammy.etweaks.utils.kernel.misc;

import android.content.Context;

import com.grarak.kerneladiutor.fragments.ApplyOnBootFragment;
import com.grarak.kerneladiutor.utils.Utils;
import com.grarak.kerneladiutor.utils.root.Control;

/**
 * Created by Morogoku on 25/04/2017.
 */

public class Pwm {

    private static final String PWM_ENABLE = "/sys/class/lcd/panel/smart_on";
    private static final String PWM_ENABLE_KLTE = "/sys/class/graphics/fb0/smart_dim";

    public static void enablePwm(boolean enable, Context context) {
        if (Utils.existFile(PWM_ENABLE) == true)
            run(Control.write(enable ? "1" : "0", PWM_ENABLE), PWM_ENABLE, context);
        else
            run(Control.write(enable ? "1" : "0", PWM_ENABLE_KLTE), PWM_ENABLE_KLTE, context);
    }

    public static boolean isPwmEnabled() {
        return Utils.readFile(PWM_ENABLE).equals("1") || Utils.readFile(PWM_ENABLE_KLTE).equals("1");
    }

    public static boolean supported() {
        return Utils.existFile(PWM_ENABLE) ||
        Utils.existFile(PWM_ENABLE_KLTE);
    }

    private static void run(String command, String id, Context context) {
        Control.runSetting(command, ApplyOnBootFragment.MISC, id, context);
    }
}
