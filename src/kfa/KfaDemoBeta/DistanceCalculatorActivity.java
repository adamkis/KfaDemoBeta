package kfa.KfaDemoBeta;


import java.util.List;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapController;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.google.android.maps.Projection;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Counts the distance to a selected city based on the network cell location
 * Draws a line on the map between the two locations
 * @author adam
 *
 */
public class DistanceCalculatorActivity extends MapActivity {

	private Projection projection;
	 private List<Overlay> mapOverlays;
	 private MapController mc;
	 private MapView mapView;
	 private LineOverlay myoverlay;
	 final Context appContext = this;
	 String chosenCity ;
	 // GeoPoint of the chosen city
	 static GeoPoint 	toGp ;
	 
	final GeoPoint	sanFrancisco 	= new GeoPoint((int)(37.7750*1E6) , (int)(-122.4183*1E6));
	final GeoPoint	ny 				= new GeoPoint((int)(40.7142*1E6) , (int)(-74.0064*1E6));
	final GeoPoint	tokyo 			= new GeoPoint((int)(35.6833*1E6) , (int)(139.7667*1E6));
	final GeoPoint	dallas			= new GeoPoint((int)(32.7828*1E6) , (int)(-96.8039*1E6));
	final GeoPoint	london 			= new GeoPoint((int)(51.5171*1E6) , (int)(-0.1062*1E6));
	GeoPoint	myGeoPoint;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setContentView(R.layout.mapscreen);
		
		double myLatitude = 0;
		double myLongitude = 0;
		
		mapView = (MapView) findViewById(R.id.mapview);	//Creating an instance of MapView
		mapView.setBuiltInZoomControls(true);			//Enabling the built-in Zoom Controls
		
		// Refreshes my location based by the best provider -> GPS is not asked for at installation, will be got from cell info
		LocationManager locationManager = (LocationManager)getSystemService(LOCATION_SERVICE);
		Criteria criteria = new Criteria();
		String bestProvider = locationManager.getBestProvider(criteria, false);
		Location myLocation = locationManager.getLastKnownLocation(bestProvider);
		try {
			myLatitude = myLocation.getLatitude();
			myLongitude = myLocation.getLongitude();
			Log.i("qw" ,
			 "Lat:" 	+ Double.toString(myLatitude) +
			 "\nLongit" + Double.toString(myLongitude)
			 );
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		myGeoPoint = new GeoPoint((int)(myLatitude*1E6) , (int)(myLongitude*1E6));
		
		if(savedInstanceState == null) { showDialog(); } 
		else{ 
			 mc = mapView.getController();
			 mc.setCenter(myGeoPoint);
			 mc.setZoom(4);
			 //Initializing the MapController and setting the map to center at the
			 //defined GeoPoint
			 mapOverlays = mapView.getOverlays();
			 mapOverlays.clear();
			 projection = mapView.getProjection();
			 myoverlay = new LineOverlay(myGeoPoint , toGp);
			 mapOverlays.add(myoverlay);	 
		 }
		 
		
		 Button chooseCityButton = (Button)findViewById(R.id.chooseCity);
		 chooseCityButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				showDialog();
			}
		});
		 


	}
	

	
	/**
	 * 
	 * Calculates distance of GeoPoints in KMs 
	 * 
	 * @param from - GeoPoint
	 * @param to - GeoPoint
	 * @return - The distance in Kilometers
	 */
	private float distanceTo(GeoPoint from, GeoPoint to){
		
		Location fromLoc = new Location("network");
		Location toLoc = new Location("network");
		
		double fromLatitude = from.getLatitudeE6() / 1E6;
		double fromLongitude = from.getLongitudeE6() / 1E6;
		fromLoc.setLatitude(fromLatitude);
		fromLoc.setLongitude(fromLongitude);
		
		double toLatitude = to.getLatitudeE6() / 1E6;
		double toLongitude = to.getLongitudeE6() / 1E6;
		toLoc.setLatitude(toLatitude);
		toLoc.setLongitude(toLongitude);
		
		return fromLoc.distanceTo(toLoc) / 1000;
	
	}

	
	/**
	 * Draws line from current location to chosen city
	 *
	 */
	class LineOverlay extends Overlay{

		private GeoPoint myGeoPoint;
		private GeoPoint diffCity;
		
		public LineOverlay(GeoPoint myGeoPoint, GeoPoint diffCity){
			this.myGeoPoint = myGeoPoint;
			this.diffCity = diffCity;
		}

		public void draw(Canvas canvas, MapView mapv, boolean shadow){
			 super.draw(canvas, mapv, shadow);
			 //Configuring the paint brush
			 Paint mPaint = new Paint();
			 mPaint.setDither(true);
			 mPaint.setColor(Color.BLUE);
			 mPaint.setStyle(Paint.Style.FILL_AND_STROKE);
			 mPaint.setStrokeJoin(Paint.Join.ROUND);
			 mPaint.setStrokeCap(Paint.Cap.ROUND);
			 mPaint.setStrokeWidth(4);
	
			 GeoPoint gP1 = myGeoPoint;
			 GeoPoint gP2 = diffCity;
	
			 Point p1 = new Point();
			 Point p2 = new Point();
			 Path path1 = new Path();
	
			 
			 projection.toPixels(gP1, p1);
			 projection.toPixels(gP2, p2);
	
			 path1.moveTo(p1.x, p1.y);
			 path1.lineTo(p2.x,p2.y);
	
			 canvas.drawPath(path1, mPaint);

		 }

	}
	
	/**
	 * City chooser dialog
	 */
    private void showDialog(){
    	
    	final CharSequence[] cities = {"Tokyo", "London", "Dallas", "San Francisco", "New York"};
    	
	    final TextView infoMessage = new TextView(appContext);
	    infoMessage.setTextSize(20);
	    infoMessage.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
	    infoMessage.setTextColor(Color.BLACK);
	    // Linkify text
		final SpannableString s =  new SpannableString("");
		Linkify.addLinks(s, Linkify.WEB_URLS);
		infoMessage.setText(s);
		infoMessage.setMovementMethod(LinkMovementMethod.getInstance());
    	
    	
    	AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
    			appContext);
 
			// Choosing cities
			alertDialogBuilder.setTitle("Choose a city to count the distance to:");
			alertDialogBuilder
				.setView(infoMessage)
				.setCancelable(true)
				.setItems(cities, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						chosenCity = String.valueOf(cities[item]);
						toGp = new GeoPoint(0, 0);
						
						if(chosenCity.equals("Tokyo")) 				toGp = tokyo;
						else if(chosenCity.equals("London"))		toGp = london;
						else if(chosenCity.equals("Dallas"))		toGp = dallas;
						else if(chosenCity.equals("San Francisco"))	toGp = sanFrancisco;
						else if(chosenCity.equals("New York"))		toGp = ny;
						
						// If you'r closer to the city than 15 Kilometers
						if(toGp != null){
							if( distanceTo(myGeoPoint, toGp) > 15 ){
								Toast.makeText(getApplicationContext(),
								"Your distance from " + chosenCity + " is: " 
										 + Integer.toString( Math.round(distanceTo(myGeoPoint, toGp)) ) +
										 " kms\n" +
										 "(" + Integer.toString( (int)(Math.round(distanceTo(myGeoPoint, toGp)*0.621371))) + " miles)",
								 Toast.LENGTH_LONG).show();
							}
							else{
								Toast.makeText(getApplicationContext(),
											"You're at " + chosenCity,
											Toast.LENGTH_LONG).show();
							}
						}
						// Show map again
						 mc = mapView.getController();
						 mc.setCenter(myGeoPoint);
						 mc.setZoom(4);
						 //Initializing the MapController and setting the map to center at the
						 //defined GeoPoint
						 mapOverlays = mapView.getOverlays();
						 mapOverlays.clear();
						 projection = mapView.getProjection();
						 myoverlay = new LineOverlay(myGeoPoint , toGp);
						 mapOverlays.add(myoverlay);
						
					}
				});

			AlertDialog alertDialog = alertDialogBuilder.create();
			alertDialog.show();  	
    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}	   
    
}


