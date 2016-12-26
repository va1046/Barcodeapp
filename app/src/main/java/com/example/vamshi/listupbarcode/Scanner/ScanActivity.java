package com.example.vamshi.listupbarcode.Scanner;


import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.vamshi.listupbarcode.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import me.dm7.barcodescanner.zbar.Result;
import me.dm7.barcodescanner.zbar.ZBarScannerView;

/**
 * Created by vamshi on 09-12-2016.
 */

public class ScanActivity extends AppCompatActivity implements ZBarScannerView.ResultHandler, DialogFragment.MessageDialogListener {
    public static final String TAG = ScanActivity.class.getSimpleName();
    static String resultscontent;
    RequestQueue requestQueue;
    Context mContext;
    private ZBarScannerView mScannerView;

    @Override
    public void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.simplescannerlayout);
        mScannerView = new ZBarScannerView(this);    // Programmatically initialize the scanner view
        setContentView(mScannerView);                // Set the scanner view as the content view
        mContext = this;
        requestQueue = Volley.newRequestQueue(mContext);
    }

    @Override
    public void onResume() {
        super.onResume();
        mScannerView.setResultHandler(this); // Register ourselves as a handler for scan results.
        mScannerView.startCamera();          // Start camera on resume
    }

    @Override
    public void onPause() {
        super.onPause();
        mScannerView.stopCamera();           // Stop camera on pause
    }

    @Override
    public void handleResult(Result rawResult) {
        // Do something with the result here
        Log.v(TAG, rawResult.getContents()); // Prints scan results
        Log.v(TAG, rawResult.getBarcodeFormat().getName()); // Prints the scan format (qrcode, pdf417 etc.)
        onPause();
        String isbn = rawResult.getContents().replaceAll("[a-zA-Z]", "");
        Log.d(TAG, "handleResult: " + isbn);
        String url = "https://www.googleapis.com/books/v1/volumes?q=isbn:" + isbn;
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray jsonArray1 = response.getJSONArray("items");
                    JSONObject jsonObject1 = jsonArray1.getJSONObject(0);
                    JSONObject jsonObject2 = jsonObject1.getJSONObject("volumeInfo");
                    String Title = jsonObject2.getString("title");
                    String Published_On = jsonObject2.getString("publishedDate");
                    JSONArray jsonArray2 = jsonObject2.getJSONArray("authors");
                    String authors = "";
                    int length = jsonArray2.length();
                    String[] recipients;
                    boolean hasSingleAuthor = true;
                    if (length > 1) {
                        recipients = new String[length];
                        hasSingleAuthor = false;
                        for (int i = 0; i < length; i++) {
                            recipients[i] = jsonArray2.getString(i);
                            authors = authors + ", " + recipients[i];
                        }
                    } else {
                        hasSingleAuthor = true;
                        authors = jsonArray2.getString(0);
                    }

                    if (hasSingleAuthor) {
                        resultscontent = "Title : " + Title + ",\nAuthor : " + authors + ",\nPublished Date : " + Published_On + ".";
                    } else {
                        resultscontent = "Title : " + Title + ",\nAuthors : " + authors + ",\nPublished Date : " + Published_On + ".";
                    }
                    showMessageDialog(resultscontent);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                onResume();
            }
        });

        requestQueue.add(jsonObjectRequest);
    }

    public void showMessageDialog(String message) {
        android.app.DialogFragment fragment = DialogFragment.newInstance("Scan Results", message, this);
        fragment.show(getFragmentManager(), "Book Details");
    }

    @Override
    public void onDialogPositiveClick(android.app.DialogFragment dialog, String bookdetails, String price) {

        JSONObject jsonObject = new JSONObject();
        try {
            String temp = bookdetails.replaceAll("\n", "");
            jsonObject.put("Book Details", temp.replaceAll(",", ", "));
            jsonObject.put("Price", price);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        final String mRequestBody = jsonObject.toString();

        String url = "http://requestb.in/1i55by81";
        final StringRequest requestbinrequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.d(TAG, "onResponse: " + response);
                closeMessageDialog(true);
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                closeMessageDialog(true);
                Log.d(TAG, "onResponse: " + error);
                Toast.makeText(mContext, "Please scan again, we couldn't collect your details", Toast.LENGTH_LONG).show();
            }
        }) {
            @Override
            public String getBodyContentType() {
                return "application/json; charset=utf-8";
            }

            @Override
            public byte[] getBody() throws AuthFailureError {
                try {
                    return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                } catch (UnsupportedEncodingException uee) {
                    VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                    return null;
                }
            }

            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                String responseString = "";
                if (response != null) {
                    responseString = String.valueOf(response.statusCode);
                    // can get more details such as response.headers
                }
                return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
            }
        };

        requestQueue.add(requestbinrequest);
    }


    @Override
    public void onDialogNegativeClick(boolean value) {
        closeMessageDialog(value);
    }

    public void closeMessageDialog(boolean value) {
        closeDialog("scan_results", value);
    }

    public void closeDialog(String dialogName, boolean value) {
        android.app.FragmentManager fragmentManager = getFragmentManager();
        android.app.DialogFragment fragment = (android.app.DialogFragment) fragmentManager.findFragmentByTag(dialogName);
        if (fragment != null) {
            fragment.dismiss();
        }
        if (value) {
            onResume();
        }
    }

}
