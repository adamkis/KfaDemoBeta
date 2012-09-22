package kfa.KfaDemoBeta;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;

/**
 * The phone's local SQLite DB manager.
 * The master of the synch (to web) is the phone's DB, when the synch happens,
 * the whole list is sent.
 * 
 *
 */
public class DatabaseHandler extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "toDoDB.db";
	private static final int DATABASE_VERSION = 1;
	
	// Table name
	private static final String TABLE_TODOS = "todos";
	
	//Table columns
	private static final String KEY_ID = "id";
	private static final String KEY_TODO = "todo";
	
	private static SQLiteDatabase db ;
	public Context toDoListActivityContext;
	
	public DatabaseHandler(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.toDoListActivityContext = context.getApplicationContext();
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		String CREATE_TODOS_TABLE = "CREATE TABLE " + TABLE_TODOS + 
				"(" + KEY_ID + " INTEGER PRIMARY KEY, " + KEY_TODO + " TEXT);" ;
		try {
			db.execSQL(CREATE_TODOS_TABLE);
		} catch (SQLException e) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
        	db.execSQL("DROP TABLE IF EXISTS " + TABLE_TODOS);
			onCreate(db);
		} catch (Exception e) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
		}
	}
	
	protected void addToDo(String todo){
		try {
			db = this.getReadableDatabase();
			ContentValues values = new ContentValues();
			values.put(KEY_TODO , todo);
			db.insert(TABLE_TODOS, null, values);
			db.close();
		} catch (Exception e) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
		}
	}
	
	/**
	 * Reads all todos out of the SQLite db and returns it as an Arraylist
	 * 
	 * @return 	The Arraylist of todos.
	 * 			The elements of it are String arrays containing: id, the text of the todo 
	 */
	protected ArrayList<String[]> getAllTodos(){
		
		ArrayList<String[]> todos = new ArrayList<String[]>();
		String selectQuery = "SELECT * FROM " + TABLE_TODOS + " ORDER BY todo";
		
		try {
			db = this.getReadableDatabase();
			Cursor cursor = db.rawQuery(selectQuery, null);

			if(cursor.moveToFirst()){
				
				do{
					String[] toDoTableRow = new String[2];
					toDoTableRow[0] = cursor.getString(0);
					toDoTableRow[1] = cursor.getString(1);
					todos.add(toDoTableRow);
				}
				while(cursor.moveToNext());
			}
			db.close();
		} catch (Exception e) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
		}
		
		return todos;
		
	}
	
	protected void deleteToDo(String todoIndex){
		try {
			db = this.getWritableDatabase();
			db.delete(TABLE_TODOS, KEY_ID + " = ?", new String[]{todoIndex});
			db.close();
		} catch (Exception e) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
		}
	}
	
	@Override
	protected void finalize() throws Throwable{
		try {
			db.close();	
		} catch (Throwable t) {
			Toast.makeText(toDoListActivityContext, "Ohh snap... Couldn't access local database", Toast.LENGTH_LONG).show();
			throw t;
		} finally{
			super.finalize();
		}
	}
	
	

}
