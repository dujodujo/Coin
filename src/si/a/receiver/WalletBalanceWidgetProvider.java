package si.a.receiver;

import java.math.BigInteger;

import com.google.bitcoin.core.Wallet;
import com.google.bitcoin.core.Wallet.BalanceType;

import si.a.application.WalletApplication;
import si.a.coin.NetworkActivity;
import si.a.coin.SendCoinsActivity;
import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import si.a.util.Constants;
import si.a.util.WalletUtils;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.util.Log;
import android.widget.RemoteViews;

public class WalletBalanceWidgetProvider extends AppWidgetProvider {
	public static String TAG = WalletBalanceWidgetProvider.class.getName();

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);
		Log.i(TAG, "onUpdate");
		
		WalletApplication walletApplication = (WalletApplication) context.getApplicationContext();
		Wallet wallet = walletApplication.getWallet();
		BigInteger balance = wallet.getBalance(BalanceType.ESTIMATED);
		
		updateWidgets(context, appWidgetManager, appWidgetIds, balance);
	}
	
	public static void updateWidgets(Context context, AppWidgetManager appWidgetManager, int[] appWidgetsIDS, BigInteger balance) {
		Log.i(TAG, "update Widgets");
		
		SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
		int precision = Integer.parseInt(preferences.getString(Constants.PREFS_KEY_COIN_PRECISION, 
			Constants.PREFS_KEY_DEFAULT_COIN_PRECISION));
		Editable balanceBuilder = new SpannableStringBuilder(WalletUtils.formatValue(balance, precision));
		WalletUtils.formatSignificant(balanceBuilder, WalletUtils.SMALLER_SPAN);
		
		for(int i = 0; i < appWidgetsIDS.length; i++) {
			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.wallet_balance_widget_content);
			views.setTextViewText(R.id.widget_wallet_balance, balanceBuilder);

			views.setOnClickPendingIntent(R.id.widget_button_balance, PendingIntent.getActivity(context, 0, new Intent(context, WalletActivity.class), 0));
			views.setOnClickPendingIntent(R.id.widget_button_request, PendingIntent.getActivity(context, 0, new Intent(context, NetworkActivity.class), 0));
			views.setOnClickPendingIntent(R.id.widget_button_send, PendingIntent.getActivity(context, 0, new Intent(context, SendCoinsActivity.class), 0));
			
			appWidgetManager.updateAppWidget(appWidgetsIDS[i], views);
		}
	}
}