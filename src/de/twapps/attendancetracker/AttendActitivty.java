package de.twapps.attendancetracker;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;









import android.app.Activity;
import android.app.ActionBar;
import android.app.AlarmManager;
import android.app.Fragment;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.IntentSender.SendIntentException;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Build;
class Entry
{
	String day;
	String coming;
	String leaving;
	String breakstart;
	String breakend;
	String comment;
	public String toLine() {
		if(comment!=null)
		{
			comment=comment.replace(',',' ');
			comment=comment.replace('\"', ' ');
		}
		String ret="";
		if(day!=null)
			ret+=day;
		ret+=",";

		if(coming!=null)
			ret+=coming;
		ret+=",";

		if(leaving!=null)
			ret+=leaving;
		ret+=",";

		if(breakstart!=null)
			ret+=breakstart;

		ret+=",";

		if(breakend!=null)
			ret+=breakend;
		ret+=",";

		if(comment!=null)
			ret+=comment;
		ret+=",";


		ret+="\n";
		// TODO Auto-generated method stub
		return ret;
	}
}
public class AttendActitivty extends Activity  {

	enum eAction {
		ATTEND,
		PAUSE
	}


	private static final String TAG = "AttendActivty";
	static Entry getCurrentDay(){
		Calendar c = Calendar.getInstance();
		Entry entry=new Entry();
		int dom=c.get(Calendar.DAY_OF_MONTH);
		try {
			File f = getFile();
			BufferedReader r=new BufferedReader(new FileReader(f));
			String l=null;
			do{ 
				l=r.readLine();
				if(l!=null)
				{
					if(Character.isDigit( l.getBytes()[0]) )
					{
						String items[]=l.split(",");
						int d=Integer.parseInt(items[0]);
						entry=new Entry();
						if(1<items.length)
							entry.coming=items[1];
						if(2<items.length)
							entry.leaving=items[2];
						if(3<items.length)
							entry.breakstart=items[3];
						if(4<items.length)
							entry.breakend=items[4];
						if(5<items.length)
							entry.comment=items[5];
						if(d==dom)
							return entry;
					}
				}
			}while(l!=null);
			r.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return entry;
	}
	static String getMonth()
	{
		Calendar cal=Calendar.getInstance();
		SimpleDateFormat month_date = new SimpleDateFormat("MMMMMMMMM");
		String month_name = month_date.format(cal.getTime());
		return month_name;
	}
	static File getFile()
	{
		String baseDir = Environment.getExternalStorageDirectory().getAbsolutePath();
		Calendar c = Calendar.getInstance(); 
		String filename=getMonth()+".attendance.csv";
		String path  = baseDir + "/" + filename;
		File f=new File(path);
		return f;
	}
	void stopAlarm()
	{
		final SharedPreferences p=this.getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
		Intent intent = new Intent(this, AlarmActivity.class);
		PendingIntent pendingIntent = PendingIntent.getActivity(AttendActitivty.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
		AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
		alarmManager.cancel(pendingIntent);
		Editor  e=p.edit();
		e.putLong("alarm", 0);
		e.commit();
	}
	 void appendAttendance(eAction action, String editable, String name, String number){
		Calendar c = Calendar.getInstance(); 
		int dom=c.get(Calendar.DAY_OF_MONTH);
		int  hour=c.get(Calendar.HOUR_OF_DAY);
		int  minute=c.get(Calendar.MINUTE);
		int year=c.get(Calendar.YEAR);
		String entry="";
		entry=entry.format("%02d:%02d", hour,minute);
		String month=getMonth();
		File f=getFile();
		Entry entries[]=new Entry[32];

		for(int i=1;i<32;i++){
			entries[i]=new Entry();
			entries[i].day=Integer.toString(i);			
		}
		try {
			BufferedReader r=new BufferedReader(new FileReader(f));
			String l=null;
			do{ 
				l=r.readLine();
				if(l!=null)
				{
					if(Character.isDigit( l.getBytes()[0] )){
						String items[]=l.split(",");

						int d=Integer.parseInt(items[0]);
						entries[d]=new Entry();
						entries[d].day=Integer.toString(d);
						if(1<items.length)
							entries[d].coming=items[1];
						if(2<items.length)
							entries[d].leaving=items[2];
						if(3<items.length)
							entries[d].breakstart=items[3];
						if(4<items.length)
							entries[d].breakend=items[4];
						if(5<items.length)
							entries[d].comment=items[5];
					}
				}
			}while(l!=null);
			r.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(action==eAction.ATTEND)
		{
		
			if(entries[dom].coming==null){
				final SharedPreferences p=this.getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
				AlarmManager am=(AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
				Intent intent = new Intent(AttendActitivty.this, AlarmActivity.class);
				int seconds=10 * 60 *60 + 40 *60;
				
				long alarm  =System.currentTimeMillis() + seconds * 1000;
				Editor  e=p.edit();
				e.putLong("alarm", alarm);
				e.commit();
				PendingIntent pendingIntent = PendingIntent.getActivity(AttendActitivty.this, 0, intent, PendingIntent.FLAG_ONE_SHOT);
				((AlarmManager) getSystemService(ALARM_SERVICE)).set(AlarmManager.RTC_WAKEUP, alarm, pendingIntent);
				
				
			
				
				entries[dom].coming=getCurTime();
			}
			entries[dom].leaving=getCurTime();
			if(!entries[dom].coming.equals(entries[dom].leaving))
			{
				stopAlarm();
			}
			if(entries[dom].breakstart!=null)
			{
				if(entries[dom].breakstart.length()>0)
				{
					if( entries[dom].breakend==null)
						entries[dom].breakend=getCurTime();
					if( entries[dom].breakend.length()<1)
						entries[dom].breakend=getCurTime();
					if( entries[dom].breakend.equals(entries[dom].breakstart))
						entries[dom].breakend=getCurTime();
				}else{
					entries[dom].breakend="";
				}
			}
		}
		if(action==eAction.PAUSE)
		{
			if(entries[dom].breakstart==null)
				entries[dom].breakstart=getCurTime();
			if(entries[dom].breakstart.length()<1)
				entries[dom].breakstart=getCurTime();
			entries[dom].breakend=entries[dom].breakstart;
			entries[dom].leaving=getCurTime();
		}
		entries[dom].comment=editable;

		try {
			FileWriter out=new FileWriter(f);

			out.write("Mitarbeiter,  "+name+" ,,,Personalnummer:, "+number+" \n");


			out.write(",,"+month+" ,"+year+",\n");
			out.write("Day,Coming,Leaving,Begin Break,End  Break,Comment\n");
			for(int i=1;i<32;i++)
			{
				String s=entries[i].toLine();
				out.write(s);
			}
			out.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	static String getCurTime()
	{
		Calendar c = Calendar.getInstance(); 
		int dom=c.get(Calendar.DAY_OF_MONTH);
		int  hour=c.get(Calendar.HOUR_OF_DAY);
		int  minute=c.get(Calendar.MINUTE);
		String ret="";
		ret=ret.format("%02d:%02d", hour,minute);
		return ret;

	}



	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_attenda_ctivity);






		nameView=(TextView)findViewById(R.id.textName);
		numberView=(TextView)findViewById(R.id.textNumber);
		inTextView=(TextView)findViewById(R.id.inView);
		outTextView=(TextView)findViewById(R.id.outView);
		breakView=(TextView)findViewById(R.id.pauseText);
		presenceView=(ImageView)findViewById(R.id.presenceImage);

		commentText=(EditText)findViewById(R.id.commentText);
		inTextView.setText("");
		outTextView.setText("");
		Button b=(Button)findViewById(R.id.attend);
		b.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final SharedPreferences p=getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
				String name=p.getString("name", "Your Name");
				String number=p.getString("number", "1234567");

				appendAttendance(eAction.ATTEND,commentText.getText().toString(),name,number);


				updateView();
				Intent i = new Intent( AttendActitivty.this, SaveActivity.class);
				startActivityForResult(i, 1);
			}
		});

		pauseButton=(Button)findViewById(R.id.pause);
		pauseButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				final SharedPreferences p=getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
				String name=p.getString("name", "Your Name");
				String number=p.getString("number", "1234567");

				appendAttendance(eAction.PAUSE,commentText.getText().toString(),name,number);
				updateView();
			}
		});

		updateView();




	}
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
       updateView();

	}


	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.attenda_ctivity, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {

			startActivityForResult(new Intent(this, SettingsActivity.class), 2);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}


	TextView inTextView;
	TextView outTextView;
	TextView breakView;
	TextView nameView;
	TextView numberView;
	EditText commentText;
	Button   pauseButton;
    ImageView presenceView;




	protected void updateView() {
		Entry curDay=getCurrentDay();
		final SharedPreferences p=getSharedPreferences("de.twapps.attendancetracker",  MODE_PRIVATE );
		String name=p.getString("name", "Your Name");
		String number=p.getString("number", "1234567");
		long alarm=p.getLong("alarm", 0);
		if(alarm>0)
		{
			presenceView.setImageResource(android.R.drawable.presence_online);
		}else{
			presenceView.setImageResource(android.R.drawable.presence_offline);
		}
		nameView.setText(name);
		numberView.setText(number);

		if(curDay.coming!=null)
			inTextView.setText(curDay.coming);
		else
			inTextView.setText("");

		if(curDay.leaving!=null)
			outTextView.setText(curDay.leaving);
		else
			outTextView.setText("");
		String breakText="";
		if(curDay.breakstart!=null){
			if(curDay.breakstart.length()>1)
			{
				breakText="Break: "+curDay.breakstart;
				pauseButton.setEnabled(false);
			}else{
				pauseButton.setEnabled(true);
			}
		}else{
			pauseButton.setEnabled(true);
		}
		if(curDay.breakend!=null)
			breakText+=" - "+ curDay.breakend;
		breakView.setText(breakText);
		if(curDay.comment!=null)
			commentText.setText(curDay.comment);
	}
}



