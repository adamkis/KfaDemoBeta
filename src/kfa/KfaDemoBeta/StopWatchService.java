package kfa.KfaDemoBeta;



import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;


public class StopWatchService extends Service {

	private final IBinder mBinder = new StopWatchBinder();
	
	public static long startTime ;
	public static long resumeTime ;
	static long pauseTime ;
	static boolean paused ;
	
	static boolean onInstantiation = true;
	
	int STOPWATCH_NOTIF_ID = 1;
	private NotificationManager mNotificationManager ;
	
	public class StopWatchBinder extends Binder {
		StopWatchService getService() {
            return StopWatchService.this;
        }
    }
	
	@Override
	public IBinder onBind(Intent arg0) {
		return mBinder;
	}

	@Override
	public void onCreate(){
		
		if( onInstantiation == true ){
			
			startTime = System.currentTimeMillis();
			resumeTime = 0;
			paused = false;
			onInstantiation = false ;
		}
		mNotificationManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		showNotification();
	}	

	
	
	public void pause(){
		if(!paused){
			pauseTime = System.currentTimeMillis();
			paused = true;
		}
	}
	
	public void resume(){
		if(paused){
			resumeTime = ( System.currentTimeMillis() - pauseTime ) + resumeTime ;
			paused = false ;
		}
	}
	
	public long getElapsedTime(){
		
		if(paused){
			return ( ( pauseTime - startTime - resumeTime ) );
		}
		else{
			return ( System.currentTimeMillis() - startTime - resumeTime);
		}
		
		
	}
	public void reset(){
		startTime = System.currentTimeMillis();
		resumeTime = 0;
		pauseTime = 0 ;
		paused = false;
	}


	/**
	 * Kills the StopWatchService notification, attempts to finalize the class
	 */
	public void clear(){
		try {
			this.finalize();
			mNotificationManager.cancelAll();
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}


	@SuppressWarnings("deprecation")
	public void showNotification(){
		
		int icon = R.drawable.stopwatchicon3;
		CharSequence tickerText = "Stopwatch started...";
		
		long when = System.currentTimeMillis();		
		Notification notification = new Notification(icon, tickerText, when);
		
		CharSequence contentTitle = "KfaDemo Stopwatch is running";
		CharSequence contentText = "Service is running";
		Intent notificationIntent = new Intent(this, StopWatchActivity.class);
		
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

		notification.setLatestEventInfo(this, contentTitle, contentText, contentIntent);
		
		mNotificationManager.notify(STOPWATCH_NOTIF_ID, notification);
		
	}


	
}
