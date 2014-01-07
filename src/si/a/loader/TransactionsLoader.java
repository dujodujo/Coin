package si.a.loader;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.google.bitcoin.core.Sha256Hash;
import com.google.bitcoin.core.Transaction;
import com.google.bitcoin.core.Wallet;

public class TransactionsLoader extends AsyncTaskLoader<Set<Transaction>> {
	public static String TAG = TransactionsLoader.class.getName();
	
	private Wallet wallet;
	
	public TransactionsLoader(Context context, Wallet wallet) {
		super(context);
		
		this.wallet = wallet;
	}
	
	@Override
	public Set<Transaction> loadInBackground() {
		Set<Transaction> transactions = wallet.getTransactions(false, false);
		Set<Transaction> filteredTransactions = new HashSet<Transaction>(transactions.size());
		
		for(Transaction tx : transactions) {
			Collection<Sha256Hash> transactionInHash = tx.getAppearsInHashes();
			if(transactionInHash != null && !transactionInHash.isEmpty())
				filteredTransactions.add(tx);
		}
		return filteredTransactions;
	}
}