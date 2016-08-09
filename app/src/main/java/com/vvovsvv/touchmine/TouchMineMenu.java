package com.vvovsvv.touchmine;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class TouchMineMenu extends Activity {
	String easy_string = "9x9x10";
	String standard_string = "16x16x40";
	String pro_string = "30x16x99";
	
	public void setRecords()
	{
		SharedPreferences myPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
		
		TextView txt_easy_record_view = (TextView)findViewById(R.id.textView_easy);		
		int saved_record = myPrefs.getInt(easy_string, 999);
		txt_easy_record_view.setText(String.valueOf(saved_record));
		
		TextView txt_standard_record_view = (TextView)findViewById(R.id.textView_standard);
		saved_record = myPrefs.getInt(standard_string, 999);
		txt_standard_record_view.setText(String.valueOf(saved_record));
		
		TextView txt_pro_record_view = (TextView)findViewById(R.id.textView_professional);
		saved_record = myPrefs.getInt(pro_string, 999);
		txt_pro_record_view.setText(String.valueOf(saved_record));
	}
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_start);
				
		setRecords();
		
		Button help = (Button)findViewById(R.id.button_help);
		help.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				Intent help_act = new Intent(getApplicationContext(),HelpActivity.class);
				TouchMineMenu.this.startActivity(help_act);
				}
			});
	}
	
	public void StartGame(int x, int y, int mines)
	{
		Bundle extra = new Bundle();
		extra.putInt("x", x);
		extra.putInt("y", y);
		extra.putInt("mines", mines);
		Intent game = new Intent(getApplicationContext(),MainActivity.class);
		game.putExtras(extra);
		TouchMineMenu.this.startActivity(game);
	}
	
	public void onClickStartGameHandler(View target) {
		switch(target.getId()) 
		{
		case R.id.button_easy:
			StartGame(9,9,10);
			break;
		case R.id.button_standard:
			StartGame(16,16,40);
			break;
		case R.id.button_professional:
			StartGame(30,16,99);
			break;
		}
	}
	
	public void onResetRecordHandler(View target) {
		String res_string = "";
		switch(target.getId()) 
		{
		case R.id.textView_easy:
			res_string = this.easy_string; 
			break;
		case R.id.textView_standard:
			res_string = this.standard_string;
			break;
		case R.id.textView_professional:
			res_string = this.pro_string;
			break;
		}
		
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.reset_record);
		builder.setMessage(R.string.sure_reset_record);
		builder.setPositiveButton(R.string.yes_sure, new ResetRecordListener(res_string));
		builder.setNegativeButton(R.string.no_i_dont, null);
		AlertDialog dialog = builder.create();
		dialog.show();
	}
	
	public class ResetRecordListener implements DialogInterface.OnClickListener {
		public ResetRecordListener(String res_str) {
			super();
			this.res_str = res_str;
		}

		public String res_str;
		
		@Override
		public void onClick(DialogInterface dialog, int which) 
		{
			SharedPreferences myPrefs = getSharedPreferences("settings", Context.MODE_PRIVATE);
			Editor editor = myPrefs.edit();
			editor.putInt(res_str, 999);
			editor.commit();
			
			setRecords();
		}
	}

}
