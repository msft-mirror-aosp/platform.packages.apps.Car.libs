/*
 * Copyright 2019 The Android Open Source Project
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

package com.android.car.ui.paintbooth.caruirecyclerview;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.android.car.ui.paintbooth.R;
import com.android.car.ui.recyclerview.CarUiListItem;
import com.android.car.ui.recyclerview.CarUiListItemAdapter;
import com.android.car.ui.recyclerview.CarUiRecyclerView;

import java.util.ArrayList;

/** Activity that shows {@link CarUiRecyclerView} with dummy {@link CarUiListItem} entries. */
public class CarUiListItemActivity extends Activity {

    private final ArrayList<CarUiListItem> mData = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.car_ui_recycler_view_activity);
        CarUiRecyclerView recyclerView = findViewById(R.id.list);

        CarUiListItemAdapter adapter = new CarUiListItemAdapter(generateDummyData());
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<CarUiListItem> generateDummyData() {
        Context context = this;

        CarUiListItem item = new CarUiListItem();
        item.setTitle("Test title");
        item.setBody("Test body");
        mData.add(item);

        item = new CarUiListItem();
        item.setTitle("Test title with no body");
        mData.add(item);

        item = new CarUiListItem();
        item.setBody("Test body with no title");
        mData.add(item);

        item = new CarUiListItem();
        item.setTitle("Test Title");
        item.setIcon(getDrawable(R.drawable.ic_launcher));
        mData.add(item);

        item = new CarUiListItem();
        item.setTitle("Test Title");
        item.setBody("Test body text");
        item.setIcon(getDrawable(R.drawable.ic_launcher));
        mData.add(item);

        item = new CarUiListItem();
        item.setIcon(getDrawable(R.drawable.ic_launcher));
        item.setTitle("Title -- Item with checkbox");
        item.setBody("Will present toast on change of selection state.");
        item.setOnCheckedChangedListener(
                (isChecked) -> Toast.makeText(context,
                        "Item checked state is: " + isChecked, Toast.LENGTH_SHORT).show());
        item.setAction(CarUiListItem.Action.CHECK_BOX);
        mData.add(item);

        item = new CarUiListItem();
        item.setIcon(getDrawable(R.drawable.ic_launcher));
        item.setBody("Body -- Item with switch");
        item.setAction(CarUiListItem.Action.SWITCH);
        mData.add(item);

        item = new CarUiListItem();
        item.setIcon(getDrawable(R.drawable.ic_launcher));
        item.setTitle("Title -- Item with checkbox");
        item.setBody("Item is initially checked");
        item.setAction(CarUiListItem.Action.CHECK_BOX);
        item.setChecked(true);
        mData.add(item);

        return mData;
    }
}
