
package com.example.slidecutlistview;

import android.content.Context;

/**
 * ���湤����
 */
public class CustomSwipeUtils {

    /**
     * ��dpת����px
     */
    public static int convertDptoPx(Context context, final float dpValue) {
        final float density = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }

    /**
     * ��ȡ��Ļ�Ŀ��
     */
    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }
}
