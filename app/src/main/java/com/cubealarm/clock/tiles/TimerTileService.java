// SPDX-License-Identifier: GPL-3.0-only

package com.cubealarm.clock.tiles;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

import static com.cubealarm.clock.uidata.UiDataModel.Tab.TIMERS;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;

import androidx.annotation.RequiresApi;

import com.cubealarm.clock.DeskClock;
import com.cubealarm.clock.R;
import com.cubealarm.clock.data.DataModel;
import com.cubealarm.clock.data.Timer;
import com.cubealarm.clock.uidata.UiDataModel;
import com.cubealarm.clock.utils.SdkUtils;

import java.util.List;

@RequiresApi(api = Build.VERSION_CODES.N)
public class TimerTileService extends TileService {

    @Override
    public void onTileAdded() {
        super.onTileAdded();
        updateTile(getQsTile());
    }

    @SuppressLint("StartActivityAndCollapseDeprecated")
    @Override
    public void onClick() {
        super.onClick();

        final Intent intent = new Intent(this, DeskClock.class)
                .addFlags(FLAG_ACTIVITY_NEW_TASK)
                .addFlags(FLAG_ACTIVITY_CLEAR_TOP);

        UiDataModel.getUiDataModel().setSelectedTab(TIMERS);

        if (SdkUtils.isAtLeastAndroid14()) {
            startActivityAndCollapse(PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE));
        } else {
            startActivityAndCollapse(intent);
        }
    }

    @Override
    public void onStartListening() {
        super.onStartListening();

        updateTile(getQsTile());
    }

    public void onStopListening() {
        super.onStopListening();

        updateTile(getQsTile());
    }

    private void updateTile(Tile tile) {
        if (tile == null) {
            return;
        }

        List<Timer> timerList = DataModel.getDataModel().getTimers();
        final int count = timerList.size();
        boolean isTimerRunningOrPaused = false;

        for (Timer timer : timerList) {
            if (timer.isRunning() || timer.isPaused()) {
                isTimerRunningOrPaused = true;
                break;
            }
        }

        if (timerList.isEmpty() || !isTimerRunningOrPaused) {
            tile.setState(Tile.STATE_INACTIVE);
        } else {
            tile.setState(Tile.STATE_ACTIVE);
        }

        if (SdkUtils.isAtLeastAndroid10()) {
            tile.setSubtitle(getString(R.string.timers_in_use, count));
        }

        tile.updateTile();
    }
}
