// SPDX-License-Identifier: GPL-3.0-only

package com.cubealarm.clock.settings;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS;
import static android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS;
import static android.provider.Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS;
import static android.provider.Settings.ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT;
import static android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS;
import static android.provider.Settings.EXTRA_APP_PACKAGE;

import static androidx.core.util.TypedValueCompat.dpToPx;
import static com.cubealarm.clock.settings.PreferencesKeys.KEY_ESSENTIAL_PERMISSIONS_GRANTED;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.WindowInsetsCompat;

import com.cubealarm.clock.BaseActivity;
import com.cubealarm.clock.R;
import com.cubealarm.clock.data.SettingsDAO;
import com.cubealarm.clock.uicomponents.CollapsingToolbarBaseActivity;
import com.cubealarm.clock.utils.DeviceUtils;
import com.cubealarm.clock.utils.InsetsUtils;
import com.cubealarm.clock.utils.SdkUtils;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.color.MaterialColors;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

public class PermissionsManagementActivity extends BaseActivity
{


    DisplayMetrics mDisplayMetrics;
    View mPermissionContainerView;

    MaterialCardView mIgnoreBatteryOptimizationsView;
    MaterialCardView mNotificationView;
    MaterialCardView mFullScreenNotificationsView;
    MaterialCardView mShowLockscreenView;

    ConstraintLayout mLayout;
    ImageView mIgnoreBatteryOptimizationsDetails;
    ImageView mNotificationDetails;
    ImageView mFullScreenNotificationsDetails;
    ImageView mShowLockscreenDetails;

    TextView mIgnoreBatteryOptimizationsStatus;
    TextView mNotificationStatus;
    TextView mFullScreenNotificationsStatus;
    Button nowButton;
    LinearLayout llFullScreenNotify, llShowOnLock;

    private static final String PERMISSION_POWER_OFF_ALARM = "org.codeaurora.permission.POWER_OFF_ALARM";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.permissions_management_activity);

        grantPowerOffPermission();
//        applyWindowInsets();

        mDisplayMetrics = getResources().getDisplayMetrics();
        mPermissionContainerView = findViewById(R.id.permission_container);
        llFullScreenNotify = findViewById(R.id.llFullScreenNotify);
        llShowOnLock = findViewById(R.id.llShowOnLock);
        mLayout = findViewById(R.id.mLayout);

        nowButton = findViewById(R.id.nowButton);
        mIgnoreBatteryOptimizationsView = findViewById(R.id.IBO_view);
        mIgnoreBatteryOptimizationsView.setOnClickListener(v -> launchIgnoreBatteryOptimizationsSettings());

        mIgnoreBatteryOptimizationsDetails = findViewById(R.id.IBO_details_button);
        mIgnoreBatteryOptimizationsDetails.setOnClickListener(v ->
                displayPermissionDetailsDialog(
                        R.drawable.ic_battery_settings,
                        R.string.ignore_battery_optimizations_dialog_title,
                        R.string.ignore_battery_optimizations_dialog_text));

        mIgnoreBatteryOptimizationsStatus = findViewById(R.id.IBO_status_text);

        nowButton.setOnClickListener(v -> askAllPermission());

        mNotificationView = findViewById(R.id.notification_view);
        mNotificationView.setOnClickListener(v -> grantOrRevokeNotificationsPermission());

        mNotificationDetails = findViewById(R.id.notification_details_button);
        mNotificationDetails.setOnClickListener(v ->
                displayPermissionDetailsDialog(
                        R.drawable.ic_notifications,
                        R.string.notifications_dialog_title,
                        R.string.notifications_dialog_text));

        mNotificationStatus = findViewById(R.id.notification_status_text);

        final boolean isCardBackgroundDisplayed = SettingsDAO.isCardBackgroundDisplayed(mPrefs);
        final boolean isCardBorderDisplayed = SettingsDAO.isCardBorderDisplayed(mPrefs);

        updateCardViews(isCardBackgroundDisplayed, isCardBorderDisplayed);

        if (SdkUtils.isAtLeastAndroid14()) {
            mFullScreenNotificationsView = findViewById(R.id.FSN_view);
            llFullScreenNotify.setVisibility(View.VISIBLE);
//                mFullScreenNotificationsView.setVisibility(View.VISIBLE);
            mFullScreenNotificationsView.setOnClickListener(v -> grantOrRevokeFullScreenNotificationsPermission());

            mFullScreenNotificationsDetails = findViewById(R.id.FSN_details_button);
            mFullScreenNotificationsDetails.setOnClickListener(v ->
                    displayPermissionDetailsDialog(
                            R.drawable.ic_fullscreen,
                            R.string.FSN_dialog_title,
                            R.string.FSN_dialog_text));

            mFullScreenNotificationsStatus = findViewById(R.id.FSN_status_text);

            updateFullScreenNotificationsCard(isCardBackgroundDisplayed, isCardBorderDisplayed);
        }

        if (DeviceUtils.isMiui()) {
            mShowLockscreenView = findViewById(R.id.show_lockscreen_view);
                llShowOnLock.setVisibility(View.VISIBLE);
//                mShowLockscreenView.setVisibility(View.VISIBLE);
            mShowLockscreenView.setOnClickListener(v -> grantShowOnLockScreenPermissionXiaomi());

            mShowLockscreenDetails = findViewById(R.id.show_lockscreen_button);
            mShowLockscreenDetails.setOnClickListener(v ->
                    displayPermissionDetailsDialog(
                            R.drawable.ic_screen_lock,
                            R.string.show_lockscreen_dialog_title,
                            R.string.show_lockscreen_dialog_text));

            updateShowLockscreenCard(isCardBackgroundDisplayed, isCardBorderDisplayed);
        }








    }


    @Override
    public void onResume() {
        super.onResume();
        updateEssentialPermissionsPref();
        setStatusText();
    }

    private void applyWindowInsets() {
        InsetsUtils.doOnApplyWindowInsets(mLayout, (v, insets) -> {
            // Get the system bar and notch insets
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() |
                    WindowInsetsCompat.Type.displayCutout());

            v.setPadding(bars.left, bars.top, bars.right, 0);
            mPermissionContainerView.setPadding(0, 0, 0, bars.bottom);
        });
    }

    private void launchIgnoreBatteryOptimizationsSettings() {
        @SuppressLint("BatteryLife") final Intent intentGrant =
                new Intent().setAction(ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).setData(
                        Uri.fromParts("package", this.getPackageName(), null));

        final Intent intentRevoke =
                new Intent(ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).addFlags(FLAG_ACTIVITY_NEW_TASK);
        if (isIgnoringBatteryOptimizations(this)) {
            displayRevocationDialog(intentRevoke);
        } else {
            startActivity(intentGrant);
        }
    }

    private void grantOrRevokeNotificationsPermission() {
        Intent intent = SdkUtils.isAtLeastAndroid8()
                ? new Intent(ACTION_APP_NOTIFICATION_SETTINGS).putExtra(EXTRA_APP_PACKAGE, this.getPackageName())
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
                : new Intent(ACTION_APPLICATION_DETAILS_SETTINGS)
                .setData(Uri.fromParts("package", this.getPackageName(), null)).addFlags(FLAG_ACTIVITY_NEW_TASK);

        if (areNotificationsEnabled(this)) {
            displayRevocationDialog(intent);
        } else if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {
            new MaterialAlertDialogBuilder(this)
                    .setIcon(R.drawable.ic_notifications)
                    .setTitle(R.string.notifications_dialog_title)
                    .setMessage(R.string.notifications_dialog_text)
                    .setPositiveButton(android.R.string.ok, (dialog, which) ->
                            startActivity(intent))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        } else {
            if (SdkUtils.isAtLeastAndroid13()) {
                requestPermissions(new String[]{POST_NOTIFICATIONS}, 1002);
            } else {
                startActivity(intent);
            }
        }
    }

    private void grantOrRevokeFullScreenNotificationsPermission() {
        if (SdkUtils.isAtLeastAndroid14()) {
            final Intent intent = new Intent(ACTION_MANAGE_APP_USE_FULL_SCREEN_INTENT)
                    .setData(Uri.fromParts("package", this.getPackageName(), null)).addFlags(FLAG_ACTIVITY_NEW_TASK);

            if (!areFullScreenNotificationsEnabled(this)) {
                startActivity(intent);
            } else {
                displayRevocationDialog(intent);
            }
        }
    }

    private void grantPowerOffPermission() {
        int codeForPowerOffAlarm = 1001;
        if (this.checkSelfPermission(PERMISSION_POWER_OFF_ALARM) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{PERMISSION_POWER_OFF_ALARM}, codeForPowerOffAlarm);
        }
    }

    private void grantShowOnLockScreenPermissionXiaomi() {
        if (!DeviceUtils.isMiui()) {
            return;
        }

        Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
        intent.setClassName("com.miui.securitycenter",
                "com.miui.permcenter.permissions.PermissionsEditorActivity");
        intent.putExtra("extra_pkgname", this.getPackageName());
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Intent fallbackIntent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            fallbackIntent.setData(Uri.parse("package:" + this.getPackageName()));
            startActivity(fallbackIntent);
        }
    }

    private void displayPermissionDetailsDialog(int iconId, int titleId, int messageId) {
        new MaterialAlertDialogBuilder(this)
                .setIcon(iconId)
                .setTitle(titleId)
                .setMessage(messageId)
                .setPositiveButton(R.string.dialog_close, null)
                .show();
    }

    private void displayRevocationDialog(Intent intent) {
        new MaterialAlertDialogBuilder(this)
                .setIcon(R.drawable.ic_key_off)
                .setTitle(R.string.permission_dialog_revoke_title)
                .setMessage(R.string.revoke_permission_dialog_message)
                .setPositiveButton(android.R.string.ok, (dialog, which) ->
                        startActivity(intent))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void setStatusText() {
        mIgnoreBatteryOptimizationsStatus.setText(isIgnoringBatteryOptimizations(this)
                ? R.string.permission_granted
                : R.string.permission_denied);
        mIgnoreBatteryOptimizationsStatus.setTextColor(isIgnoringBatteryOptimizations(this)
                ? this.getColor(R.color.colorGranted)
                : this.getColor(R.color.colorAlert));

        mNotificationStatus.setText(areNotificationsEnabled(this)
                ? R.string.permission_granted
                : R.string.permission_denied);
        mNotificationStatus.setTextColor(areNotificationsEnabled(this)
                ? this.getColor(R.color.colorGranted)
                : this.getColor(R.color.colorAlert));

        if (SdkUtils.isAtLeastAndroid14()) {
            mFullScreenNotificationsStatus.setText(areFullScreenNotificationsEnabled(this)
                    ? R.string.permission_granted
                    : R.string.permission_denied);
            mFullScreenNotificationsStatus.setTextColor(areFullScreenNotificationsEnabled(this)
                    ? this.getColor(R.color.colorGranted)
                    : this.getColor(R.color.colorAlert));
        }
    }

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        final PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    public static boolean areNotificationsEnabled(Context context) {
        if (SdkUtils.isAtLeastAndroid13()) {
            return ContextCompat.checkSelfPermission(context, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED;
        } else {
            return NotificationManagerCompat.from(context).areNotificationsEnabled();
        }
    }

    public static boolean areFullScreenNotificationsEnabled(Context context) {
        if (SdkUtils.isAtLeastAndroid14()) {
            final NotificationManager notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
            return notificationManager.canUseFullScreenIntent();
        }
        return false;
    }

    public static boolean areEssentialPermissionsNotGranted(Context context) {
        return !isIgnoringBatteryOptimizations(context)
                || !areNotificationsEnabled(context)
                || SdkUtils.isAtLeastAndroid14() && !areFullScreenNotificationsEnabled(context);
    }

    private void updateEssentialPermissionsPref() {
        boolean granted = !areEssentialPermissionsNotGranted(this);
        mPrefs.edit().putBoolean(KEY_ESSENTIAL_PERMISSIONS_GRANTED, granted).apply();
    }

    private void updateCardViews(boolean isCardBackgroundDisplayed, boolean isCardBorderDisplayed) {
        if (isCardBackgroundDisplayed) {
            mIgnoreBatteryOptimizationsView.setCardBackgroundColor(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.BLACK)
            );
            mNotificationView.setCardBackgroundColor(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.BLACK)
            );
        } else {
            mIgnoreBatteryOptimizationsView.setCardBackgroundColor(Color.TRANSPARENT);
            mNotificationView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (isCardBorderDisplayed) {
            mIgnoreBatteryOptimizationsView.setStrokeWidth((int) dpToPx(2, mDisplayMetrics));
            mIgnoreBatteryOptimizationsView.setStrokeColor(MaterialColors.getColor(
                    this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK)
            );

            mNotificationView.setStrokeWidth((int) dpToPx(2, mDisplayMetrics));
            mNotificationView.setStrokeColor(MaterialColors.getColor(
                    this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK)
            );
        }
    }

    private void updateFullScreenNotificationsCard(boolean isCardBackgroundDisplayed, boolean isCardBorderDisplayed) {
        if (isCardBackgroundDisplayed) {
            mFullScreenNotificationsView.setCardBackgroundColor(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.BLACK));
        } else {
            mFullScreenNotificationsView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (isCardBorderDisplayed) {
            mFullScreenNotificationsView.setStrokeWidth((int) dpToPx(2, mDisplayMetrics));
            mFullScreenNotificationsView.setStrokeColor(MaterialColors.getColor(
                    this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK));
        }
    }

    private void updateShowLockscreenCard(boolean isCardBackgroundDisplayed, boolean isCardBorderDisplayed) {
        if (isCardBackgroundDisplayed) {
            mShowLockscreenView.setCardBackgroundColor(
                    MaterialColors.getColor(this, com.google.android.material.R.attr.colorSurface, Color.BLACK));
        } else {
            mShowLockscreenView.setCardBackgroundColor(Color.TRANSPARENT);
        }

        if (isCardBorderDisplayed) {
            mShowLockscreenView.setStrokeWidth((int) dpToPx(2, mDisplayMetrics));
            mShowLockscreenView.setStrokeColor(MaterialColors.getColor(
                    this, androidx.appcompat.R.attr.colorPrimary, Color.BLACK));
        }
    }

    public void askAllPermission(){

        if(!areNotificationsEnabled(this)){
            grantOrRevokeNotificationsPermission();
        }
        else if(!isIgnoringBatteryOptimizations(this)){
            launchIgnoreBatteryOptimizationsSettings();
        }
        else if (SdkUtils.isAtLeastAndroid14()) {
            if(!areFullScreenNotificationsEnabled(this)){
                grantOrRevokeFullScreenNotificationsPermission();
            }else {
                finish();
            }
        }
        else if (DeviceUtils.isMiui()) {
            grantShowOnLockScreenPermissionXiaomi();
        }
        else {
            finish();
        }
    }


}

