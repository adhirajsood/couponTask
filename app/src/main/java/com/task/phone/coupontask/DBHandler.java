package com.task.phone.coupontask;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;



public class DBHandler extends SQLiteOpenHelper {

    // All Static variables
    // Database Version
    private static final int DATABASE_VERSION = 1;
    // Database Name
    private static final String DATABASE_NAME = "storeList";

    private static final String TABLE_STORES = "storeCouponDunia";

    public static final String KEY_BRAND_ID = "BrandID";
    public static final String KEY_BRAND_NAME = "BrandName";
    public static final String KEY_NEIGHBOURHOOD_NAME = "NeighbourhoodName";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_LATITUDE = "Latitude";
    public static final String KEY_LONGITUDE = "Longitude";
    public static final String KEY_LOGO_URL = "LogoURL";

    public DBHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    private static DBHandler instance;


    public static synchronized DBHandler getHelper(Context context) {
        if (instance == null)
            instance = new DBHandler(context);

        return instance;
    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        String CREATE_TABLE_LOCATIONS = "CREATE TABLE " + TABLE_STORES + "("
                + KEY_BRAND_ID + " TEXT,"
                + KEY_BRAND_NAME + " TEXT,"
                + KEY_NEIGHBOURHOOD_NAME + " TEXT,"
                + KEY_LOGO_URL + " TEXT,"
                + KEY_LATITUDE + " TEXT,"
                + KEY_LONGITUDE + " TEXT,"
                + KEY_DISTANCE + " DOUBLE)";

        try {

            db.execSQL(CREATE_TABLE_LOCATIONS);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STORES);

        // Create tables again
        onCreate(db);
    }

    public void addStores(JSONObject storesResponse) {
        try {

            SQLiteDatabase db = this.getWritableDatabase();

            ContentValues values = new ContentValues();
            try {
                values.put(KEY_BRAND_ID, storesResponse.getString(KEY_BRAND_ID));
                values.put(KEY_BRAND_NAME, storesResponse.getString(KEY_BRAND_NAME));
                values.put(KEY_NEIGHBOURHOOD_NAME, storesResponse.getString(KEY_NEIGHBOURHOOD_NAME));
                values.put(KEY_LOGO_URL, storesResponse.getString(KEY_LOGO_URL));
                values.put(KEY_LATITUDE, storesResponse.getString(KEY_LATITUDE));
                values.put(KEY_LONGITUDE, storesResponse.getString(KEY_LONGITUDE));
                values.put(KEY_DISTANCE, storesResponse.getDouble(KEY_DISTANCE));
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Inserting Row
            db.insertWithOnConflict(TABLE_STORES, null, values, SQLiteDatabase.CONFLICT_IGNORE);
            //db.close(); // Closing database connection
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public JSONArray getStores() {
        JSONArray storesLoc = new JSONArray();
        // Select All Query
        String selectQuery = "SELECT  * FROM " + TABLE_STORES +" ORDER BY "+KEY_DISTANCE+" ASC";

        System.out.println(selectQuery);
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        int i = 0;

        try{

            // looping through all rows and adding to list
            if (cursor.moveToFirst()) {
                do {
                    JSONObject locationResponse = new JSONObject();
                    try {
                        locationResponse.put(KEY_BRAND_ID, cursor.getString(0));
                        locationResponse.put(KEY_BRAND_NAME, cursor.getString(1));
                        locationResponse.put(KEY_NEIGHBOURHOOD_NAME, cursor.getString(2));
                        locationResponse.put(KEY_LOGO_URL, cursor.getString(3));
                        locationResponse.put(KEY_LATITUDE, cursor.getString(4));
                        locationResponse.put(KEY_LONGITUDE, cursor.getString(5));
                        locationResponse.put(KEY_DISTANCE, cursor.getString(6));

                        storesLoc.put(i, locationResponse);
                        i++;
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } while (cursor.moveToNext());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {

            if(!cursor.isClosed()) {
                cursor.close();
            }
        }

        //db.close();
        return storesLoc;
    }


    public void updateDistance(float distanceYou, String brandID) {

        SQLiteDatabase db = this.getWritableDatabase();

        String updateQuery = "";

        updateQuery = "update " + TABLE_STORES + " set "+KEY_DISTANCE +" = '"+ distanceYou +"' where " + KEY_BRAND_ID + " = " + brandID;


        db.execSQL(updateQuery);
    }
}

