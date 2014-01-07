package si.a.parser;

import java.math.BigInteger;

import si.a.util.Constants;

import android.content.Context;
import android.content.DialogInterface.OnClickListener;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.ProtocolException;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.uri.BitcoinURI;
import com.google.bitcoin.uri.BitcoinURIParseException;

public abstract class BinaryInputParser extends InputParser {
	
	protected byte[] input;
	
	public BinaryInputParser(final String inputType, final byte[] input) {
		this.inputType = inputType;
		this.input = input;
	}
	
	@Override
	public void parse() {
		try {
			final Transaction trans = new Transaction(Constants.NETWORK_PARAMETERS, input);
			directTransaction(trans);
		} catch (final ProtocolException x) {}
	}
	
	protected abstract void bitCoinRequest(final Address address, final String addressLabel, 
		final BigInteger amount);
	
	protected void directTransaction(final Transaction transaction) {}
	
	protected void dialog(Context context, OnClickListener endListener, int titleResId, int messageResId, 
		final Object messageArgs) {}
}