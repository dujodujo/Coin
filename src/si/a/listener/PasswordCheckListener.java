package si.a.listener;

import android.text.InputType;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;

public class PasswordCheckListener implements OnCheckedChangeListener {
	
	private EditText passwordView;
	
	public PasswordCheckListener(EditText passwordView) {
		this.passwordView = passwordView;
	}
	
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		this.passwordView.setInputType(InputType.TYPE_CLASS_TEXT
		| (isChecked ? InputType.TYPE_TEXT_VARIATION_PASSWORD : InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD));
	}
}