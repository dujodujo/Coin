package si.a.wiew;

import java.math.BigInteger;
import java.util.Currency;
import java.util.jar.Attributes;

import org.bitcoinj.wallet.Protos.Transaction;
import com.google.bitcoin.core.Utils;

import si.a.util.Constants;
import si.a.util.WalletUtils;
import si.a.coin.app.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.InputType;
import android.text.SpannableStringBuilder;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

public class CurrencyAmountView extends FrameLayout {
	
	public static final String TAG = CurrencyAmountView.class.getName();
	
	private Drawable deleteButtonDrawable;
	private Drawable contextButtonDrawable;
	private Drawable currencySymbolDrawable;
	
	private TextView textView;
	private Button contextButton;
	
	public TextViewListener textViewListener;
	public CurrencyListener currencyListener;
	
	private int hintPrecision = Constants.COIN_MAX_PRECISION;
	private int inputPrecision = Constants.COIN_MAX_PRECISION;
	
	private boolean validAmount = true;
	private boolean signedAmount = true;
	
	public CurrencyAmountView(Context context) {
		this(context, null);
	}

	public CurrencyAmountView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}
	
	public CurrencyAmountView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		
		Resources resources = context.getResources();
		
		textViewListener = new TextViewListener();
		
		LayoutInflater.from(context).inflate(R.layout.currency_amount_content, this);
		textView = (TextView) findViewById(R.id.send_coins_fragment_receiving_label);

		textView.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
		textView.setHintTextColor(resources.getColor(R.color.bg_less));
		textView.setSingleLine();
		
		textView.addTextChangedListener(textViewListener);
		textView.setOnFocusChangeListener(textViewListener);
		textView.setOnEditorActionListener(textViewListener);
		
		contextButton = (Button) findViewById(R.id.context_button);
	}
	
	private void update() {
		Log.i(TAG, "update");
		
		boolean enabled = textView.isEnabled();
		contextButton.setEnabled(enabled);
		
		final String amount = textView.getText().toString().trim();
		
		if(enabled && !amount.isEmpty()) {
			textView.setCompoundDrawablesWithIntrinsicBounds(currencySymbolDrawable, null, deleteButtonDrawable, null);
			contextButton.setOnClickListener(deleteClickListener);
		} else if(enabled && contextButtonDrawable != null) {
			textView.setCompoundDrawablesWithIntrinsicBounds(currencySymbolDrawable, null, contextButtonDrawable, null);
			contextButton.setOnClickListener(contextClickListener);
		} else {
			Log.i(TAG, "blah");
			textView.setCompoundDrawablesWithIntrinsicBounds(currencySymbolDrawable, null, null, null);
			contextButton.setOnClickListener(null);
		}
	}
	
	public void setCurrencySymbol(final String currencyCode) {
		Log.i(TAG, "setCurrencySymbol");
		
		if(Constants.CURRENCY_CODE_COIN.equals(currencyCode)) {
			currencySymbolDrawable = getResources().getDrawable(R.drawable.currency_symbol);
		} else if (currencyCode != null) {
			String currencySymbol = getCurrencySymbol(currencyCode);
			float textSize = textView.getTextSize();
			currencySymbolDrawable = new CurrencySymbolView(currencySymbol, textSize, R.color.bg_less, textSize * 0.5f);
		} else {
			currencySymbolDrawable = null;
		}
		update();
	}
	
	private String getCurrencySymbol(final String currencyCode) {
		Log.i(TAG, "getCurrencySymbol");

		try {
			final Currency currency = Currency.getInstance(currencyCode);
			return currency.getSymbol();
		} catch (final IllegalArgumentException exception){
			return currencyCode;
		}
	}
	
	public static interface CurrencyListener {
		public void changed();
		public void done();
		public void focusChanged(final boolean hasFocus);
	};
	
	public void setListener(CurrencyListener listener) {
		this.currencyListener = listener;
	}
	
 	private final class TextViewListener implements TextWatcher, OnFocusChangeListener, OnEditorActionListener {
		
		private boolean fire = true;
		
		public void setFire(final boolean fire) { this.fire = fire; }
		
		public void onFocusChange(View v, boolean hasFocus) {}

		public void afterTextChanged(Editable editable) {
			Log.i(TAG, "afterTextChanged");
			
			final String original = editable.toString();
			final String replaced = original.replace(',', '.');
			if(!replaced.equals(original)) {
				editable.clear();
				editable.append(replaced);
			}
			WalletUtils.formatSignificant(editable, WalletUtils.SMALLER_SPAN);
		}

		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		public void onTextChanged(CharSequence s, int start, int before, int count) {
			Log.i(TAG, "onTextChanged");

			update();
			if(currencyListener != null && fire)
				currencyListener.changed();
		}

		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) { return false; }
	};
	
	private final OnClickListener deleteClickListener = new OnClickListener() {
		
		public void onClick(View v) {
			Log.i(TAG, "deleteClickListener");
			
			textView.requestFocus();
		}
	};
	
	private final OnClickListener contextClickListener = new OnClickListener() {
		public void onClick(View v) {
			Log.i(TAG, "contextClickListener");
			
			BigInteger amount = new BigInteger("100");
			setCoinAmount(amount);
			textView.requestFocus();
		}
	};

	public BigInteger getCoinAmount() {
		Log.i(TAG, "getCoinAmount");
		//if(isValidAmount(true))
		return Utils.toNanoCoins(textView.getText().toString().trim());
		//else
		//	return null;
	}
	
	public boolean isValidAmount(final boolean valid) {
		Log.i(TAG, "isValidAmount");
		
		String amount = textView.getText().toString().trim();
		try {
			if(!amount.isEmpty()) {
				BigInteger nanoCoins = Utils.toNanoCoins(amount);
				if(valid && nanoCoins.signum() == 0)
					return true;
				return true;
			}
		} catch (Exception exception) {}
		return false;
	}
	
	public void setHint(final BigInteger amount) {
		Log.i(TAG, "setHint");
		
		final SpannableStringBuilder hint;
		if(amount != null)
			hint = new SpannableStringBuilder(WalletUtils.formatValue(amount, hintPrecision));
		else 
 			hint = new SpannableStringBuilder("0.00");
		WalletUtils.formatSignificant(hint, WalletUtils.SMALLER_SPAN);
		textView.setHint(hint);
	}
	
	public void setCoinAmount(BigInteger amount) {
		Log.i(TAG, "setCoinAmount");
		
		String signed = WalletUtils.formatValue(amount, inputPrecision);
		String unsigned = WalletUtils.formatValue(amount, Constants.CURRENCY_PLUS_SIGN, 
			Constants.CURRENCY_MINUS_SIGN, inputPrecision);
		if(amount != null)
			textView.setText(signedAmount ? signed : unsigned);
		else
			textView.setText(null);
	}
	
	@Override
	protected Parcelable onSaveInstanceState() {
		Bundle bundle = new Bundle();
		bundle.putParcelable("super_state", super.onSaveInstanceState());
		bundle.putParcelable("child_textView", textView.onSaveInstanceState());
		bundle.putSerializable("amount", getCoinAmount());
		return bundle;
	}
	
	@Override
	protected void onRestoreInstanceState(Parcelable state) {
		if(state != null) {
			Bundle bundle = (Bundle)state;
			super.onRestoreInstanceState(state);
			textView.onRestoreInstanceState(state);
			setCoinAmount((BigInteger) bundle.getSerializable("amount"));
		} else {
			super.onRestoreInstanceState(state);
		}
	}
	
	public void setSignedAmount(boolean signedAmount) {
		this.signedAmount = signedAmount;
	}
		
	public void setValidateAmount(final boolean validateAmount) {
		this.validAmount = validateAmount;
	}
}