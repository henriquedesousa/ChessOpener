package org.ips.ests.chessopener;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import org.ips.ests.chessopener.api.BoardConstants;
import org.ips.ests.chessopener.api.ChessView;
import org.ips.ests.chessopener.api.chess.MyPGNProvider;
import org.ips.ests.chessopener.api.chess.PGNColumns;

import java.io.IOException;
import java.io.InputStream;

public class ChessBoardActivity extends AppCompatActivity {

	/** instances for the view and game of chess **/
	private ChessView _chessView;
	// private SaveGameDlg _dlgSave;
	// private ImageButton _butMenu;
	// private String[] _itemsMenu; // convenience member for 'dynamic final
									// variable' purpose
	// private Uri _uri
	// private String _action
	private long _lGameID;
	private float _fGameRating;
	private PowerManager.WakeLock _wakeLock;
	private Uri _uriNotification;
	private Ringtone _ringNotification;
	private final TextToSpeech _speech = null;

	public static final int REQUEST_SETUP = 1;
	public static final int REQUEST_OPEN = 2;
	public static final int REQUEST_OPTIONS = 3;
	public static final int REQUEST_NEWGAME = 4;
	public static final int REQUEST_FROM_QR_CODE = 5;

	/** Called when the activity is first created. */
	@SuppressLint("InvalidWakeLockTag")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
		_wakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK,
				"DoNotDimScreen");
		_uriNotification = null;
		_ringNotification = null;

		setContentView(R.layout.activity_chess_board);

		// Set a toolbar to replace the action bar.
		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_actionbar);
		setSupportActionBar(toolbar);


		_chessView = new ChessView(this);
		// String _keyboardBuffer = "";

		_lGameID = 0;
		_fGameRating = 2.5F;

		newGame();
	}

	@Override
	protected void onResume() {
		Log.i("main", "onResume");

		SharedPreferences prefs = getSharedPreferences("ChessPlayer", MODE_PRIVATE);

		if (prefs.getBoolean("wakeLock", true)) {
			_wakeLock.acquire(10*60*1000L /*10 minutes*/);
		}

		String sOpeningDb = prefs.getString("OpeningDb", null);
		if (sOpeningDb == null) {
			try {
				String outFilename = "/data/data/" + getPackageName()
						+ "/db.bin";
				_chessView.loadDB(getAssets().open("db.bin"), outFilename, 17);
			} catch (IOException e) {

				Log.e("onResume", e.toString());
			}
		} else {
			Uri uri = Uri.parse(sOpeningDb);
			Log.i("onResume", "db : " + uri.getPath());
			_chessView.setOpeningDb(uri.getPath());
		}

		StringBuilder sPGN;
		String sFEN = prefs.getString("FEN", null);

		String sTmp = prefs.getString("NotificationUri", null);
		if (sTmp == null) {
			_uriNotification = null;
			_ringNotification = null;
		} else {
			_uriNotification = Uri.parse(sTmp);
			_ringNotification = RingtoneManager.getRingtone(this,
					_uriNotification);
		}

		final Intent intent = getIntent();
		Uri uri = intent.getData();

		if (uri != null) {
			_lGameID = 0;
			sPGN = new StringBuilder();
			Log.i("onResume", "opening " + uri);
			InputStream is;
			try {
				is = getContentResolver().openInputStream(uri);
				byte[] b = new byte[4096];
				int len;

				while ((len = is.read(b)) > 0) {
					sPGN.append(new String(b));
				}
				is.close();

				sPGN = new StringBuilder(sPGN.toString().trim());

				loadPGN(sPGN.toString());

			} catch (Exception e) {
				sPGN = new StringBuilder(prefs.getString("game_pgn", ""));
				Log.e("onResume", "Failed " + e);
			}
		} else if (sFEN != null) {
			// default, from prefs
			Log.i("onResume", "Loading FEN " + sFEN);
			_lGameID = 0;
			loadFEN(sFEN);
		} else {
			_lGameID = prefs.getLong("game_id", 0);
			if (_lGameID > 0) {
				Log.i("onResume", "loading saved game " + _lGameID);
				loadGame();
			} else {
				sPGN = new StringBuilder(prefs.getString("game_pgn", null));
				Log.i("onResume", "pgn: " + sPGN);
				loadPGN(sPGN.toString());
			}
		}

		_chessView.OnResume(prefs);

		_chessView.updateState();
		//
		super.onResume();
	}

	@Override
	protected void onPause() {
		if (_wakeLock.isHeld()) {
			_wakeLock.release();
		}

		if (_lGameID > 0) {
			ContentValues values = new ContentValues();

			values.put(PGNColumns.DATE, _chessView.getDate().getTime());
			values.put(PGNColumns.WHITE, _chessView.getWhite());
			values.put(PGNColumns.BLACK, _chessView.getBlack());
			values.put(PGNColumns.PGN, _chessView.exportFullPGN());
			values.put(PGNColumns.RATING, _fGameRating);
			values.put(PGNColumns.EVENT, _chessView.getPGNHeadProperty("Event"));

			saveGame(values, false);
		}
		SharedPreferences.Editor editor = getSharedPreferences("ChessPlayer",
				MODE_PRIVATE).edit();
		editor.putLong("game_id", _lGameID);
		editor.putString("game_pgn", _chessView.exportFullPGN());
		editor.putString("FEN", null); //
		if (_uriNotification == null)
			editor.putString("NotificationUri", null);
		else
			editor.putString("NotificationUri", _uriNotification.toString());
		_chessView.OnPause(editor);

		editor.commit();

		super.onPause();
	}

	@Override
	protected void onDestroy() {
		_chessView.OnDestroy();
		if (_speech != null) {
			_speech.stop();
			_speech.shutdown();
		}
		super.onDestroy();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		super.onActivityResult(requestCode, resultCode, data);
		Log.i("main", "onActivityResult");

		if (requestCode == REQUEST_SETUP) {
			if (resultCode == RESULT_OK) {
				// data));
				_chessView.clearPGNView();
			}
		} else if (requestCode == REQUEST_OPEN) {
			if (resultCode == RESULT_OK) {

				Uri uri = data.getData();
				try {
					_lGameID = Long.parseLong(uri.getLastPathSegment());
				} catch (Exception ex) {
					_lGameID = 0;
				}
				SharedPreferences.Editor editor = getSharedPreferences(
						"ChessPlayer", MODE_PRIVATE).edit();
				editor.putLong("game_id", _lGameID);
				editor.putInt("boardNum", 0);
				editor.putString("FEN", null);
				editor.commit();
			}
		} else if (requestCode == REQUEST_FROM_QR_CODE) {
			if (resultCode == RESULT_OK) {
				String contents = data.getStringExtra("SCAN_RESULT");
				// String format = data.getStringExtra("SCAN_RESULT_FORMAT");

				SharedPreferences.Editor editor = getSharedPreferences(
						"ChessPlayer", MODE_PRIVATE).edit();
				editor.putLong("game_id", 0);
				editor.putInt("boardNum", 0);
				editor.putString("FEN", contents);
				editor.commit();
				// doToast("Content: " + contents + "::" + format);
			}
		} else if (requestCode == REQUEST_NEWGAME) {

			if (resultCode == BoardConstants.RESULT_960) {
				newGameRandomFischer();
			} else if (resultCode == RESULT_OK) {
				newGame();
			}
		}
	}

	public void doToast(final String text) {
		Toast t = Toast.makeText(this, text, Toast.LENGTH_LONG);
		t.setGravity(Gravity.BOTTOM, 0, 0);
		t.show();
	}

	private void loadFEN(String sFEN) {
		if (sFEN != null) {
			Log.i("loadFEN", sFEN);
			if (!_chessView.initFEN(sFEN, true)) {
				doToast(getString(R.string.err_load_fen));
				Log.e("loadFEN", "FAILED");
			}
			_chessView.updateState();
		}
	}

	private void loadPGN(String sPGN) {
		if (sPGN != null) {
			if (!_chessView.loadPGN(sPGN)) {
				doToast(getString(R.string.err_load_pgn));
			}
			_chessView.updateState();
		}
	}

	private void newGame() {
		_lGameID = 0;
		_chessView.newGame();
		SharedPreferences.Editor editor = getSharedPreferences("ChessPlayer",
				MODE_PRIVATE).edit();
		editor.putString("FEN", null);
		editor.putInt("boardNum", 0);
		editor.putString("game_pgn", null);
		editor.putLong("game_id", _lGameID);
		editor.commit();
	}

	private void newGameRandomFischer() {
		_lGameID = 0;

		int seed = getSharedPreferences("ChessPlayer", MODE_PRIVATE).getInt(
				"randomFischerSeed", -1);
		seed = _chessView.newGameRandomFischer(seed);
		doToast(String.format(getString(R.string.chess960_position_nr), seed));

		SharedPreferences.Editor editor = getSharedPreferences("ChessPlayer",
				MODE_PRIVATE).edit();
		editor.putString("FEN", _chessView.getJNI().toFEN());
		editor.putInt("boardNum", 0);
		editor.putString("game_pgn", null);
		editor.putLong("game_id", _lGameID);
		editor.commit();
	}

	@SuppressLint("Range")
	private void loadGame() {
		if (_lGameID > 0) {
			Uri uri = ContentUris.withAppendedId(MyPGNProvider.CONTENT_URI,
					_lGameID);
			try ( Cursor c = managedQuery(uri, PGNColumns.COLUMNS, null, null, null) ) {
				if (c != null && c.getCount() == 1) {

					c.moveToFirst();

					_lGameID = c.getLong(c.getColumnIndex(PGNColumns._ID));
					String sPGN = c.getString(c.getColumnIndex(PGNColumns.PGN));
					_chessView.loadPGN(sPGN);

					_chessView.setPGNHeadProperty("Event",
							c.getString(c.getColumnIndex(PGNColumns.EVENT)));
					_chessView.setPGNHeadProperty("White",
							c.getString(c.getColumnIndex(PGNColumns.WHITE)));
					_chessView.setPGNHeadProperty("Black",
							c.getString(c.getColumnIndex(PGNColumns.BLACK)));
					_chessView.setDateLong(c.getLong(c
							.getColumnIndex(PGNColumns.DATE)));

					_fGameRating = c.getFloat(c.getColumnIndex(PGNColumns.RATING));
				} else {
					_lGameID = 0; // probably deleted
				}
			}
		} else {
			_lGameID = 0;
		}
	}

	@SuppressLint("Range")
	public void saveGame(ContentValues values, boolean bCopy) {

		SharedPreferences.Editor editor = getSharedPreferences("ChessPlayer",
				MODE_PRIVATE).edit();
		editor.putString("FEN", null);
		editor.commit();

		_chessView.setPGNHeadProperty("Event",
				(String) values.get(PGNColumns.EVENT));
		_chessView.setPGNHeadProperty("White",
				(String) values.get(PGNColumns.WHITE));
		_chessView.setPGNHeadProperty("Black",
				(String) values.get(PGNColumns.BLACK));
		_chessView.setDateLong((Long) values.get(PGNColumns.DATE));

		_fGameRating = (Float) values.get(PGNColumns.RATING);
		//

		if (_lGameID > 0 && !bCopy) {
			Uri uri = ContentUris.withAppendedId(MyPGNProvider.CONTENT_URI,
					_lGameID);
			getContentResolver().update(uri, values, null, null);
		} else {
			Uri uri = MyPGNProvider.CONTENT_URI;
			Uri uriInsert = getContentResolver().insert(uri, values);
			try ( Cursor c = managedQuery(uriInsert, new String[] { PGNColumns._ID },
					null, null, null) ) {
				if (c != null && c.getCount() == 1) {
					c.moveToFirst();
					_lGameID = c.getLong(c.getColumnIndex(PGNColumns._ID));
				}
			}
		}
	}
}