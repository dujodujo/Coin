package si.a.wiew;

import java.math.BigInteger;
import java.util.Locale;

import si.a.util.Constants;
import si.a.util.WalletUtils;

import com.google.bitcoin.core.Utils;

import android.content.Context;
import android.graphics.Paint;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.AttributeSet;
import android.widget.TextView;

public class CurrencyTextView extends TextView {
	
	private String symbol = "";
	private ForegroundColorSpan prefixColorSpan = null;
	private BigInteger amount = null;
	private int precision = Constants.COIN_MAX_PRECISION;
	private RelativeSizeSpan prefixSpan = null;
	private RelativeSizeSpan smallSpan = null;
	private boolean signed = false;
	private boolean flag = true;

	public CurrencyTextView(Context context) {
		super(context);
	}
	
	public CurrencyTextView(Context context, AttributeSet attributes) {
		super(context, attributes);
	}
	
	public void setAmount(BigInteger amount) {
		this.amount = amount;
	}
	
	public void setPrecision(int precision) {
		this.precision = precision;
		updateView();
	}
	
	public void setSigned(boolean signed) {
		this.signed = signed;
		updateView();
	}
	
	public void setSymbol(final String symbol) {
		this.symbol = symbol + Constants.CHAR_HAIR_SPACE;
		updateView();
	}
	
	private void updateView() {
		final Editable text;
		
		if(this.amount != null) {
			String value;
			if(signed) {
				value = WalletUtils.formatValue(amount, Constants.CURRENCY_PLUS_SIGN, Constants.CURRENCY_MINUS_SIGN, precision);
			} else {
				value = WalletUtils.formatValue(amount, "", "-", precision);
			}
			text = new SpannableStringBuilder(value);
			WalletUtils.formatSignificant(text, smallSpan);
			
			if(symbol != null) {
				text.insert(0, symbol);
				text.setSpan(smallSpan, 0, symbol.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
			}
		} else {
			text = null;
		}
		setText(text);
	}
	
	public void setRelativeSpan(float smallRelativeSize) {
		if(smallRelativeSize != 1) {
			this.prefixSpan = new RelativeSizeSpan(smallRelativeSize);
			this.smallSpan = new RelativeSizeSpan(smallRelativeSize);
		} else {
			this.smallSpan = null;
			this.prefixSpan = null;
		}
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		
		setRelativeSpan(0.85f);
		setSingleLine();
	}

	public void setPaintFlag(final boolean flag) {
		if(flag)
			setPaintFlags(getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			setPaintFlags(getPaintFlags() & Paint.STRIKE_THRU_TEXT_FLAG);
	}
}