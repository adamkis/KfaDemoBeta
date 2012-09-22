package kfa.KfaDemoBeta;


import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.text.SpannableString;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ToDoListActivity extends Activity {
    /** Called when the activity is first created. */
	
	private static String session_id;
	
	ListView toDoListView ;
	
	// List of all todos. Elements: String[]{"ID of todo", "Text of todo"}
	// This is the structure, the SQLite DB returns, when the whole list is fetched
	static ArrayList<String[]> toDoItems ;
	// List of all todos, just the texts
	static ArrayList<String> toDoTexts ;
	static ArrayAdapter<String> toDoListAdapter;
	TextView footer;
	DatabaseHandler db;
	
	final Context appContext = this;
    
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.todolistscreen);
        
        db = new DatabaseHandler(this);
        /**
         * If no session ID is present for web use, it calls the webservice for generation.
         * Getting the response it writes it in the shared preferences
         * 
         * if there is a session id, it uses it for synch
         */
        SharedPreferences settings = getPreferences(0);
        if( settings.getString("SESSION_ID", "empty").equals("empty")){
        	
        	String android_id = Secure.getString(getBaseContext().getContentResolver(), Secure.ANDROID_ID); 
            ToDoSoapClient myToDoSoap = new ToDoSoapClient(this);
    		session_id = myToDoSoap.doSoapCall(android_id, "generateSid");
        	
            SharedPreferences.Editor editor = settings.edit();
            editor.putString("SESSION_ID", session_id);
            editor.commit();
        	
        }
        else{
            Log.i("myDebug", "SessionId:" + settings.getString("SESSION_ID", "empty") );
            session_id = settings.getString("SESSION_ID", "empty") ;
        }
        
        toDoListView = (ListView)findViewById(R.id.listView1);
        footer = (TextView)findViewById(R.id.footer);
        final EditText addToDoItem = (EditText)findViewById(R.id.addToDoItem);
        final Button  addToDoItemButton = (Button)findViewById(R.id.button1);
        final Button  todoUsageButton = (Button)findViewById(R.id.todoUsageButton);
        if (savedInstanceState == null){
        	showInfo();
        	toDoItems = db.getAllTodos();
        	toDoTexts = new ArrayList<String>();
        	for(int i=0; i<toDoItems.size(); i++){
        		toDoTexts.add(toDoItems.get(i)[1]);
        	}

        }
        
        footer.setText("SID: " + session_id + "     Please long-click an item" );
        toDoListAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, toDoTexts);
        toDoListView.setAdapter(toDoListAdapter);
        
        registerForContextMenu(toDoListView);

        // Add button
        addToDoItemButton.setOnClickListener(new OnClickListener() {
        
			@Override
			public void onClick(View v) {
				
				String toDoText = addToDoItem.getText().toString();
				
				if( toDoText != null && toDoText.length()>0 ){
				
					db.addToDo(toDoText);
					toDoItems = db.getAllTodos();
					toDoTexts = new ArrayList<String>();
					for(int i=0; i<toDoItems.size(); i++){
						toDoTexts.add(toDoItems.get(i)[1]);
			        }
					
					toDoListAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, toDoTexts);
					toDoListView.setAdapter(toDoListAdapter);
					
					toDoListAdapter.notifyDataSetChanged();
					addToDoItem.setText("");
					
					ToDoSoapClient myToDoSoap = new ToDoSoapClient(getApplicationContext());
					myToDoSoap.doSoapCall(myToDoSoap.buildTodoXML(toDoItems, session_id), "synch");
					
					Toast.makeText(getBaseContext(), "Todo made", Toast.LENGTH_SHORT).show();
				}
				else{
					Toast.makeText(getBaseContext(), "Please add text", Toast.LENGTH_LONG).show();
				}
		    	
			}
		});
        
        // Usage button
        todoUsageButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				showInfo();
			}
		});
        
    }
 
    /**
     * Shows a dialog with the instructions
     */
    public void showInfo(){
	    	
	    final TextView infoMessage = new TextView(appContext);
	    infoMessage.setTextSize(20);
	    infoMessage.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
	    infoMessage.setBackgroundColor(Color.WHITE);
	    infoMessage.setTextColor(Color.BLACK);
	
	    // Create links for URLs
		final SpannableString s = 
	       new SpannableString(		getApplicationContext().getText(R.string.todoListInfo1) 
	    		   				+ 	session_id
	    		   				+ 	getApplicationContext().getText(R.string.todoListInfo2));
		Linkify.addLinks(s, Linkify.WEB_URLS);
		infoMessage.setText(s);
		infoMessage.setMovementMethod(LinkMovementMethod.getInstance());
	    	
		AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
				appContext);
		alertDialogBuilder.setTitle("Information");
 
		// Dialog message with 1 button, that closes the dialog
		alertDialogBuilder
			.setView(infoMessage)
			.setCancelable(false)
			.setPositiveButton("Dismiss",new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog,int id) {
					dialog.cancel();
				}
			  });

		AlertDialog alertDialog = alertDialogBuilder.create();
		alertDialog.show();
    	
    }
    
    
    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuinfo ){
    	
    	if(v.getId() == toDoListView.getId()){
    	
	    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuinfo;
	    	menu.setHeaderTitle(toDoTexts.get(info.position));
	    	
	    	menu.add(Menu.NONE, 0, 0, "Delete");
	    	menu.add(Menu.NONE, 1, 1, "Back");
    	}
    } 
    
    @Override
    public boolean onContextItemSelected(MenuItem item){
    	
    	AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
    	int menuItemIndex = item.getItemId();
    	
    	String listItem = toDoTexts.get(info.position);
    	
    	// Delete
    	if (menuItemIndex==0){
    		
    		db.deleteToDo(toDoItems.get(info.position)[0]);
    		
    		toDoItems = db.getAllTodos();
			toDoTexts = new ArrayList<String>();
			for(int i=0; i<toDoItems.size(); i++){
				toDoTexts.add(toDoItems.get(i)[1]);
	        }
    		
    		toDoListAdapter = new ArrayAdapter<String>(getBaseContext(), android.R.layout.simple_list_item_1, toDoTexts);
			toDoListView.setAdapter(toDoListAdapter);
			toDoListAdapter.notifyDataSetChanged();
			
			ToDoSoapClient myToDoSoap = new ToDoSoapClient(this);
			myToDoSoap.doSoapCall(myToDoSoap.buildTodoXML(toDoItems, session_id), "synch");
			
    		footer.setText("SID: " + session_id + "    \"" + item.toString()+listItem+"\" Deleted");

    	}
    	
    	//Back
    	if (menuItemIndex == 1){
    		footer.setText("SID: " + session_id + "     Please long-click an item");	 
    	}
    	
    	return true;
    }
    
    
    @Override
    public void onPause(){
    	super.onPause();
    	
    	toDoItems = db.getAllTodos();
    	
    	if(this.isFinishing()){

	    	ToDoSoapClient myToDoSoap = new ToDoSoapClient(this);
			myToDoSoap.doSoapCall(myToDoSoap.buildTodoXML(toDoItems, session_id), "synch");    	

    	}
    }
    
    
    
}