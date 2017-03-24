package com.xuchongyang.linphonedemo.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

/**
 * Created by Mark Xu on 17/3/11.
 * FragmentHelper
 */

public class FragmentHelper {
    public static void replaceFragment(FragmentActivity fragmentActivity, int containerViewId, Fragment fragment) {
        FragmentManager fragmentManager = fragmentActivity.getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(containerViewId, fragment);
        transaction.commit();
    }
}
