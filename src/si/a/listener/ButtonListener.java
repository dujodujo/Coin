package si.a.listener;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.TextView;

public class ButtonListener implements TextWatcher, OnItemSelectedListener {
	
	private TextView passwordView;
	private Button button;
	
	public ButtonListener(final TextView passwordView, final Button button) {
		this.passwordView = passwordView;
		this.button = button;
	}

	public void onItemSelected(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
		handle();
	}

	public void onNothingSelected(AdapterView<?> arg0) {
		handle();
	}

	public void afterTextChanged(Editable arg0) {
		handle();
	}

	public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}

	public void onTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {}
	
	private void handle() {
		final boolean hasPassword = !passwordView.getText().toString().trim().isEmpty();
		button.setEnabled(hasPassword);
	}
}
