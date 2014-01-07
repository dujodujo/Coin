package si.a.fragment;

import si.a.coin.WalletActivity;
import si.a.coin.app.R;
import android.app.Activity;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;

public class BitmapFragment extends DialogFragment {
	
	public static final String FRAGMENT_TAG = BitmapFragment.class.getName();
	private static final String KEY_BITMAP = "bitmap";
	private WalletActivity activity;
	
	public static BitmapFragment getInstance(final Bitmap bitmap) {
		BitmapFragment fragment = new BitmapFragment();
		Bundle bundle = new Bundle();
		bundle.putParcelable(KEY_BITMAP, bitmap);
		fragment.setArguments(bundle);
		return fragment;
	}
	
	public static void show(final FragmentManager fragmentManager, Bitmap bitmap) {
		final DialogFragment dialogFragment = getInstance(bitmap);
		dialogFragment.show(fragmentManager, FRAGMENT_TAG);
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
		Bitmap bitmap = (Bitmap) getArguments().getParcelable(KEY_BITMAP);
		
		Dialog dialog = new Dialog(activity);
		dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		dialog.setContentView(R.layout.bitmap_dialog);
		dialog.setCanceledOnTouchOutside(true);
		
		ImageView imageView = (ImageView) dialog.findViewById(R.id.bitmap_dialog_image_view);
		imageView.setImageBitmap(bitmap);
		imageView.setOnClickListener(new View.OnClickListener() {
			
			public void onClick(View v) { dismiss(); }
		});
		
		return super.onCreateDialog(savedInstanceState);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		this.activity = (WalletActivity) activity;
	}
}