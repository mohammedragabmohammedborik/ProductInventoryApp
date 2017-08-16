package com.example.mohammedragab.productinventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohammedragab.productinventoryapp.data.InventoryContract;

/**
 * Created by MohammedRagab on 6/15/2017.
 */

public class ProductCursorAdapter extends CursorAdapter {
    // create constructor
    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        // create layout for item list
        return LayoutInflater.from(context).inflate(R.layout.item_list, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        //  find  column of Product attribute that we intrest it
        int idColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        int quantityColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        final long id = cursor.getLong(idColIndex);
        final int quantity = cursor.getInt(quantityColIndex);
        int nameColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);

        if (cursor != null) {
            // find individual view that we want to modfiy in list layout view
            TextView nameTextView = (TextView) view.findViewById(R.id.ProductNameTextView);
            TextView quantityTextView = (TextView) view.findViewById(R.id.QuantityTextView);
            TextView priceTextView = (TextView) view.findViewById(R.id.PriceTextView);
            final Button saleButton = (Button) view.findViewById(R.id.ListSaleButton);
            // make a sale via sale button on item list
            saleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (quantity - 1 >= 0) {
                        Toast.makeText(context, "Sale!", Toast.LENGTH_SHORT).show();
                        ContentValues values = new ContentValues();
                        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity - 1);
                        Uri uri = ContentUris.withAppendedId(InventoryContract.InventoryEntry.CONTENT_URI, id);
                        context.getContentResolver().update(
                                uri,
                                values,
                                InventoryContract.InventoryEntry._ID + "=?",
                                new String[]{String.valueOf(id)}
                        );
                    }
                }
            });

            saleButton.setFocusable(false);
            nameTextView.setText(cursor.getString(nameColIndex));
            quantityTextView.setText(String.valueOf(cursor.getInt(quantityColIndex)));
            priceTextView.setText(String.valueOf(cursor.getInt(priceColIndex)));
        }
    }
}
