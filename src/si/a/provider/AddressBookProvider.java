package si.a.provider;

import java.util.List;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public class AddressBookProvider extends ContentProvider {
	
	private static final String DATABASE_TABLE = "adress_book";
	
	public static final String KEY_ROWID = "_id";
	public static final String KEY_ADDRESS = "address";
	public static final String KEY_LABEL = "label";
	
	public static final String SELECTION_QUERY = "q";
	public static final String SELECTION_IN = "in";
	public static final String SELECTION_NOTIN = "notin";
	
	private Helper helper;
	
	private static class Helper extends SQLiteOpenHelper {

		private static final String DATABASE_NAME = "address_book";
		private static final int DATABASE_VERSION = 1;
		
		private static final String DATABASE_CREATE = "CREATE TABLE " + DATABASE_NAME + " ("
				+ KEY_ROWID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
				+ KEY_ADDRESS + " TEXT NOT NULL, "
				+ KEY_LABEL + " TEXT NULL";
				
		public Helper(Context context) {
			super(context, DATABASE_NAME, null, DATABASE_VERSION);
		}

		@Override
		public void onCreate(SQLiteDatabase db) {
			db.execSQL(DATABASE_CREATE);
		}

		@Override
		public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
			db.beginTransaction();
			try {
				for(int i = oldVersion; i < newVersion; i++)
					upgrade(db, i);
				db.setTransactionSuccessful();
			} finally {
				db.endTransaction();
			}
		}
		
		private static void upgrade(SQLiteDatabase db, int oldVersion) {}
	}
	
	public static Uri contentUri(final String packageName) {
		return Uri.parse("content://" + packageName + "." + DATABASE_TABLE);
	}
	
	public static String resolveLabel(final Context context, final String address) {
		String label = null;
		final Uri uri = contentUri(context.getPackageName()).buildUpon().appendPath(address).build();
		final Cursor cursor = context.getContentResolver().query(uri, null, null, null, null);
		if(cursor != null) {
			if(cursor.moveToFirst()) {
				label = cursor.getString(cursor.getColumnIndexOrThrow(AddressBookProvider.KEY_LABEL));
				cursor.close();
			}
		}
		return label;
	}
	
	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		final List<String> pathSegments = uri.getPathSegments();
		
		final String address = uri.getLastPathSegment();
		final int count = helper.getWritableDatabase().delete(DATABASE_TABLE, 
			KEY_ADDRESS + "= ?", new String[] {address});
		
		if(count > 0)
			getContext().getContentResolver().notifyChange(uri, null);
		
		return count;
	}

	@Override
	public String getType(Uri uri) { throw new UnsupportedOperationException(); }

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		final String address = uri.getLastPathSegment();
		values.put(KEY_ADDRESS, address);
		long rowId = helper.getWritableDatabase().insertOrThrow(DATABASE_TABLE, null, values);
		final Uri rowUri = 
			contentUri(getContext().getPackageName()).buildUpon().appendPath(address).appendPath(Long.toString(rowId)).build();
		getContext().getContentResolver().notifyChange(rowUri, null);
		return rowUri;
	}

	@Override
	public boolean onCreate() {
		helper = new Helper(getContext());
		return true;
	}
	
	@Override
	public Cursor query(Uri uri, String[] projection, String originalSelection, 
			String[] originalSelectionArgs, String sortOrder) {
		
		SQLiteQueryBuilder builder = new SQLiteQueryBuilder();
		builder.setTables(DATABASE_TABLE);
		
		final List<String> pathSegments = uri.getPathSegments();
		String selection = null;
		String[] selectionArgs = null;
		
		if(pathSegments.size() == 1) {
			final String address = uri.getLastPathSegment();
			builder.appendWhere(KEY_ADDRESS + "=");
			builder.appendWhereEscapeString(address);
		} else if(SELECTION_QUERY.equals(originalSelection)) {
			final String q = "%" + originalSelectionArgs[0].trim() + "%";
			selection = KEY_ADDRESS + " LIKE ? OR " + KEY_LABEL + " LIKE ?";
			selectionArgs = new String[] {q, q};
		} else if(SELECTION_IN.equals(originalSelection)) {
			final String[] addresses = originalSelectionArgs[0].trim().split(",");
			builder.appendWhere(KEY_ADDRESS + " IN (");
			appendAddresses(builder, addresses);
			builder.appendWhere(")");
		} else if(SELECTION_NOTIN.equals(originalSelection)) {
			final String[] addresses = originalSelectionArgs[0].trim().split(",");
			builder.appendWhere(KEY_ADDRESS + " NOT IN (");
			appendAddresses(builder, addresses);
			builder.appendWhere(")");
		}
		
		final Cursor cursor = builder.query(helper.getReadableDatabase(), projection, selection, selectionArgs, 
			null, null, sortOrder);
		cursor.setNotificationUri(getContext().getContentResolver(), uri);
		return cursor;
	}
	
	private static void appendAddresses(SQLiteQueryBuilder builder, String[] addresses) {
		for(String address : addresses) {
			builder.appendWhereEscapeString(address.trim());
			if(!address.equals(addresses[addresses.length-1])) {
				builder.appendWhere(",");
			}
		}
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
		final String address = uri.getLastPathSegment();
		final int count = helper.getWritableDatabase().update(DATABASE_TABLE, values, 
			KEY_ADDRESS +  "=?", new String[] {address});
		return count;
	}
}