package com.ait.dragrecycleritem;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;

public class Utils {
    public boolean isOnline(Context context) {
        ConnectivityManager conMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = conMgr.getActiveNetworkInfo();

        if (netInfo == null || !netInfo.isConnected() || !netInfo.isAvailable()) {
            return false;
        }
        return true;
    }

    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
                return false;
            }
            boolean mode = locationMode != Settings.Secure.LOCATION_MODE_OFF;
            return mode;

        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }

    }

    public void showlocationAlert(final Context context) {
        android.app.AlertDialog.Builder alertDialog = new android.app.AlertDialog.Builder(context);

        alertDialog.setTitle("Location not found,please turn on your device location");

        alertDialog.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                openGpsEnableSetting(context);
            }
        });

        alertDialog.setCancelable(false);
        alertDialog.show();
    }

    private void openGpsEnableSetting(Context context) {
        Intent intent = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        context.startActivity(intent);

    }

    public boolean hasPermission(Context context, String permission) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return (context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED);
        }
        return true;
    }

    public Bitmap getBluePinWithNumber(Context context, int number) {

        Bitmap backgroundBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.blue_pin);
        Bitmap resultBitmap = Bitmap.createBitmap(backgroundBitmap.getWidth(), backgroundBitmap.getHeight(), backgroundBitmap.getConfig());

        Canvas canvas = new Canvas(resultBitmap);
        canvas.drawBitmap(backgroundBitmap, new Matrix(), null);

        // Draw text on canvas...
        Paint textPaint = new Paint();
        textPaint.setTextAlign(Paint.Align.CENTER);
        //textPaint.setTypeface(Typeface.DEFAULT);
        textPaint.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        if (number >= 10)
            textPaint.setTextSize(30);
        else
            textPaint.setTextSize(40);
        textPaint.setColor(context.getResources().getColor(R.color.colorAccent));
        int xPos = (canvas.getWidth() / 2);
        int yPos = (int) ((canvas.getHeight() / 2) - ((textPaint.descent() + textPaint.ascent()) / 2));
        //((textPaint.descent() + textPaint.ascent()) / 2) is the distance from the baseline to the center.
if(number == -1){
    canvas.drawText("C", xPos, yPos, textPaint);
}else {
    canvas.drawText(number + "", xPos, yPos, textPaint);
}

        return resultBitmap;
    }
}
