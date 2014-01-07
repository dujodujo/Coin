package si.a.fragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.net.InetAddress;

import com.google.bitcoin.core.Peer;
import com.google.bitcoin.core.VersionMessage;

import si.a.coin.AbstractWalletActivity;
import si.a.coin.app.R;
import si.a.service.BlockchainService;
import si.a.service.BlockchainServiceImplementation;

import android.app.Activity;
import android.content.AsyncTaskLoader;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class PeerListFragment extends ListFragment {
	
	private AbstractWalletActivity activity;
	private Context context;
	private LoaderManager loaderManager;
	private BlockchainService service;
	private ArrayAdapter<Peer> adapter;
	
	private Handler handler = new Handler();
	
	private static final int ID_PEER_LOADER = 0;
	private static final int ID_DNS_LOADER = 1;
	
	private final Map<InetAddress, String> hostnames = new HashMap<InetAddress, String>();

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
		this.activity = (AbstractWalletActivity) activity;
		this.loaderManager = getLoaderManager();
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		setEmptyText(getString(R.string.peer_list_fragment_empty));
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		adapter = new ArrayAdapter<Peer>(activity, 0) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				super.getView(position, convertView, parent);
				if(convertView == null) {
					LayoutInflater inflater = LayoutInflater.from(context);
					convertView = inflater.inflate(R.layout.peer_list_row, null);
				}
				
				final Peer peer = getItem(position);
				VersionMessage versionMessage = peer.getPeerVersionMessage();
				boolean isDownloading = peer.getDownloadData();
				
				TextView rowIP = (TextView) convertView.findViewById(R.id.peer_list_row_ip);
				InetAddress address = peer.getAddress().getAddr();
				String hostName = hostnames.get(address);
				rowIP.setText(hostName != null ? hostName : address.getHostAddress());
				
				TextView rowHeight = (TextView) convertView.findViewById(R.id.peer_list_row_height);
				long height = peer.getBestHeight();
				rowHeight.setText(height > 0 ? height + " blocks" : null);
				rowHeight.setTypeface(isDownloading ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
				
				TextView rowVersion = (TextView) convertView.findViewById(R.id.peer_list_row_version);
				rowVersion.setText(versionMessage.subVer);
				rowVersion.setTypeface(isDownloading ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
				
				TextView rowPing = (TextView) convertView.findViewById(R.id.peer_list_row_ping);
				long pingTime = peer.getPingTime();
				rowPing.setText(pingTime < Long.MAX_VALUE ? getString(R.string.peer_list_row_ping_time) : null);
				rowPing.setTypeface(isDownloading ? Typeface.DEFAULT_BOLD : Typeface.DEFAULT);
				
				return convertView;
			}
		};
		setListAdapter(adapter);
	}

	/*
	@Override
	public void onResume() {
		super.onResume();
		
		handler.post(new Runnable() {

			@Override
			public void run() {
				adapter.notifyDataSetChanged();
				
				Loader<Object> loader = loaderManager.getLoader(ID_DNS_LOADER);
				boolean loaderRunning = loader != null && loader.isStarted();
				
				if(!loaderRunning) {
					for(int i = 0; i < adapter.getCount(); i++) {
						Peer peer = adapter.getItem(i);
						InetAddress address = peer.getAddress().getAddr();
						
						if(!hostnames.containsKey(address)) {
							Bundle bundle = new Bundle();
							bundle.putSerializable("address", address);
							loaderManager.initLoader(ID_DNS_LOADER, bundle, reverseDNSLoaderCallbacks).forceLoad();
							break;
						}
					}
				}
			}
		});
	}

	private static class PeerLoader extends AsyncTaskLoader<List<Peer>> {
		private Context context;
		private BlockchainService service;

		public PeerLoader(Context context, BlockchainService service) {
			super(context);
			this.context = context.getApplicationContext();
			this.service = service;
		}

		@Override
		public List<Peer> loadInBackground() {
			return service.getConnectedPeers();
		}

		@Override
		protected void onStartLoading() {
			super.onStartLoading();
			//context.registerReceiver(receiver, filter);
		}

		@Override
		protected void onStopLoading() {
			super.onStopLoading();
			//context.unregisterReceiver(receiver);
		}
		
		private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {

			@Override
			public void onReceive(Context context, Intent intent) { forceLoad(); }
		};
	}

	private final LoaderCallbacks<List<Peer>> peerLoaderCallbacks = new LoaderCallbacks<List<Peer>>() {
		
		@Override
		public Loader<List<Peer>> onCreateLoader(int id, Bundle args) {
			return new PeerLoader(activity, service);
		}

		@Override
		public void onLoadFinished(Loader<List<Peer>> loader, List<Peer> peers) {
			adapter.clear();
			if(peers != null) {
				for(Peer peer : peers) {
					adapter.add(peer);
				}
			}
		}

		@Override
		public void onLoaderReset(Loader<List<Peer>> loader) {
			adapter.clear();
		}
	};
	
	private final LoaderCallbacks<String> reverseDNSLoaderCallbacks = new LoaderCallbacks<String>() {

		@Override
		public Loader<String> onCreateLoader(int id, Bundle bundle) {
			InetAddress address = (InetAddress) bundle.getSerializable("address");
			return new ReverseDNSLoader(context, address);
		}

		@Override
		public void onLoadFinished(Loader<String> loader, String hostname) {
			InetAddress address = ((ReverseDNSLoader) loader).address;
			hostnames.put(address, hostname);
			loaderManager.destroyLoader(ID_DNS_LOADER);
		}

		@Override
		public void onLoaderReset(Loader<String> loader) {}
	};
	
	private static class ReverseDNSLoader extends AsyncTaskLoader<String> {
		public static String TAG = ReverseDNSLoader.class.getName();
		public InetAddress address;

		public ReverseDNSLoader(Context context, InetAddress address) {
			super(context);
			this.address = address;
		}

		@Override
		public String loadInBackground() { return null; }
	}
	*/
}