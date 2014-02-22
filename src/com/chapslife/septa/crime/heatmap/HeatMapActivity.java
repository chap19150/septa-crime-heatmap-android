package com.chapslife.septa.crime.heatmap;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.maps.android.heatmaps.HeatmapTileProvider;
import com.google.maps.android.heatmaps.WeightedLatLng;

public class HeatMapActivity extends Activity {
	
	public static final String TAG = HeatMapActivity.class.getSimpleName();
	
	private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private GoogleMap mMap;
        
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		setUpMapIfNeeded();
	}
	
	@Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }
	
	private void setUpMapIfNeeded() {
        if (mMap != null) {
            return;
        }
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        if (mMap != null) {
            generateHeatMap();
        }
    }
	
	private void generateHeatMap() {
		//center to Suburban Station
		getMap().moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(39.9538889, -75.1677778), 10));
		List<WeightedLatLng> list = null;
		// Get the data: latitude/longitude positions of stations.
	    try {
	        list = readItems(R.raw.regional_rail_stop_crime_count);
	    } catch (JSONException e) {
	        Toast.makeText(this, "Problem reading list of locations.", Toast.LENGTH_LONG).show();
	    }
	    
	 // Create a heat map tile provider, passing it the latlngs of the police stations.
	    mProvider = new HeatmapTileProvider.Builder()
	    	.weightedData(list)
	    	.radius(40)
	        .build();
	    // Add a tile overlay to the map, using the heat map tile provider.
	    mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
	}

	protected GoogleMap getMap() {
        setUpMapIfNeeded();
        return mMap;
    }
	
	private ArrayList<WeightedLatLng> readItems(int resource) throws JSONException {
		ArrayList<WeightedLatLng> list = new ArrayList<WeightedLatLng>();
        InputStream inputStream = getResources().openRawResource(resource);
        Scanner scanner = new Scanner(inputStream);
        String json = scanner.useDelimiter("\\A").next();
        JSONObject object = new JSONObject(json);
        JSONArray stations = object.getJSONArray("stations");
        scanner.close();
        for (int i = 0; i < stations.length(); i++) {
            JSONObject stationObject = stations.getJSONObject(i);
            double lat = stationObject.getDouble("stop_lat");
            double lng = stationObject.getDouble("stop_lon");
            double intensity = stationObject.getDouble("count");
            list.add(new WeightedLatLng(new LatLng(lat, lng),intensity));
        }
        
        return list;
	}
}
