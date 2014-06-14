package travel.onroute.balancemanager;

import android.app.DialogFragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import travel.onroute.BalanceManager.R;

/**
 * Created by e.mazurov on 14.06.2014.
 */
public class PasswordDialog extends DialogFragment {

    public interface PassDialogListener {
        void onFinishEditDialog(boolean isPassOk);
    }

    private EditText mEditText;
    public String mPrefPass;

    public PasswordDialog() {
        // Empty constructor required for DialogFragment
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.password_dialog, container);
        mEditText = (EditText) view.findViewById(R.id.txt_pass);
        getDialog().setTitle("Password");

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        mPrefPass = pref.getString("password", "qwerty");

        Button b = (Button) view.findViewById(R.id.button_ok);
        b.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ((PassDialogListener) getActivity()).onFinishEditDialog(mEditText.getText().toString().equals(mPrefPass));
                dismiss();
            }
        });

        return view;
    }
}
