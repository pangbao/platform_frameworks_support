/*
 * Copyright 2018 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package androidx.slice;

import static androidx.slice.SliceViewManagerTest.TestSliceProvider.sSliceProviderReceiver;
import static androidx.slice.core.SliceHints.INFINITY;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.filters.SdkSuppress;
import android.support.test.filters.SmallTest;
import android.support.test.runner.AndroidJUnit4;

import androidx.slice.builders.GridRowBuilder;
import androidx.slice.builders.ListBuilder;
import androidx.slice.render.SliceRenderActivity;
import androidx.slice.widget.SliceLiveData;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

// TODO: move to slice-builders module?
/**
 * Tests for content validation of the different slice builders.
 */
@RunWith(AndroidJUnit4.class)
@SmallTest
@SdkSuppress(minSdkVersion = 19)
public class SliceBuilderTest {

    private final Context mContext = InstrumentationRegistry.getContext();
    private final Uri mUri = Uri.parse("content://androidx.slice.view.test/slice");

    @Before
    public void setup() {
        SliceProvider.setSpecs(SliceLiveData.SUPPORTED_SPECS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowForInvalidRangeMax() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        Slice slice = lb.addInputRange(new ListBuilder.InputRangeBuilder()
                .setInputAction(getIntent(""))
                .setTitle("Input range")
                .setMax(20)
                .setMin(50))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowForInvalidRangeMin() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        Slice slice = lb.addInputRange(new ListBuilder.InputRangeBuilder()
                .setTitle("Input range")
                .setInputAction(getIntent(""))
                .setMax(20)
                .setMin(30))
                .build();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowForInvalidRangeValue() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        Slice slice = lb.addInputRange(new ListBuilder.InputRangeBuilder()
                .setInputAction(getIntent(""))
                .setTitle("Input range")
                .setMax(80)
                .setValue(100)
                .setMin(30))
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowForGridRowFirst() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.addGridRow(new GridRowBuilder().addCell(
                new GridRowBuilder.CellBuilder().addTitleText("Title").addText("Text")));
        lb.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowForTextlessHeader() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.setHeader(new ListBuilder.HeaderBuilder());
        lb.build();
    }

    public void testNoThrowForFirstHeader() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.setHeader(new ListBuilder.HeaderBuilder()
                .setTitle("Title"));
        lb.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowForNoTextFirstInputRange() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.addInputRange(new ListBuilder.InputRangeBuilder()
                .setInputAction(getIntent(""))
                .setMax(80)
                .setValue(70)
                .setMin(30))
                .build();
    }

    public void testNoThrowForFirstInputRange() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.addInputRange(new ListBuilder.InputRangeBuilder()
                .setInputAction(getIntent(""))
                .setTitle("Title")
                .setMax(80)
                .setValue(70)
                .setMin(30))
                .build();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowForNoTextFirstRow() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.addRow(new ListBuilder.RowBuilder().addEndItem(System.currentTimeMillis()));
        lb.build();
    }

    @Test(expected = IllegalStateException.class)
    public void testThrowForNotBinding() {
        SliceProvider.setSpecs(null);
        new ListBuilder(mContext, mUri, INFINITY);
    }

    @Test
    public void testGetPinnedSpecs() throws InterruptedException {
        sSliceProviderReceiver = mock(SliceProvider.class);
        SliceViewManager.getInstance(mContext).pinSlice(mUri);
        try {
            SliceProvider.setSpecs(null);
            verify(sSliceProviderReceiver, timeout(2000)).onSlicePinned(eq(mUri));
            new ListBuilder(mContext, mUri, INFINITY);
        } finally {
            SliceViewManager.getInstance(mContext).unpinSlice(mUri);
            verify(sSliceProviderReceiver, timeout(2000)).onSliceUnpinned(eq(mUri));
        }
    }

    public void testNoThrowForFirstRow() {
        ListBuilder lb = new ListBuilder(mContext, mUri, INFINITY);
        lb.addRow(new ListBuilder.RowBuilder().setTitle("Title"));
        lb.build();
    }

    private PendingIntent getIntent(String action) {
        Intent intent = new Intent(action);
        intent.setClassName(mContext.getPackageName(), SliceRenderActivity.class.getName());
        return PendingIntent.getActivity(mContext, 0, intent, 0);
    }
}