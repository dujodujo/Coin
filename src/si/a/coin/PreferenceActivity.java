package si.a.coin;

import si.a.application.WalletApplication;
import si.a.util.Constants;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;

public class PreferenceActivity {
	
	private WalletApplication walletApplication;
	
	public static final String PREFS_KEY_REPORT_ISSUE = "report";
	public static final String PREFS_KEY_START_RESET = "reset";
	public static final String PREFS_KEY_DATA_USAGE = "data usage";
}
