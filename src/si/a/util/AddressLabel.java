package si.a.util;

import com.google.bitcoin.core.Address;
import com.google.bitcoin.core.AddressFormatException;
import com.google.bitcoin.core.NetworkParameters;
import com.google.bitcoin.core.WrongNetworkException;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class AddressLabel implements Parcelable {
	public static final String TAG = AddressLabel.class.getName();
	
	public Address address;
	public String label;
	
	public AddressLabel(NetworkParameters addressParameters, String address, String label) 
			throws WrongNetworkException, AddressFormatException {
		Log.i(TAG, "AddressLabel");
		
		this.address = new Address(addressParameters, address);
		this.label = label;
	}
	
	public AddressLabel(final Parcel source) {
		Log.i(TAG, "AddressLabel Source");
		
		NetworkParameters addressParameters = (NetworkParameters) source.readSerializable();
		final byte[] addressHash = new byte[Address.LENGTH];
		source.readByteArray(addressHash);
		
		address = new Address(addressParameters, addressHash);
		label = source.readString();
	}

	public int describeContents() {
		return 0;
	}

	public void writeToParcel(Parcel dest, int flags) {
		dest.writeSerializable(address.getParameters());
		dest.writeByteArray(address.getHash160());
		dest.writeString(label);
	}
	
	public static final Parcelable.Creator<AddressLabel> CREATOR = new Parcelable.Creator<AddressLabel>() {

		public AddressLabel createFromParcel(final Parcel source) {
			return new AddressLabel(source);
		}

		public AddressLabel[] newArray(int size) {
			return new AddressLabel[size];
		}
	};
}
