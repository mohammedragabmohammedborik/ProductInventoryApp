package com.example.mohammedragab.productinventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.mohammedragab.productinventoryapp.data.InventoryContract.InventoryEntry;


/**
 * Created by MohammedRagab on 6/11/2017.
 */

public class InventoryProvider extends ContentProvider {
    // craete instantiate object
    private InventoryDbHelper mProductDbHelper;
    /** URI matcher code for the content URI for the products table */
    private static final int PRODUCT = 100;

    /** URI matcher code for the content URI for a single product in the product table */
    private static final int Product_ID= 101;

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTS,PRODUCT);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY , InventoryContract.PATH_PRODUCTS + "/#" ,Product_ID);
    }
    // Tag for the log messages
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
       mProductDbHelper = new InventoryDbHelper(getContext());

        return true;
    }


    public Cursor query( Uri uri, String[] projection,  String selection, String[] selectionArgs,  String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mProductDbHelper.getReadableDatabase();
        // This cursor will hold the result of the query
        Cursor cursor;
        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch(match){

            case PRODUCT:
                // query all data in table 
                cursor= database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            case Product_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI.
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID for example equal 2 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the Products table where the _id equals 2 to return a
                // Cursor containing that row of the table.
                cursor= database.query(InventoryEntry.TABLE_NAME,projection,selection,selectionArgs,null,null,sortOrder);
                break;
            default:throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // set notification uri on a cursor for anyupdate data change.
        cursor.setNotificationUri(getContext().getContentResolver(),uri);
        return cursor;

    }
    @Override
    public String getType( Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case Product_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert( Uri uri,ContentValues values) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case PRODUCT :
                return insertProduct( uri, values);
            default: throw new  IllegalArgumentException("Insertio not supportwith ur "+ uri);
        }
    }
    private Uri insertProduct(Uri uri, ContentValues values){
        SQLiteDatabase database=mProductDbHelper.getWritableDatabase();
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product  requires a name");
        }

        // Check that the price is valid
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
        if (price == null && price<0 ) {
            throw new IllegalArgumentException("Product requires price");
        }

        // check for the quantity
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        if (quantity == null &&quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }
        String supplier = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
        if (supplier == null) {
            throw new IllegalArgumentException("Product  requires a supplier name");
        }

        // Insert the new product with the given values
        long id = database.insert(InventoryEntry.TABLE_NAME, null, values);
        // If the ID is -1, then the insertion failed. Log an error and return null.
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        // notifiy all listener that data changed for Product uri
        getContext().getContentResolver().notifyChange(uri,null);

       // return the new uri with id of newly inserted

        return ContentUris.withAppendedId(uri, id);
    }
    @Override
    public int delete( Uri uri, String selection, String[] selectionArgs) {
        // Get writeable database
        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();
        // Track the number of rows that were deleted
        int rowsDeleted;

        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                // Delete all rows that match the selection and selection args
                // For  case Product:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case Product_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                // For case Product_ID:
                // Delete a single row given by the ID in the URI
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        // Return the number of rows deleted
        return rowsDeleted;

}
    @Override
    public int update( Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCT:
                return updateProduct(uri, values, selection, selectionArgs);
            case Product_ID:
                // For the PRODUCT_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateProduct(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }
    /**
     * Update Products in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more product).
     * Return the number of rows that were successfully updated.
     */
    private int updateProduct(Uri uri, ContentValues values, String selection, String[] selectionArgs) {

        // Otherwise, get writeable database to update the data
        SQLiteDatabase database = mProductDbHelper.getWritableDatabase();
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_SUPPLIER)) {
            String supplier = values.getAsString(InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            if (supplier == null ) {
                throw new IllegalArgumentException("Product requires supplier name");
            }
        }

        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_PRICE)) {
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_PRICE);
            if (price == null && price < 0) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }
        if (values.containsKey(InventoryEntry.COLUMN_PRODUCT_QUANTITY)) {
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("need validate quantity");
            }
        }
        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return  rowsUpdated;
    }
}
