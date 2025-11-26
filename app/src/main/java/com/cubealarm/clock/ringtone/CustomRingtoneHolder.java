/*
 * Copyright (C) 2016 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.cubealarm.clock.ringtone;

import com.cubealarm.clock.data.CustomRingtone;

class CustomRingtoneHolder extends RingtoneHolder {

    CustomRingtoneHolder(CustomRingtone ringtone) {
        super(ringtone.getUri(), ringtone.getTitle());
    }

    @Override
    public int getItemViewType() {
        return RingtoneViewHolder.VIEW_TYPE_CUSTOM_SOUND;
    }
}
