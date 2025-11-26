/*
 * Copyright (C) 2016 The Android Open Source Project
 * modified
 * SPDX-License-Identifier: Apache-2.0 AND GPL-3.0-only
 */

package com.cubealarm.clock.ringtone;

import static androidx.recyclerview.widget.RecyclerView.NO_ID;

import android.net.Uri;

import androidx.annotation.StringRes;

import com.cubealarm.clock.ItemAdapter;

final class HeaderHolder extends ItemAdapter.ItemHolder<Uri> {

    private final @StringRes
    int mTextResId;

    HeaderHolder(@StringRes int textResId) {
        super(null, NO_ID);
        mTextResId = textResId;
    }

    @StringRes
    int getTextResId() {
        return mTextResId;
    }

    @Override
    public int getItemViewType() {
        return HeaderViewHolder.VIEW_TYPE_ITEM_HEADER;
    }
}
