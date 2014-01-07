package si.a.util;

import java.nio.charset.Charset;
import android.text.format.DateUtils;

import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.Utils;

public class Constants {
	
	public static final NetworkParameters NETWORK_PARAMETERS = NetworkParameters.testNet();
	
	public static final int MAX_NUM_CONFIRMATIONS = 6;
	
	public static final String PREFS_KEY_LAST_VERSION = "last_version";
    public static final String PREFS_KEY_LAST_USED = "last_used";
    public static final String VERSION_URL = "http://wallet.schildbach.de/version";
    
    public static final int HTTP_TIMEOUT_MS = 30 * (int) DateUtils.SECOND_IN_MILLIS;
    
    public static final String MARKET_APP_URL = "market://details?id=%s";
    public static final String BINARY_APP_URL = "http://code.google.com/p/bitcoin-wallet/downloads/list";
    
    public static final String DONATION_ADDRESS = "donation address";
    
    public static final Charset UTF_8 = Charset.forName("UTF-8");
    public static final Charset ASCII = Charset.forName("ASCII");
    
	public static final char CHAR_HAIR_SPACE = '\u200a';
	public static final char CHAR_THIN_SPACE = '\u2009';
    
    public static final String PREFS_KEY_CONNECTIVITY_NOTIFICATION = "connectivity_notification";
    public static final String PREFS_KEY_SELECTED_ADDRESS = "connectivity_notification";
    public static final String PREFS_KEY_EXCHANGE_CURRENCY = "exchange_currency";
    public static final String PREFS_KEY_TRUSTED_PEER = "trusted peer";
    public static final String PREFS_KEY_TRUSTED_PEER_ONLY = "trusted peer only";
    public static final String PREFS_KEY_BEST_CHAIN_HEIGHT_EVER = "best_chain_height_ever";
    public static final String PREFS_KEY_COIN_PRECISION = "btc_precision";
    public static final String PREFS_KEY_DEFAULT_COIN_PRECISION = "4";
    public static final String PREFS_KEY_BACKUP = "backup";
    public static final String PREFS_KEY_DISCLAIMER = "disclaimer";
    
    public static final int ADDRESS_FORMAT_GROUP_SIZE = 4;
    public static final int ADDRESS_FORMAT_LINE_SIZE = 12;

    public static final String WALLET_KEY_BACKUP = "key-backup-base58";
    public static final String WALLET_FILENAME = "wallet-testnet";
    
    public static final int COIN_MAX_PRECISION = 3;
    public static final int LOCAL_PRECISION = 2;
    
    public static final String CURRENCY_PLUS_SIGN = "+";
    public static final String CURRENCY_MINUS_SIGN = "-";
    public static final String CURRENCY_CODE_COIN = "coin";
    
    public static final String EXCHANGE_RATE = "DE";
    
    public static final String BLOCK_EXPLORER_BASE_URL = "https://blockexplorer.com/";
    public static final String BLOCK_EXPLORER_BASE_URL_TESTNET = "https://blockexplorer.com/testnet/";
    
    public static final String BLOCKCHAIN_FILENAME = "blockchain" + NETWORK_PARAMETERS.ID_TESTNET;
    public static final String CHECKPOINTS_FILENAME = "checkpoints" + NETWORK_PARAMETERS.ID_TESTNET;
}