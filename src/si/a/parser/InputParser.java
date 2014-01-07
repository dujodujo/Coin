package si.a.parser;

import java.math.BigInteger;
import java.util.regex.Pattern;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.Base58;
import com.google.bitcoin.core.Transaction;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;

public abstract class InputParser {
	
	protected String inputType;
	
	protected abstract void parse();
	
	protected abstract void directTransaction(final Transaction transaction);
	
	protected abstract void bitCoinRequest(final Address address, final String addressLabel, 
		final BigInteger amount);
	
	protected void dialog(Context context, OnClickListener endListener, int titleResId, int messageResId, 
		final Object messageArgs) {}
}
