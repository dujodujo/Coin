package si.a.parser;

import java.math.BigInteger;

import si.a.coin.app.R;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface.OnClickListener;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.bitcoin.uri.BitcoinURIParseException;

public abstract class StringInputParser extends InputParser {
	
	public StringInputParser(final String input) { 
		this.inputType = input; 
	}

	@Override
	public void parse() {
		if(inputType.startsWith("bitcoin:")) {
			try {
				final BitcoinURI bitcoinUri = new BitcoinURI(null, inputType);
				final Address address = bitcoinUri.getAddress();
				final String addressLabel = bitcoinUri.getLabel();
				final BigInteger amount = bitcoinUri.getAmount();
				bitCoinRequest(address, addressLabel, amount);
				
			} catch(final BitcoinURIParseException exception) {}
		}
	}

	@Override
	protected void bitCoinRequest(final Address address, final String addressLabel, 
		final BigInteger amount) {}

	@Override
	protected void dialog(Context context, OnClickListener endListener, int titleResId, int messageResId, 
			final Object messageArgs) {
		final Builder dialog = new AlertDialog.Builder(context);
		if(titleResId != 0)
			dialog.setTitle(titleResId);
		dialog.setMessage(context.getString(messageResId, messageArgs));
		dialog.setNeutralButton(R.string.button_dismiss, endListener);
		dialog.show();
	}
}