package com.gjh.adkiller.ui.settings;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.gjh.adkiller.R;
import com.gjh.adkiller.Settings;
import com.gjh.adkiller.Utilities;

public class ManagePackageWidgetsDialogFragment extends DialogFragment {

    private String TAG = "DialogFragment";

    private EditText editRules;
    private String originalRules;
    private Settings setting;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().setCanceledOnTouchOutside(false);

        View view = inflater.inflate(R.layout.layout_manage_package_widgets, container, false);

        editRules = view.findViewById(R.id.editText_rules);
        setting = Settings.getInstance();
        originalRules = setting.getPackageWidgetsInString();
        editRules.setText(originalRules);

        Button btReset = view.findViewById(R.id.button_reset);
        if(btReset != null) {
            btReset.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    editRules.setText(originalRules);
                }
            });
        }

        Button btCopy = view.findViewById(R.id.button_copy);
        if(btCopy != null) {
            btCopy.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    // Gets a handle to the clipboard service.
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clip = ClipData.newPlainText("Rules: Widget in Packages", editRules.getText().toString());
                    clipboard.setPrimaryClip(clip);

                    Utilities.toast("???????????????????????????!");
                }
            });
        }


        Button btPaste = view.findViewById(R.id.button_paste);
        if(btPaste != null) {
            btPaste.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ClipboardManager clipboard = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                    ClipData clipData = clipboard.getPrimaryClip();
                    if(clipData != null && clipData.getItemCount() > 0) {
                        String pasteData = clipData.getItemAt(0).getText().toString();
                        editRules.setText(pasteData);
                        Utilities.toast("???????????????????????????!");
                    } else {
                        Utilities.toast("???????????????????????????!");
                    }
                }
            });
        }


        Button btCancel = view.findViewById(R.id.button_widgets_cancel);
        if(btCancel != null) {
            btCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utilities.toast("???????????????");
                    getDialog().dismiss();
                }
            });
        }

        Button btRules = view.findViewById(R.id.button_widgets_rules);
        if(btRules != null) {
            btRules.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Utilities.toast("????????????");
                }
            });
        }

        Button btConfirm = view.findViewById(R.id.button_widgets_confirm);
        if(btConfirm != null) {
            btConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    boolean result = setting.setPackageWidgetsInString(editRules.getText().toString());
                    if(result) {
                        Utilities.toast("???????????????!");
                        getDialog().dismiss();
                    } else {
                        Utilities.toast("???????????????????????????????????????!");
                    }
                }
            });
        }


        return view;
    }
}
