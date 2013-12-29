package com.example.pastweatherhistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.widget.ExpandableListView;

public class DisplayWeatherData extends Activity {

	ExpandableListAdapter listAdapter;
	ExpandableListView expListView;
	ArrayList<String> listData;
	List<String> listDataHeader;
	HashMap<String, List<String>> listDataChild;
	String[] splits;
	int mCityRecordCount[];

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_weather_data);

		expListView = (ExpandableListView) findViewById(R.id.lvExp);

		Bundle b = getIntent().getExtras();
		if (b != null) {
			listData = b.getStringArrayList("listData");
			splits = b.getStringArray("splits");
			mCityRecordCount = b.getIntArray("mCityRecordCount");
		}

		prepareListData();

		listAdapter = new ExpandableListAdapter(this, listDataHeader,
				listDataChild);

		// setting list adapter
		expListView.setAdapter(listAdapter);

	}

	/*
	 * Preparing the list data
	 */

	private void prepareListData() {

		// creating a separate list for each city
		int count = 0;
		List<List<String>> lists = new ArrayList<List<String>>();
		for (int i = 0; i < splits.length; i++) {
			List<String> mEachCitylist = new ArrayList<String>();
			for (int ii = count; ii < mCityRecordCount[i] + count; ii++)
					mEachCitylist.add(listData.get(ii));
			lists.add(mEachCitylist);
			count = count + mCityRecordCount[i];
  
		}

		listDataHeader = new ArrayList<String>();
		listDataChild = new HashMap<String, List<String>>();

		// Adding child data
		for (int i = 0; i < splits.length; i++)
			listDataHeader.add(splits[i]);

		for (int j = 0; j < listDataHeader.size(); j++) {
			for (int k = 0; k < mCityRecordCount[j]; k++) 
						listDataChild.put(listDataHeader.get(j), lists.get(j));
		}

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.display_weather_data, menu);
		return true;
	}

}
