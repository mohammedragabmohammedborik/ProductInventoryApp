package com.example.mohammedragab.productinventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by MohammedRagab on 6/11/2017.
 */

public class InventoryContract {

    /**
     * The "Content authority" is a name for the entire content provider,   A convenient string to use for the
     * content authority is the package name for the app, which is guaranteed to be unique on the
     * device.
     */
    public static final String CONTENT_AUTHORITY = "com.example.mohammedragab.productinventoryapp";

    /**
     * Use CONTENT_AUTHORITY to create the base of all URI's which apps will use to contact
     * the content provider.
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Possible path (appended to base content URI for possible URI's)
     * For instance, content://com.example.android.productinventoryapp/productinventoryapp/ is a valid path for
     * looking at product data. content://com.example.android.productinventoryapp/staff/ will fail,
     * as the ContentProvider hasn't been given any information on what to do with "staff".
     */
    public static final String PATH_PRODUCTS = "productinventoryapp";

    /**
     * Inner class that defines constant values for the product database table.
     */

   //  constructor To redefine the scope of the constructor. Making the constructor private will prevent
    // anyone but the class itself from constructing an object.
    private InventoryContract() {
    }

    /**
     * Inner class that defines constant values for the products database table.
     * Each entry in the table represents a single product.
     */
    public static final class InventoryEntry implements BaseColumns {
        /**
         * The content URI to access the inventory data in the provider
         */
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTS);
        /**
         * Name of database table for products
         */
        public final static String TABLE_NAME = "product";
        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory products.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;
        /**
         * The MIME type of the {@link #CONTENT_URI} for a single product.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTS;

        /**
         * Unique ID number for the product (only for use in the database table).
         * Type: INTEGER
         */
        public final static String _ID = BaseColumns._ID;

        /**
         * Name of the Product.
         */
        public final static String COLUMN_PRODUCT_NAME = "name";
        /**
         * Price of Product.
         * Type: Integer
         */
        public final static String COLUMN_PRODUCT_PRICE = "price";
        // Quantity of a product that available.
        public final static String COLUMN_PRODUCT_QUANTITY = "quantity";
        // supplier of product.
        public final static String COLUMN_PRODUCT_SUPPLIER = "supplier";
        // image product.
        public final static String COLUMN_PRODUCT_IMAGE = "image";
    }
}

