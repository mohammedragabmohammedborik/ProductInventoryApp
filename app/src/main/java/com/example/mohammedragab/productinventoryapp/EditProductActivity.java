package com.example.mohammedragab.productinventoryapp;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mohammedragab.productinventoryapp.data.InventoryContract;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import static com.example.mohammedragab.productinventoryapp.R.string.quantity;
import static java.lang.Integer.parseInt;

public class EditProductActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = EditProductActivity.class.getSimpleName();

    private boolean mHasSetImage = false;

    private boolean mProductHasChanged = false;

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private int mQuantity;

    private static final int PICK_IMAGE_REQUEST = 0;

    // Identifies a particular Loader being used in this component
    private static final int PRODUCT_LOADER = 0;

    private Uri mCurrentProductUri;

    private ImageView mProductImageView;
    private EditText mNameEditText;
    private EditText mSupplierEditText;
    private EditText mPriceEditText;
    private EditText mQuantityEditText;
    private TextView mQuantityTextView;

    private Button mSaleButton;
    private ImageButton mIncrementButton;
    private ImageButton mDecrementButton;
    private ImageButton mOrderButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit);

        // Initializing all the view variables associated to both product activity "modes".
        mNameEditText = (EditText) findViewById(R.id.ProductNameEditText);
        mSupplierEditText = (EditText) findViewById(R.id.SupplierEditView);
        mPriceEditText = (EditText) findViewById(R.id.PriceEditText);
        mQuantityEditText = (EditText) findViewById(R.id.QuantityEditText);
        mProductImageView = (ImageView) findViewById(R.id.ProductImageView);




        // Set onTouchListeners for all editviews/imageviews
        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupplierEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mProductImageView.setOnTouchListener(mTouchListener);

        // If data is passed by intent, then display the current product in questions information\
        // Also known as "Edit mode"
        if (getIntent().getData() != null) {
            // Defining all the variables required for just the editing process
            mQuantityTextView = (TextView) findViewById(R.id.displayQuantity);
            mSaleButton = (Button) findViewById(R.id.SaleButton);
            mIncrementButton = (ImageButton) findViewById(R.id.IncrementButton);
            mDecrementButton = (ImageButton) findViewById(R.id.DecrementButton);
            mOrderButton = (ImageButton) findViewById(R.id.OrderButton);

            // Adding additional onTouchListeners to the newly tracked variables:
            mSaleButton.setOnTouchListener(mTouchListener);
            mIncrementButton.setOnTouchListener(mTouchListener);
            mDecrementButton.setOnTouchListener(mTouchListener);

            // Since we are in editmode, can hide the quantity editText and replace it with the
            // quantity controller view layout.
            mQuantityEditText.setVisibility(View.GONE);
            findViewById(R.id.QantityLayout).setVisibility(View.VISIBLE);

            mCurrentProductUri = getIntent().getData();
            setTitle(getString(R.string.edit_product));

            // Handle the various quantity alteration options
            mSaleButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(-1);
                }
            });
            mIncrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(1);
                }
            });
            mDecrementButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alterQuantity(-1);
                }
            });


            // Handle the order button request
            mOrderButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int i=0;
                    String textFromInput = mQuantityTextView.getText().toString();
                    if(!textFromInput.isEmpty()){
                         i =Integer.parseInt(textFromInput);}
                        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
                        emailIntent.setData(Uri.parse("mailto:" + mSupplierEditText.getText().toString()));
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Product Order");
                        emailIntent.putExtra(Intent.EXTRA_TEXT, "I would like to order "+String.valueOf(i)+"\t"+mNameEditText.getText().toString());
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send email using..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(EditProductActivity.this, "No email clients installed.", Toast.LENGTH_SHORT).show();
                        }
                    }

            });

            getLoaderManager().initLoader(PRODUCT_LOADER, null, this);

        } else {
            mQuantityEditText.setOnTouchListener(mTouchListener);
            setTitle(getString(R.string.add_product));
            findViewById(R.id.productQuantityOptions).setVisibility(View.INVISIBLE);
            findViewById(R.id.QantityLayout).setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        }

        mProductImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openImageSelector();
            }
        });

    }

    //method to set quantity increment or decrement.
    private void alterQuantity(int increment) {
        if (mQuantity + increment == 0) {

            mSaleButton.setEnabled(false);
            mDecrementButton.setEnabled(false);
            mDecrementButton.setVisibility(View.INVISIBLE);
        } else {
            mSaleButton.setEnabled(true);
            mDecrementButton.setEnabled(true);
            mDecrementButton.setVisibility(View.VISIBLE);
        }
        if (quantity + increment >= 0) {
            mQuantity += increment;
            mQuantityTextView.setText(String.valueOf(mQuantity));
        }
    }

    @Override
    public void onBackPressed() {
        // If the Product hasn't changed, continue with handling back button press
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.discard_changes);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the product.
                deleteProduct();
                finish();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing product the Product.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new Product, hide the "Delete" menu item.
        if (mCurrentProductUri == null) {
            menu.findItem(R.id.action_delete).setVisible(false);
            menu.findItem(R.id.action_save).setVisible(true);
        } else {
            menu.findItem(R.id.action_delete).setVisible(true);
            menu.findItem(R.id.action_save).setIcon(R.drawable.ic_save_white_24dp);
        }

        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                if (validateProductInfo()) {
                    saveProduct();
                    finish();
                }
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();

                return true;
            case android.R.id.home:
                // If product hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditProductActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // method check for integer
    private boolean isInteger(String stringValue) {
        try {
            parseInt(stringValue);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    // method for valdiate entering informations
    private boolean validateProductInfo() {
        String productName = mNameEditText.getText().toString().trim();
        String supplier = mSupplierEditText.getText().toString().trim();
        String priceStr = mPriceEditText.getText().toString();
        String quantityStr;
        if (mCurrentProductUri != null) {
            quantityStr = mQuantityTextView.getText().toString();
        } else {
            quantityStr = mQuantityEditText.getText().toString();
        }
        /** Stage one of testing, need to make sure all the required fields are not empty */
        if (TextUtils.isEmpty(productName)) {
            Toast.makeText(this,"please enter product name",Toast.LENGTH_LONG).show();
            return false;
        }else if(TextUtils.isEmpty(supplier)){Toast.makeText(this,"please enter supplier name",Toast.LENGTH_LONG).show();
        return false;}
        else if(TextUtils.isEmpty(priceStr)){Toast.makeText(this,"please enterenter price",Toast.LENGTH_LONG).show();
        return false;}else if(TextUtils.isEmpty(quantityStr)){
            Toast.makeText(this,"please enterenter price",Toast.LENGTH_LONG).show();
            return false;
        }

        try {
            Integer.parseInt(priceStr);
        } catch (NullPointerException e) {
            mPriceEditText.setError(getString(R.string.numeric_not_null));
            return false;
        }

        if (supplier.isEmpty()) {
            mSupplierEditText.setError(getString(R.string.invalid_supplier));
            return false;
        }

        try {
            Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            mQuantityEditText.setError(getString(R.string.numbers_only));
            return false;
        } catch (NullPointerException e) {
            mQuantityEditText.setError(getString(R.string.numeric_not_null));
            return false;
        }

        return true;
    }


    /**
     * Get user input from editor and save new product into database.
     */
    private void saveProduct() {
        ContentValues values = new ContentValues();
        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME, mNameEditText.getText().toString());

        if (!mHasSetImage) {
            values.putNull(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);
        } else {
            Bitmap bitmap = ((BitmapDrawable) mProductImageView.getDrawable()).getBitmap();
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE, getBitmapAsByteArray(bitmap));
        }

        values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER, mSupplierEditText.getText().toString());

        boolean success = false;

        String priceStr = mPriceEditText.getText().toString();
        if (!priceStr.isEmpty()) {
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE, Double.parseDouble(priceStr));
        }
        if (mCurrentProductUri != null) {
            values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, mQuantity);
        } else {
            String quantityStr = mQuantityEditText.getText().toString();
            if (!quantityStr.isEmpty() && isInteger(quantityStr)) {
                values.put(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY, parseInt(quantityStr));
            }
        }

        if (mCurrentProductUri != null) {
            int numUpdated = getContentResolver().update(mCurrentProductUri, values, null, null);
            // Show a toast message depending on whether or not the insertion was successful
            if (numUpdated != -1) {
                // If the row ID is -1, then there was an error with insertion.
                success = true;
            }
        } else {
            // Insert a new row for Product in the database, returning the ID of that new row.
            Uri newRowUri = getContentResolver().insert(InventoryContract.InventoryEntry.CONTENT_URI, values);
            // Show a toast message depending on whether or not the insertion was successful
            if (newRowUri != null) {
                success = true;
            }
        }

        if (!success) {
            // If the row ID is -1, then there was an error with insertion.
            Toast.makeText(this, R.string.operation_error, Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast with the row ID.
            Toast.makeText(this, R.string.operation_successful, Toast.LENGTH_SHORT).show();
        }

    }

    private void deleteProduct() {
        // Only proceed if the current activity is in "edit" mode.
        if (mCurrentProductUri != null) {
            int deleteCount = getContentResolver().delete(mCurrentProductUri, null, null);

            if (deleteCount == 0) {
                Toast.makeText(this, "Error deleting product", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "Product deleted", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * Take a byte array and decode the stream into a bitmap
     *
     * @param imgByte
     * @return
     */
    public Bitmap getImage(byte[] imgByte) {
        ByteArrayInputStream imageStream = new ByteArrayInputStream(imgByte);
        return BitmapFactory.decodeStream(imageStream);
    }


    public static byte[] getBitmapAsByteArray(Bitmap bitmap) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        return outputStream.toByteArray();
    }


    public void openImageSelector() {
        Intent intent;
        intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    /**
     * Method handles the activity result from invoking an ACTION_PICK intent
     * Main body of the method was copied from the udacity's mentors code found here:
     * https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param requestCode , the intent request code
     * @param resultCode  , status of the result
     * @param resultData  , intent containing the returned data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                Uri uri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + uri.toString());

                int padding = 6;
                mProductImageView.setPadding(padding, padding, padding, padding);
                mProductImageView.setImageBitmap(getBitmapFromUri(uri));
                mHasSetImage = true;
            }
        }
    }

    /**
     * Function designed to extract a bitmap from the uri returned from an image gallery activity.
     * Function extracted from udacity mentors example for image selection:
     * https://github.com/crlsndrsjmnz/MyShareImageExample
     *
     * @param uri returned from the intent activity
     * @return bitmap for the image passed back
     */
    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mProductImageView.getWidth();
        int targetH = mProductImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        switch (id) {
            case PRODUCT_LOADER:
                String[] projection = {
                        InventoryContract.InventoryEntry._ID,
                        InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME,
                        InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE,
                        InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                        InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER,
                        InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE};

                // Returns a new CursorLoader
                return new CursorLoader(
                        this,                       // Parent activity context
                        mCurrentProductUri,        // Table to query
                        projection,     // Projection to return
                        null,            // No selection clause
                        null,            // No selection arguments
                        null             // Default sort order
                );
            default:
                // An invalid id was passed in
                return null;
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            int nameColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int imgColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_IMAGE);
            int priceColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_PRICE);
            int supplierColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_SUPPLIER);
            int quantityColIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_QUANTITY);


            if (cursor != null) {

                mNameEditText.setText(cursor.getString(nameColIndex));
                int price = Integer.parseInt(cursor.getString(priceColIndex));
                mPriceEditText.setText(String.valueOf(price));
                mSupplierEditText.setText(cursor.getString(supplierColIndex));

                mQuantity = cursor.getInt(quantityColIndex);
                mQuantityTextView.setText(String.valueOf(mQuantity));


                byte[] image = cursor.getBlob(imgColIndex);
                if (image != null) {
                    mProductImageView.setImageBitmap(getImage(image));
                    int padding = 6;
                    mProductImageView.setPadding(padding, padding, padding, padding);
                }
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mSupplierEditText.setText("");
        mQuantityEditText.setText("");
        mProductImageView.setImageResource(R.drawable.ic_add_a_photo_white_24dp);
        int padding = 20;
        mProductImageView.setPadding(padding, padding, padding, padding);
    }
}