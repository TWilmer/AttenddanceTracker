package de.twapps.attendancetracker;

import android.app.Activity;
import android.app.ActionBar;
import android.app.Fragment;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.os.Build;

public class SettingsActivity extends Activity {

	EditText nameEdit;
	EditText numberEdit;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_settings);

		Button b=(Button)findViewById(R.id.buttonSave);
		nameEdit=(EditText)findViewById(R.id.editName);
		numberEdit=(EditText)findViewById(R.id.editNumber);
		final SharedPreferences p=this.getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
		nameEdit.setText(p.getString("name", "Your Name"));
		numberEdit.setText(p.getString("number", "1234567"));
		b.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Editor e=p.edit();
				e.putString("name", nameEdit.getText().toString());
				e.putString("number", numberEdit.getText().toString());
				e.commit();
				finish();
			}
		});
				
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.settings, menu);
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

	
	

}
