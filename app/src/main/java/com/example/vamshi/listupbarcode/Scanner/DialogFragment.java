package com.example.vamshi.listupbarcode.Scanner;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vamshi.listupbarcode.R;

/**
 * Created by vamshi on 09-12-2016.
 */

public class DialogFragment extends android.app.DialogFragment {
    private String mTitle;
    private String mMessage;
    private MessageDialogListener mListener;

    public static DialogFragment newInstance(String title, String message, MessageDialogListener listener) {
        DialogFragment fragment = new DialogFragment();
        fragment.mTitle = title;
        fragment.mMessage = message;
        fragment.mListener = listener;
        return fragment;
    }

    public void onCreate(Bundle state) {
        super.onCreate(state);
        setRetainInstance(true);
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            mMessage = savedInstanceState.getString("message");
            mTitle = savedInstanceState.getString("title");
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage).setTitle(mTitle);
        setCancelable(false);

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.dialogfragmentlayout, null);
        builder.setView(view);
        final EditText project_name = (EditText) view.findViewById(R.id.project_name);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (mListener != null) {
                    String price = project_name.getText().toString();
                    if (!price.isEmpty()) {
                        if (!price.equals("0")) {
                            mListener.onDialogPositiveClick(DialogFragment.this,mMessage, price);
                        } else {
                            Toast.makeText(getActivity(), "Please scan again and type a valid price", Toast.LENGTH_LONG).show();
                            mListener.onDialogNegativeClick(true);
                        }
                    } else {
                        Toast.makeText(getActivity(), "Please scan again and type a valid price", Toast.LENGTH_LONG).show();
                        mListener.onDialogNegativeClick(true);
                    }
                }
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                mListener.onDialogNegativeClick(true);
            }
        });

        return builder.create();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString("message", mMessage);
        outState.putString("title", mTitle);
        super.onSaveInstanceState(outState);
    }

    public interface MessageDialogListener {
        public void onDialogPositiveClick(android.app.DialogFragment dialog, String bookdetails, String price);

        public void onDialogNegativeClick(boolean value);
    }
}
