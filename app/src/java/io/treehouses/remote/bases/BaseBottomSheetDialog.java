package io.treehouses.remote.bases;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import io.treehouses.remote.R;
import io.treehouses.remote.callback.HomeInteractListener;

public class BaseBottomSheetDialog extends BottomSheetDialogFragment {
    protected Context context;
    protected HomeInteractListener listener;

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        setStyle(DialogFragment.STYLE_NORMAL, R.style.DialogStyle);
        return super.onCreateDialog(savedInstanceState);
    }

    @Override
    public void onAttach(@NonNull Context c) {
        super.onAttach(c);
        try {
            listener = (HomeInteractListener) c;
            context = c;
        } catch (ClassCastException e) {
            throw new ClassCastException(c.toString() + " must implement HomeInteractListener");
        }
    }


}
