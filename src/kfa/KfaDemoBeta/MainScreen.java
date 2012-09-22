package kfa.KfaDemoBeta;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class MainScreen extends Activity {
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);
        
        Button toDoList = (Button)findViewById(R.id.buttonToDoList);
        Button stopWatch = (Button)findViewById(R.id.buttonStopWatch);
        Button map = (Button)findViewById(R.id.buttonMap);
        Button emailME = (Button)findViewById(R.id.emailMeActivityStarterButton);
        
        // Link for ToDolist Activity
        toDoList.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainScreen.this, ToDoListActivity.class));
				
			}
		});
        
        // Link for StopWatch Activity
        stopWatch.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				startActivity(new Intent(MainScreen.this, StopWatchActivity.class));
				
			}
		});
        
        // Link for Map Activity
        map.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent myIntent = new Intent(MainScreen.this, DistanceCalculatorActivity.class);
				myIntent.putExtra("isInitiating",true);
				startActivity(myIntent);
				
			}
		});
        
        // If pressed, calls the email client to compose a mail for me
        emailME.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				 Intent i = new Intent(Intent.ACTION_SEND);
			        i.setType("message/rfc822");
			        i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"kisferencadam@gmail.com"});
			        i.putExtra(Intent.EXTRA_SUBJECT, "Email from your app");
			        i.putExtra(Intent.EXTRA_TEXT   , "Hi Adam,\n\nPlease contact me.\n\nBest wishes,\nThe sender");
			        try {
			            startActivity(Intent.createChooser(i, "Send mail..."));
			        } catch (android.content.ActivityNotFoundException ex) {
			            Toast.makeText(MainScreen.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
			        }
				
			}
		});
        

        
    }
    

}