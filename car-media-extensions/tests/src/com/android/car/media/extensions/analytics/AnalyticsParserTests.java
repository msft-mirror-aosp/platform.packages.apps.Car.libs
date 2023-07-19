/*
 * Copyright (C) 2023 The Android Open Source Project
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
package  com.android.car.media.extensions.analytics;

import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_BROWSE_NODE_CHANGE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_EVENT_NAME;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_ITEM_IDS;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_MEDIA_ID;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_TIMESTAMP;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VERSION;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_MEDIA_CLICKED;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_VIEW_CHANGE;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_EVENT_VISIBLE_ITEMS;
import static com.android.car.media.extensions.analytics.Constants.ANALYTICS_ROOT_KEY_PASSKEY;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import android.content.ComponentName;
import android.os.Bundle;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.android.car.media.extensions.analytics.client.AnalyticsCallback;
import com.android.car.media.extensions.analytics.client.AnalyticsParser;
import com.android.car.media.extensions.analytics.event.AnalyticsEvent;
import com.android.car.media.extensions.analytics.event.BrowseChangeEvent;
import com.android.car.media.extensions.analytics.event.MediaClickedEvent;
import com.android.car.media.extensions.analytics.event.ViewChangeEvent;
import com.android.car.media.extensions.analytics.event.VisibleItemsEvent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@RunWith(AndroidJUnit4.class)
public class AnalyticsParserTests {
    @Rule public final MockitoRule mockito = MockitoJUnit.rule();

    @Mock
    AnalyticsCallback mAnalyticsCallback;

    @Captor ArgumentCaptor<AnalyticsEvent> mEventArgCaptor;

    @Captor ArgumentCaptor<Integer> mErrorArgCaptor;

    private final UUID mPassKey = UUID.randomUUID();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void parserCallbackTimeComponentTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_MEDIA_CLICKED);
        String mediaID = "test_media_id_1";
        bundle.putString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID, mediaID);
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback).onMediaClickedEvent(
                (MediaClickedEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertNotNull(mEventArgCaptor.getValue());
        AnalyticsEvent event = mEventArgCaptor.getValue();
        ComponentName componentName =
                new ComponentName(
                        AnalyticsParserTests.class.getPackage().toString(),
                        AnalyticsParserTests.class.getName());
        assertEquals(componentName.flattenToString(), event.getComponent());
        assertTrue(event.getTime() > 0);
        assertTrue(event.getAnalyticsVersion() > 0.0);
    }

    @Test
    public void parserCallbackMediaClickedTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_MEDIA_CLICKED);
        String mediaID = "test_media_id_1";
        bundle.putString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID, mediaID);
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback)
                .onMediaClickedEvent((MediaClickedEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertTrue(mEventArgCaptor.getValue() instanceof MediaClickedEvent);
        MediaClickedEvent event = (MediaClickedEvent) mEventArgCaptor.getValue();
        String eventMediaId = event.getMediaId();
        assertEquals(eventMediaId, mediaID);
    }

    @Test
    public void parserCallbackVisibleItemsTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_VISIBLE_ITEMS);
        List<String> visibleItems = Arrays.asList("test_media_id_1", "test_media_id_2",
                "test_media_id_3");
        bundle.putStringArrayList(ANALYTICS_EVENT_DATA_KEY_ITEM_IDS,
                new ArrayList<>(visibleItems));
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback)
                .onVisibleItemsEvent((VisibleItemsEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertTrue(mEventArgCaptor.getValue() instanceof VisibleItemsEvent);
        VisibleItemsEvent event = (VisibleItemsEvent) mEventArgCaptor.getValue();
        List<String> eventVisibleItems = event.getItemsId();
        assertArrayEquals(eventVisibleItems.toArray(), visibleItems.toArray());
    }

    @Test
    public void parserCallbackBrowseNodeChangeTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_BROWSE_NODE_CHANGE);
        String node = "test_media_id_1";
        int action = 1;
        bundle.putString(ANALYTICS_EVENT_DATA_KEY_MEDIA_ID, node);
        bundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, action);
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback)
                .onBrowseNodeChangeEvent((BrowseChangeEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertTrue(mEventArgCaptor.getValue() instanceof BrowseChangeEvent);
        BrowseChangeEvent event = (BrowseChangeEvent) mEventArgCaptor.getValue();
        String eventBrowseNew = event.getBrowseNodeId();
        int eventAction = event.getViewAction();
        assertEquals(eventAction, action);
        assertEquals(eventBrowseNew, node);
    }

    @Test
    public void parserCallbackViewEntryTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_VIEW_CHANGE);
        int viewComponent = AnalyticsEvent.BROWSE_LIST;
        bundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT, viewComponent);
        bundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, ViewChangeEvent.SHOW);
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback)
                .onViewChangeEvent((ViewChangeEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertTrue(mEventArgCaptor.getValue() instanceof ViewChangeEvent);
        ViewChangeEvent event = (ViewChangeEvent) mEventArgCaptor.getValue();
        int eventViewType = event.getViewComponent();
        assertEquals(eventViewType, viewComponent);
        int type = event.getViewAction();
        assertEquals(type, ViewChangeEvent.SHOW);
    }

    @Test
    public void parserCallbackViewExitTest() {
        Bundle bundle = createTestBundle(ANALYTICS_EVENT_VIEW_CHANGE);
        int viewComponent = AnalyticsEvent.BROWSE_LIST;
        bundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_COMPONENT, viewComponent);
        bundle.putInt(ANALYTICS_EVENT_DATA_KEY_VIEW_ACTION, ViewChangeEvent.HIDE);
        AnalyticsParser.parseAnalyticsBundle(bundle, mAnalyticsCallback);
        verify(mAnalyticsCallback)
                .onViewChangeEvent((ViewChangeEvent) mEventArgCaptor.capture());
        verifyNoMoreInteractions(mAnalyticsCallback); // Verify onError not called
        assertTrue(mEventArgCaptor.getValue() instanceof ViewChangeEvent);
        ViewChangeEvent event = (ViewChangeEvent) mEventArgCaptor.getValue();
        int eventViewType = event.getViewComponent();
        assertEquals(eventViewType, viewComponent);
        int type = event.getViewAction();
        assertEquals(type, ViewChangeEvent.HIDE);
    }

    private Bundle createTestBundle(String eventName) {
        Bundle bundle = new Bundle();
        bundle.putString(ANALYTICS_ROOT_KEY_PASSKEY, mPassKey.toString());
        bundle.putString(ANALYTICS_EVENT_DATA_KEY_EVENT_NAME, eventName);
        bundle.putLong(ANALYTICS_EVENT_DATA_KEY_TIMESTAMP, System.currentTimeMillis());
        bundle.putFloat(ANALYTICS_EVENT_DATA_KEY_VERSION, 1.0f);
        ComponentName componentName =
                new ComponentName(
                        AnalyticsParserTests.class.getPackage().toString(),
                        AnalyticsParserTests.class.getName());
        bundle.putString(
                ANALYTICS_EVENT_DATA_KEY_HOST_COMPONENT_ID, componentName.flattenToString());
        return bundle;
    }
}
