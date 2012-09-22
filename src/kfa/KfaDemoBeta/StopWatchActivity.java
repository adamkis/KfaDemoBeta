package kfa.KfaDemoBeta;

import kfa.KfaDemoBeta.StopWatchService.StopWatchBinder;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.os.SystemClock;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.Toast;
import android.widget.TextView;

public class StopWatchActivity extends Activity {
    /** Called when the activity is first created. */
    
	static Chronometer stopWatchChronometer;
	TextView header ;
	static StopWatchService sws;
	
	public static boolean isStarted = false;
	private static boolean isRunning = false;
	private static boolean isReseted = false;
	private static boolean firstStart = true;
	private static boolean isStopped = true;
	
    boolean mBound = false;
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
        setContentView(R.layout.stopwatchscreen);
        Button startButton = (Button)findViewById(R.id.startButton);
        Button stopButton = (Button)findViewById(R.id.stopButton);
        Button resetButton = (Button)findViewById(R.id.resetButton);
        header = (TextView)findViewById(R.id.header);
        
        stopWatchChronometer = (Chronometer)findViewById(R.id.chronometer1);
        
        // Starts and binds the background service
        Intent intent = new Intent(this, StopWatchService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        
        // If returned from somewhere, or screen rotated, the chronometer sets the elapsed time
        // If returned from somewhere while stopwatch was running, starts it
        if( isStarted && (sws != null) ){
        	try{
        		stopWatchChronometer.setBase(SystemClock.elapsedRealtime() - sws.getElapsedTime());
	        	if(isRunning){
	        		stopWatchChronometer.start();
	        	}
        	}catch(Exception e){
        		Toast.makeText(getApplicationContext(), "Ohh snap... Couldn't start stopwatch", Toast.LENGTH_LONG).show();
        	}
        }
        else{ 
        	stopWatchChronometer.setText("Press start!");
        }
        
        // START button
	    startButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				
					if (mBound) {
						if(isReseted){ sws.reset(); isReseted = false;}
						if(firstStart){sws.reset(); firstStart = false;}
						sws.resume();
						stopWatchChronometer.setBase(SystemClock.elapsedRealtime() - sws.getElapsedTime());
						stopWatchChronometer.start();
						isRunning = true;
						isStarted = true;
						isStopped = false;
					}
			}
		});
	    // STOP button
	    stopButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				
				stopWatchChronometer.stop();
				sws.pause();
				isRunning = false;
				isStopped = true;
				 
			}
        });
	    // RESET button
        resetButton.setOnClickListener(new OnClickListener() {
		
			@Override
			public void onClick(View v) {
				sws.reset();
				sws.pause();
				stopWatchChronometer.stop();
				stopWatchChronometer.setBase(SystemClock.elapsedRealtime());
				isReseted = true;
				isRunning = false;
				isStopped = true;
				
			}
       });
       
        
    }


	@Override
	public void onStop(){
		super.onStop();
            unbindService(mConnection);
            mBound = false;
	}
	
	@Override
	public void finish(){
		super.finish();
		
		if(isStopped == true){
	        if(sws != null){
	        	sws.clear();
	        }
		}
	}
	
	// Field used for binding
	private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            StopWatchBinder binder = (StopWatchBinder) service;
            sws = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };
	

	
	
	

	
}