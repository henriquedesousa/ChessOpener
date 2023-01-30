package org.ips.ests.chessopener;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.ips.ests.chessopener.api.ChessView;
import org.ips.ests.chessopener.api.GameControl;
import org.ips.ests.chessopener.biblioteca.LibraryActivity;

import org.ips.ests.chessopener.utils.PreferenceUtils;
import org.ips.ests.chessopener.utils.UiUtils;

public class MainActivity extends AppCompatActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set a toolbar to replace the action bar.
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);


        Button pvp = (Button) findViewById(R.id.button_training);
        pvp.setOnClickListener(v -> {
			ChessView._playMode = GameControl.HUMAN_HUMAN;
			Intent myIntent = new Intent(MainActivity.this, ChessBoardActivity.class);
			MainActivity.this.startActivity(myIntent);
		});




		Button pva = (Button) findViewById(R.id.button_game);
		pva.setOnClickListener(v -> {
			ChessView._playMode = GameControl.HUMAN_PC;
			Intent myIntent = new Intent(MainActivity.this, ChessBoardActivity.class);
			MainActivity.this.startActivity(myIntent);
		});

		Button btnBiblioteca = (Button) findViewById(R.id.button_library);
		btnBiblioteca.setOnClickListener(v -> {

			Intent myIntent = new Intent(MainActivity.this, LibraryActivity.class);
			MainActivity.this.startActivity(myIntent);
		});

		if (PreferenceUtils.isFirstUsage(this)) {
            PreferenceUtils.setFirstUsage(this, false);
			UiUtils.showAboutDialog(this);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			Toast.makeText(this, getString(R.string.not_implemented), Toast.LENGTH_SHORT).show();
			return true;


	} else if (id == R.id.action_about) {
			UiUtils.showAboutDialog(this);
		}

		return super.onOptionsItemSelected(item);
	}


}
