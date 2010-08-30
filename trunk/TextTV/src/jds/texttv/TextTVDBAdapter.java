package jds.texttv;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Simple notes database access helper class. Defines the basic CRUD operations
 * for the notepad example, and gives the ability to list all notes as well as
 * retrieve or modify a specific note.
 * 
 * This has been improved from the first version of this tutorial through the
 * addition of better error handling and also using returning a Cursor instead
 * of using a collection of inner classes (which is less scalable and not
 * recommended).
 */
public class TextTVDBAdapter {

    public static final String KEY_PAGE = "page";
    public static final String KEY_NAME = "name";
    public static final String KEY_ROWID = "_id";

    
    public static final int INDEX_ROWID = 0;
    public static final int INDEX_PAGE = 1;    
    public static final int INDEX_NAME = 2;
                
    private static final String TAG = "TextTVDBAdapter";
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    
    /**
     * Database creation sql statement
     */
    private static final String DATABASE_CREATE =
            "create table favorites (_id integer primary key autoincrement, "
                    + "page int, name string);";                    

    private static final String DATABASE_NAME = "user_data.db";
    private static final String DATABASE_TABLE = "favorites";
    private static final int DATABASE_VERSION = 1;

    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

            db.execSQL(DATABASE_CREATE);
            Log.d(TAG,"Database created"); 
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion);
            /*
            if (oldVersion == 3)
            {
            	//Add three columns
            	db.execSQL("ALTER TABLE favorites ADD COLUMN filesize INT");
            	db.execSQL("ALTER TABLE favorites ADD COLUMN bytesdownloaded INT");
            }
            else
            {
            	db.execSQL("DROP TABLE IF EXISTS favorites");
            	onCreate(db);
            }
            */
            db.execSQL("DROP TABLE IF EXISTS favorites");
        	onCreate(db);
        }
    }

    /**
     * Constructor - takes the context to allow the database to be
     * opened/created
     * 
     * @param ctx the Context within which to work
     */
    public TextTVDBAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    /**
     * Open the SRPlayer database. If it cannot be opened, try to create a new
     * instance of the database. If it cannot be created, throw an exception to
     * signal the failure
     * 
     * @return this (self reference, allowing this to be chained in an
     *         initialization call)
     * @throws SQLException if the database could be neither opened or created
     */
    public TextTVDBAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();        
        return this;
    }
    
    public void close() {
        mDbHelper.close();
    }


    /**
     * Create a new favorite
     * 
     * @param title the title of the note
     * @param body the body of the note
     * @return rowId or -1 if failed
     */
    public long createFavorite(int Page, String Name) {
    	Log.d(TAG,"New row in datase");
    	ContentValues initialValues = new ContentValues();
        initialValues.put(KEY_PAGE, Page);
        initialValues.put(KEY_NAME, Name);                
        
        return mDb.insert(DATABASE_TABLE, null, initialValues);
    }

    /**
     * Delete the favorite with the given rowId
     * 
     * @param rowId id of note to delete
     * @return true if deleted, false otherwise
     */
    public boolean deleteFavorite(long rowId) {

        return mDb.delete(DATABASE_TABLE, KEY_ROWID + "=" + rowId, null) > 0;
    }

    /**
     * Delete the favorite with the given rowId
     * 
     * @param Page Page number of the favorite
     * @return true if deleted, false otherwise
     */
    public boolean deleteFavorite(int Page) {

        return mDb.delete(DATABASE_TABLE, KEY_PAGE + "=" + Page, null) > 0;
    }

    
    
    /**
     * Return a Cursor positioned at the page that matches the given rowId
     * 
     * @param rowId id of favorite to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchFavorite(long rowId) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PAGE, KEY_NAME}, KEY_ROWID + "=" + rowId, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }
    
    public Cursor fetchAllFavorites() throws SQLException {

        Cursor mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PAGE, KEY_NAME}, null, null,
                        null, null, KEY_PAGE, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }

    
    /**
     * Return a Cursor positioned at the page that matches the given rowId
     * 
     * @param Page of favorite to retrieve
     * @return Cursor positioned to matching note, if found
     * @throws SQLException if note could not be found/retrieved
     */
    public Cursor fetchFavorite(int Page) throws SQLException {

        Cursor mCursor =

                mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                        KEY_PAGE, KEY_NAME}, KEY_PAGE + "=" + Page, null,
                        null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;

    }


    /**
     * Update the favorite
     */
    public boolean updateFavorite(long rowId, int Page, String Name) {
        ContentValues args = new ContentValues();
        args.put(KEY_PAGE, Page);
        args.put(KEY_NAME, Name);        
        
        return mDb.update(DATABASE_TABLE, args, KEY_ROWID + "=" + rowId, null) > 0;
    }
     
    /**
     * Get the next favorite
     */
    public Cursor getNextFavorite(int Page) {
    	Cursor mCursor =

        mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_PAGE, KEY_NAME}, KEY_PAGE + ">" + Page, null,
                    null, null, KEY_PAGE, "5");    	
	    if (mCursor != null) {
	    	if (mCursor.getCount() == 0)
	    	{
	    		mCursor.close();
	    		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
	                        KEY_PAGE, KEY_NAME}, null, null,
	                        null, null, KEY_PAGE, "5");

	    	}
	    		
	        mCursor.moveToFirst();
	    }
	    
	    
	    
	    return mCursor;
    }
    
    /**
     * Get the prev favorite
     */
    public Cursor getPrevFavorite(int Page) {
    	Cursor mCursor =

        mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_PAGE, KEY_NAME}, KEY_PAGE + "<" + Page, null,
                    null, null, KEY_PAGE + " DESC", "5");
	    if (mCursor != null) {
	    	if (mCursor.getCount() == 0)
	    	{
	    		mCursor.close();
	    		mCursor = mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
	                        KEY_PAGE, KEY_NAME}, null, null,
	                        null, null, KEY_PAGE + " DESC", "5");

	    	}
	    		
	        mCursor.moveToFirst();
	    }
	    
	    
	    
	    return mCursor;
    }

    
    /**
     * Return if a page is in the database 
     * 
     * @return Cursor
     */
    public boolean isPageInDB(int Page) {    
    	Cursor mCursor =

            mDb.query(true, DATABASE_TABLE, new String[] {KEY_ROWID,
                    KEY_PAGE, KEY_NAME}, KEY_PAGE + "=" + Page, null,
                    null, null, null, null);
	    if (mCursor != null) {
	    	
	        return false;
	    }
	    return true;

    }
}
