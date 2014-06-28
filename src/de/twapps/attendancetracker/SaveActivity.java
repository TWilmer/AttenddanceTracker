package de.twapps.attendancetracker;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.drive.Contents;
import com.google.android.gms.drive.Drive;
import com.google.android.gms.drive.DriveApi;
import com.google.android.gms.drive.DriveApi.DriveIdResult;
import com.google.android.gms.drive.DriveFile;
import com.google.android.gms.drive.DriveFile.DownloadProgressListener;
import com.google.android.gms.drive.DriveFolder.DriveFileResult;
import com.google.android.gms.drive.DriveId;
import com.google.android.gms.drive.Metadata;
import com.google.android.gms.drive.MetadataBuffer;
import com.google.android.gms.drive.MetadataChangeSet;
import com.google.android.gms.drive.DriveApi.ContentsResult;
import com.google.android.gms.drive.query.Filters;
import com.google.android.gms.drive.query.Query;
import com.google.android.gms.drive.query.Query.Builder;
import com.google.android.gms.drive.query.SearchableField;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.Intent;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.os.Build;

public class SaveActivity extends Activity  implements ConnectionCallbacks, OnConnectionFailedListener {

	private static final String TAG = "SaveActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_save);

		mGoogleApiClient = new GoogleApiClient.Builder(this)
		.addApi(Drive.API)
		.addScope(Drive.SCOPE_FILE)
		.addConnectionCallbacks(this)
		.addOnConnectionFailedListener(this)
		.build();



	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
	//	getMenuInflater().inflate(R.menu.save, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
	@Override
	public void onConnected(Bundle arg0) {
		Toast.makeText(this, "Connected", Toast.LENGTH_SHORT).show();


		saveFileToDrive();
	}
	@Override
	public void onConnectionSuspended(int arg0) {
		Toast.makeText(this, "Connection supended", Toast.LENGTH_SHORT).show();



	}

	@Override
	public void onConnectionFailed(ConnectionResult result) {
		// Called whenever the API client fails to connect.
		Log.i(TAG, "GoogleApiClient connection failed: " + result.toString());
		if (!result.hasResolution()) {
			// show the localized error dialog.
			GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
			return;
		}
		// The failure has a resolution. Resolve it.
		// Called typically when the app is not yet authorized, and an
		// authorization
		// dialog is displayed to the user.
		try {
			result.startResolutionForResult(this, REQUEST_CODE_RESOLUTION);
		} catch (SendIntentException e) {
			Log.e(TAG, "Exception while starting resolution activity", e);
		}
	}
	private static final int REQUEST_CODE_RESOLUTION = 3;
	private static final int REQUEST_CODE_CREATOR = 2;

	@Override
	protected void onResume() {
		super.onResume();
		if (mGoogleApiClient == null) {
			// Create the API client and bind it to an instance variable.
			// We use this instance as the callback for connection and connection
			// failures.
			// Since no account name is passed, the user is prompted to choose.
			mGoogleApiClient = new GoogleApiClient.Builder(this)
			.addApi(Drive.API)
			.addScope(Drive.SCOPE_FILE)
			.addConnectionCallbacks(this)
			.addOnConnectionFailedListener(this)
			.build();
		}
		// Connect the client. Once connected, the camera is launched.
		mGoogleApiClient.connect();
	}

	private GoogleApiClient mGoogleApiClient;
	@Override
	protected void onPause() {
		if (mGoogleApiClient != null) {
			mGoogleApiClient.disconnect();
		}
		super.onPause();
	}

	/**
	 * Create a new file and save it to Drive.
	 */

	private void saveFileToDrive() {
		Log.i(TAG, "saveFileToDrive");
	
		Query query = new Query.Builder().addFilter(Filters.eq(SearchableField.TITLE, getFilename())).build();
		Drive.DriveApi.query(mGoogleApiClient, query)
		.setResultCallback(new ResultCallback<DriveApi.MetadataBufferResult>() {

			public void onResult(com.google.android.gms.drive.DriveApi.MetadataBufferResult arg0) {

				if(arg0.getStatus().isSuccess())
				{
					MetadataBuffer b=arg0.getMetadataBuffer();
					int numFile=b.getCount();
					Log.i(TAG, "got files:" +numFile);
					if(numFile>0)
					{
						Metadata m =b.get(0);
						if(m.isEditable())
						{
							
							Log.i(TAG, "is editable "+m.getTitle());
							storeFile(m.getDriveId());
							b.close();
							return;
						}else{
							Log.i(TAG, "is not editable ");
							createFileToDrive();
						}
					}else{
						createFileToDrive();
						b.close();
						return;
					}

					b.close();

				}
				if(arg0.getStatus().hasResolution())
				{
					Log.i(TAG, "Has a resolution ");
					try {
						arg0.getStatus().startResolutionForResult(SaveActivity.this, 1);
					} catch (SendIntentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				
			};

		});
	}

	static String getFilename()
	{
		return AttendActitivty.getMonth()+"-Attendance.csv";
	}



	private void createFileToDrive() {

		// Start by creating a new contents, and setting a callback.
		Log.i(TAG, "Creating new contents.");

		Drive.DriveApi.newContents(mGoogleApiClient).setResultCallback(new ResultCallback<ContentsResult>() {

			@Override
			public void onResult(ContentsResult result) {
				// If the operation was not successful, we cannot do anything
				// and must
				// fail.
				Log.i(TAG, "Has Result to create file ");
				if (!result.getStatus().isSuccess()) {
					Log.i(TAG, "Failed to create new contents.");
					if(result.getStatus().hasResolution())
					{
						Log.i(TAG, "Has a resolution ");
						try {
							result.getStatus().startResolutionForResult(SaveActivity.this, 1);
						} catch (SendIntentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}else{
						Toast.makeText(SaveActivity.this, "Could not create file", Toast.LENGTH_SHORT).show();
						finish();
					}
					return;
				}
				// Otherwise, we can write our data to the new contents.
				Log.i(TAG, "New file created.");




				// Create the initial metadata - MIME type and title.
				// Note that the user will be able to change the title later.
				MetadataChangeSet metadataChangeSet = new MetadataChangeSet.Builder()
				.setMimeType("text/csv").setTitle(getFilename()).build();

				Drive.DriveApi.getRootFolder(mGoogleApiClient)
				.createFile(mGoogleApiClient, metadataChangeSet, result.getContents())
				.setResultCallback(fileCallback);



			}
		});
	}
	final private ResultCallback<DriveFileResult> fileCallback = new
			ResultCallback<DriveFileResult>() {
		@Override
		public void onResult(DriveFileResult result) {
			Log.i(TAG, "Created the file ");
			if (!result.getStatus().isSuccess()) {
				if(result.getStatus().hasResolution())
				{
					Log.i(TAG, "Has a resolution ");
					try {
						result.getStatus().startResolutionForResult(SaveActivity.this, 1);
					} catch (SendIntentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				Toast.makeText(SaveActivity.this, "Could not create file", Toast.LENGTH_SHORT).show();
				return;
			}
			storeFile(result.getDriveFile().getDriveId());
			//      showMessage("Created a file: " +);
		}



	};


	private void storeFile(DriveId driveId) {
		Log.i(TAG, "storeFile the file ");
		// Get an output stream for the contents.
		final DriveFile drv= Drive.DriveApi.getFile(mGoogleApiClient, driveId);
		drv.openContents(mGoogleApiClient, DriveFile.MODE_WRITE_ONLY, null).setResultCallback(new ResultCallback<ContentsResult>() {
			@Override
			public void onResult(ContentsResult result) {
				Log.i(TAG, "storeFile the file  in progress");
				if (!result.getStatus().isSuccess()) {
					Log.i(TAG, "storeFile the file error ");
					if(result.getStatus().hasResolution())
					{
						Log.i(TAG, "Has a resolution ");
						try {
							result.getStatus().startResolutionForResult(SaveActivity.this, 1);
						} catch (SendIntentException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					return;
				}
				Contents contents = result.getContents();

				OutputStream outputStream = contents.getOutputStream();
				Log.i(TAG, "storeFile the file  in ongoing...");
				File f=AttendActitivty.getFile();
				FileInputStream in;
				try {
					in = new FileInputStream(f);
					int r;
					byte buffer[]=new byte[1024*4];
					do{
						r=in.read(buffer);
						if(r>0)
							outputStream.write(buffer, 0, r);
					}while(r>0);
					in.close();
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Log.i(TAG, "storeFile the file  in commmiting...");
				drv.commitAndCloseContents(mGoogleApiClient, contents)
				.setResultCallback(new ResultCallback<Status>() {


					@Override
					public void onResult(Status result) {
						if(result.isSuccess())
						{
							Log.i(TAG, "Finsihed file store ");
							finish();
							return;
						}
						if(result.getStatus().hasResolution())
						{
							Log.i(TAG, "Has a resolution to store file ");
							try {
								result.getStatus().startResolutionForResult(SaveActivity.this, 1);
							} catch (SendIntentException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}

					}
				});

			}
		});

	}


	 @Override
	    protected void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
	     // saveFileToDrive();
	    }


}
