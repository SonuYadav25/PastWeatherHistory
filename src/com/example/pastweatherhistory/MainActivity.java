package com.example.pastweatherhistory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * @author Sonu
 * 
 */
public class MainActivity extends Activity {

	EditText editText;
	TextView hintTextView;
	Button submit_button, Reset_button;
	ProgressBar progressBar;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.pastweatherdata_main);

		editText = (EditText) findViewById(R.id.editText);
		submit_button = (Button) findViewById(R.id.submit_button);
		Reset_button = (Button) findViewById(R.id.reset_button);
		progressBar = (ProgressBar) findViewById(R.id.progressBar);
		editText.setHint("Please use & as separator(no spaces) ");
		hintTextView = (TextView) findViewById(R.id.hintTextView);
		hintTextView
				.setText("Note:-For multiword city use + in between and not spaces.");

		Reset_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				editText.setText(null);
				progressBar.setVisibility(View.GONE);
			}
		});

		submit_button.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {

				String editTextstr = editText.getText().toString();
				if (editTextstr.equals("")) {
					editText.setError(Html
							.fromHtml("Cities cannot be empty</font>"));

				} else {
					AsyncTaskRunner runner = new AsyncTaskRunner(
							getApplicationContext(), progressBar);
					String mInput = editText.getText().toString();

					if (isConnectedToInternet())
						runner.execute(mInput);
					else {
						Toast.makeText(getApplicationContext(),
								"Sorry,no internet connectivty",
								Toast.LENGTH_LONG).show();
					}
				}
			}
		});

	}

	public boolean isConnectedToInternet() {
		ConnectivityManager connectivity = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connectivity != null) {
			NetworkInfo[] info = connectivity.getAllNetworkInfo();
			if (info != null)
				for (int i = 0; i < info.length; i++)
					if (info[i].getState() == NetworkInfo.State.CONNECTED) {
						return true;
					}

		}
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}

/**
 * class which runs the long operation. ( fetching data through url )
 */
class AsyncTaskRunner extends AsyncTask<String, String, JSONObject> {

	Context mcontext;
	// list JSONArray
	JSONArray list = null;
	JSONObject json;
	HashMap<String, String> map;
	ArrayList<String> listData;
	ProgressBar progressBar;
	String splits[], description, mcheckMessage;
	int mCityRecordCount[], k, mcheckCount = 1;
	Boolean flag = false;

	public AsyncTaskRunner(Context context, ProgressBar progressBar) {
		mcontext = context;
		map = new HashMap<String, String>();
		listData = new ArrayList<String>();
		this.progressBar = progressBar;
	}

	@Override
	protected JSONObject doInBackground(String... params) {
		// Do your long operations here and return the result
		try {

			// Creating JSON Parser instance
			JSONParser jParser = new JSONParser();

			splits = params[0].split("&");

			mCityRecordCount = new int[splits.length];

			for (k = 0; k < splits.length; k++) {
				// url to make request
				String url = "http://api.openweathermap.org/data/2.5/history/city?q="
						+ splits[k]
						+ "&type=day&cnt=30&APPID=0688dbf4a4947346ca330aed8e3dbc6b";

				// getting JSON string from URL
				json = jParser.getJSONFromUrl(url);

				// check message field in json object for data
				mcheckMessage = json.getString("message");
				if (json.has("cnt"))
					mcheckCount = json.getInt("cnt");

				if (mcheckMessage.equals("no data") || 0 == mcheckCount)
					flag = true;

				if (json != null && !flag) // invoke on data availability
					getWeatherData();
			}
		} catch (Exception e) {

			e.printStackTrace();

		}
		return json;
	}

	private void getWeatherData() {

		try {
			// Getting Array of list
			list = json.getJSONArray("list");

			// get number of each city records returned in the list
			mCityRecordCount[k] = list.length();

			// looping through All list
			for (int i = 0; i < list.length(); i++) {
				JSONObject l = list.getJSONObject(i);

				// Storing each json item in variable
				// weather
				JSONArray weatherarray = l.getJSONArray("weather");

				for (int j = 0; j < weatherarray.length(); j++) {
					description = weatherarray.getJSONObject(j).getString(
							"description");
				}

				// wind is assign in JSON Object
				JSONObject windobj = l.getJSONObject("wind");
				String speed = windobj.getString("speed");

				// main
				JSONObject mainobj = l.getJSONObject("main");
				String pressure = mainobj.getString("pressure");
				String humidity = mainobj.getString("humidity");
				Double temp = mainobj.getDouble("temp");
				temp -= 275.15;
				String temprature = String.valueOf(Math.round(temp));

				map.put("weatherinfo", "Temperature:" + temprature + "C  "
						+ "Humidity:" + humidity + "%  "
						+ "Atmosheric Condition:" + description + " Pressure:"
						+ pressure + "hPa  " + "Wind(speed):" + speed + "mpa ");

				// adding Hashmap to ArrayList
				for (Map.Entry<String, String> amap : map.entrySet()) {
					listData.add(amap.getValue());

				}
			}

		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	@Override
	protected void onPostExecute(JSONObject result) {
		// execution of result of Long time consuming operation

		progressBar.setVisibility(View.GONE);
		if (json != null && !flag) {

			Intent intent = new Intent(mcontext, DisplayWeatherData.class);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			intent.putStringArrayListExtra("listData", listData);
			intent.putExtra("splits", splits);
			intent.putExtra("mCityRecordCount", mCityRecordCount);
			mcontext.startActivity(intent);

		}

		else if (flag) {
			Toast.makeText(mcontext, "City name is not correct",
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(mcontext, "No Data Retrieved", Toast.LENGTH_LONG)
					.show();
		}

	}

	@Override
	protected void onPreExecute() {
		progressBar.setVisibility(View.VISIBLE);
	}

	@Override
	protected void onProgressUpdate(String... text) {
	}
}
