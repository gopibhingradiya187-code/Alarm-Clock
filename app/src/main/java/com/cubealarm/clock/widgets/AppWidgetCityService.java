// SPDX-License-Identifier: GPL-3.0-only

package com.cubealarm.clock.widgets;

import android.content.Intent;
import android.widget.RemoteViewsService;

import com.cubealarm.clock.widgets.materialyouwidgets.MaterialYouDigitalAppWidgetCityViewsFactory;
import com.cubealarm.clock.widgets.standardwidgets.DigitalAppWidgetCityViewsFactory;

public class AppWidgetCityService {

    public static class DigitalAppWidgetCityService extends RemoteViewsService {

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent i) {
            return new DigitalAppWidgetCityViewsFactory(getApplicationContext(), i);
        }
    }

    public static class MaterialYouDigitalAppWidgetCityService extends RemoteViewsService {

        @Override
        public RemoteViewsFactory onGetViewFactory(Intent i) {
            return new MaterialYouDigitalAppWidgetCityViewsFactory(getApplicationContext(), i);
        }
    }
}
