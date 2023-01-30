package org.ips.ests.chessopener.api;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import org.ips.ests.chessopener.R;
import org.ips.ests.chessopener.Start;
import org.ips.ests.chessopener.api.chess.Move;
import org.ips.ests.chessopener.api.chess.PGNEntry;
import org.ips.ests.chessopener.api.chess.UI;
import org.ips.ests.chessopener.biblioteca.LibraryActivity;
import org.ips.ests.chessopener.model.Opening;
import org.ips.ests.chessopener.utils.OpeningUtils;
import org.ips.ests.chessopener.utils.UiUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.ViewAnimator;
import android.widget.ViewSwitcher;

/**
 * 
 */
public class ChessView extends UI {
	private final ChessViewBase _view;

    private final Button _btnCurrentOpening;
	private final Activity _parent;
	private ImageButton _butPlay;
	private ViewAnimator _viewAnimator;
	private ProgressBar _progressPlay;
	private TextView _tvClockMe, _tvClockOpp, _tvTitleMe, _tvTitleOpp, _tvAnnotate, _tvEngine, _tvAnnotateGuess;
	private int _dpadPos;
	public static int _playMode;
	private String _sPrevECO;
	private final HorizontalScrollView _hScrollHistory;
	private final ScrollView _vScrollHistory;
	private final RelativeLayout _layoutHistory;
	private final ArrayList<PGNView> _arrPGNView;
	private final LayoutInflater _inflater;
	private boolean _bAutoFlip, _bShowMoves, _bShowLastMove;
	private ViewSwitcher _switchTurnMe, _switchTurnOpp;
	private SeekBar _seekBar; 
	private Vibrator _vibrator;
	private ImageView _imgStatusGuess;
	private JSONArray _jArrayECO;

	// keep track of captured pieces
	private ImageView[][] _arrImageCaptured;
	private final TextView[][] _arrTextCaptured;
	///////////////////////////////
	
	public static int SUBVIEW_CPU 		= 0;
	public static int SUBVIEW_CAPTURED 	= 1;
	public static int SUBVIEW_SEEK 		= 2;
	public static int SUBVIEW_HISTORY 	= 3;
	public static int SUBVIEW_ANNOTATE 	= 4;
	public static int SUBVIEW_GUESS 	= 5;
	public static int SUBVIEW_BLINDFOLD	= 6;
	public static int SUBVIEW_ECO		= 7;
	
	static class InnerHandler extends Handler{
		WeakReference<ChessView> _chessView;
		
		InnerHandler(ChessView view){
			_chessView = new WeakReference<>(view);
		}
		@Override public void handleMessage(Message msg) {
        	
			ChessView chessView = _chessView.get();
			if(chessView != null){
	        	long lTmp;
	        	if(chessView._view._flippedBoard){
	        		lTmp = chessView.getBlackRemainClock();
	        	}
	        	else{
	        		lTmp = chessView.getWhiteRemainClock();
	        	}
	    		if(lTmp < 0){
	    			lTmp = -lTmp;
	    			if(chessView._tvClockMe != null) chessView._tvClockMe.setTextColor(0xffff0000);
	    		}
	    		 else {
	    			 if(chessView._tvClockMe != null) chessView._tvClockMe.setTextColor(0xffffffff);
	     		}
	    		if(chessView._tvClockMe != null) chessView._tvClockMe.setText(chessView.formatTime(lTmp));
	    		
	    		if(chessView._view._flippedBoard){
	    			lTmp = chessView.getWhiteRemainClock();
	    		} else {
	    			lTmp = chessView.getBlackRemainClock();
	    		}
	    		if(lTmp < 0){
	    			lTmp = -lTmp;
	    			if(chessView._tvClockOpp != null) chessView._tvClockOpp.setTextColor(0xffff0000);
	    		} else {
	    			if(chessView._tvClockOpp != null) chessView._tvClockOpp.setTextColor(0xffffffff);
	    		}
	    		if(chessView._tvClockOpp != null) chessView._tvClockOpp.setText(chessView.formatTime(lTmp));	
			}
        }
	}
	
	protected InnerHandler m_timerHandler = new InnerHandler(this);
	
	
	public ChessView(Activity activity) {
		super();
		_parent = activity;
		_view = new ChessViewBase(activity);
		
		//_playMode = HUMAN_PC;
		_bAutoFlip = false;
		_bShowMoves = false;
		_bShowLastMove = true;
		_dpadPos = -1;
		
		_arrPGNView = new ArrayList<>();
		
		_inflater = (LayoutInflater)_parent.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
		
		OnClickListener ocl = arg0 -> handleClick(_view.getIndexOfButton(arg0));
		
		_vScrollHistory = null;
		_hScrollHistory = null;
		_layoutHistory = null;
		
		_view.init(ocl);

		_jArrayECO = null;


        _btnCurrentOpening = (Button)_parent.findViewById(R.id.ButtonCurrentOpening);
        _btnCurrentOpening.setOnClickListener(v -> {

			Opening opening = OpeningUtils.findOpeningFromString(((Button)v).getText().toString(), Start.openings);
			if (opening != null) {
				Bundle args = new Bundle();
				args.putSerializable(Opening.OPENING_BUNDLE_KEY, opening);

				Intent i = new Intent(_parent, LibraryActivity.class);
				i.putExtras(args);
				_parent.startActivity(i);

			} else {
				UiUtils.doToast(_parent, _parent.getString(R.string.toast_opening_not_in_db));
			}
		});

		
		OnClickListener oclUndo = arg0 -> {
			if(m_bActive){
				undo();
			} else {
				stopThreadAndUndo();
			}
		};

		ImageButton butPrevious = (ImageButton)_parent.findViewById(R.id.ButtonPrevious);
		if(butPrevious != null){
			//butPrevious.setFocusable(false);
			butPrevious.setOnClickListener(oclUndo);
		}
		OnClickListener oclFf = arg0 -> {
			if(m_bActive){
				jumptoMove(_jni.getNumBoard());
				updateState();
			}

		};
    	
		ImageButton butNext = (ImageButton)_parent.findViewById(R.id.ButtonNext);
		if(butNext != null){
			//butNext.setFocusable(false);
			butNext.setOnClickListener(oclFf);
		}

        Button butNewGame = (Button)_parent.findViewById(R.id.ButtonNewGame);
		if(butNewGame != null){
			//butNewGame.setFocusable(false);
			butNewGame.setOnClickListener(arg0 -> {
//	        		Intent intent = new Intent();
//	        	    intent.setClass(_parent, options.class);
//	        	    intent.putExtra("requestCode", ChessBoardActivity.REQUEST_NEWGAME);
//	        		_parent.startActivityForResult(intent, ChessBoardActivity.REQUEST_NEWGAME);

				newGame();
			});
		}

		
//		Button bHintGuess = (Button)_parent.findViewById(R.id.ButtonHintGuess);
    	Button bHintGuess = null;
		if(bHintGuess != null){
			//bHintGuess.setFocusable(false);
			bHintGuess.setOnClickListener(v -> {
				int iFrom = getFromOfNextMove();
				if(iFrom == -1){
					_tvAnnotateGuess.setText("No move available");
				} else {
					m_iFrom = iFrom;
					paintBoard();
				}
			});
		}


			
		_arrTextCaptured = new TextView[2][5];
		_selectedLevel = 3;
		_lClockStartWhite = 0;
		_lClockStartBlack = 0;
		_lClockTotal = 0;

		Timer _timer = new Timer(true);
        _timer.schedule(new TimerTask(){
			@Override public void run() {
				Message msg = new Message();
			    msg.what = 1;
			    m_timerHandler.sendMessage(msg);				
			}}, 1000, 1000);
        
        _sPrevECO = null;

	}
	
	private String formatTime(long msec){
		final String sTmp = String.format("%02d:%02d", (int)(Math.floor(msec/60000)), ((int)(msec / 1000) % 60)); 
    	return sTmp;
    }
		
	public void setAutoFlip(boolean b){
		_bAutoFlip = b;
	}
	public void setShowMoves(boolean b){
		_bShowMoves = b;
	}

	public void onClickPGNView(PGNView item){
		if(m_bActive){
			int i = _arrPGNView.indexOf(item);
			Log.i("onClickPGNView", "index " + i);
			if(_jni.getNumBoard()-1 > i)
				jumptoMove(i+2);
			else
				jumptoMove(i+1);

			updateState();
		}
	}
	
	public void clearPGNView(){
        _btnCurrentOpening.setEnabled(false);
        _btnCurrentOpening.setText(_parent.getString(R.string.button_no_opening));
		_arrPGNView.clear();
		if(_layoutHistory != null){
			_layoutHistory.removeAllViews();
		}
		updateState();
	}

	@Override public void newGame(){
		super.newGame();
		clearPGNView();
	}
	@Override public int newGameRandomFischer(int seed){
		
		int ret = super.newGameRandomFischer(seed);
		clearPGNView();
		
		return ret;
	}
	@Override 
	public void addPGNEntry(int ply, String sMove, String sAnnotation, int move, boolean bScroll){
		super.addPGNEntry(ply, sMove, sAnnotation, move, bScroll);
		
		while(ply >= 0 && _arrPGNView.size() >= ply)
			_arrPGNView.remove(_arrPGN.size()-1);
		
		View v = _inflater.inflate(R.layout.pgn_item, null, false);
		v.setId(ply);
		_arrPGNView.add(new PGNView(this, v, ply, sMove, sAnnotation.length() > 0));
		
		if(_layoutHistory != null){
			while(ply >= 0 && _layoutHistory.getChildCount() >= ply)
				_layoutHistory.removeViewAt(_layoutHistory.getChildCount() - 1);
		
			
			RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
			if(_layoutHistory.getChildCount() > 0){
				if(_vScrollHistory != null){
					if(_layoutHistory.getChildCount() % 2 == 0){
						lp.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
						lp.addRule(RelativeLayout.BELOW, _layoutHistory.getChildAt(_layoutHistory.getChildCount()-1).getId());
					} else {
						lp.addRule(RelativeLayout.RIGHT_OF, _layoutHistory.getChildAt(_layoutHistory.getChildCount()-1).getId());
						if(_layoutHistory.getChildCount() > 2){
							lp.addRule(RelativeLayout.BELOW, _layoutHistory.getChildAt(_layoutHistory.getChildCount()-2).getId());
						}
					}
				} else if(_hScrollHistory != null){
					lp.addRule(RelativeLayout.RIGHT_OF, _layoutHistory.getChildAt(_layoutHistory.getChildCount()-1).getId());
				}
			}
			_layoutHistory.addView(v, lp);
			
		}
		
		if(bScroll){
			scrollToEnd();
		}
	}
	@Override public void setAnnotation(int i, String sAnno){
		super.setAnnotation(i, sAnno);

		_arrPGNView.get(i).setAnnotated(sAnno.length() > 0);
		_arrPGNView.get(i).setSelected(false);
	}
	
	@Override
	public void paintBoard(){

		int[] arrSelPositions;
		
		int lastMove = _jni.getMyMove(); 
		if(lastMove != 0 && _bShowLastMove){
			arrSelPositions = new int[4];
			arrSelPositions[0] = m_iFrom;
			arrSelPositions[1] = Move.getTo(lastMove);
			arrSelPositions[2] = Move.getFrom(lastMove);
			arrSelPositions[3] = _dpadPos;
		}
		else{
			arrSelPositions = new int[2];
			arrSelPositions[0] = m_iFrom;
			arrSelPositions[1] = _dpadPos;
		}
		int turn = _jni.getTurn();
		// 
		if(_playMode == HUMAN_HUMAN &&
				_bAutoFlip && 
				(turn == BoardConstants.WHITE && _view.getFlippedBoard() ||
				turn == BoardConstants.BLACK && false == _view.getFlippedBoard()))
				_view.flipBoard();
		
		
		ArrayList<Integer> arrPos = new ArrayList<Integer>();
		// collect legal moves if pref is set
		if(_bShowMoves && m_iFrom != -1){
			try{
				// via try catch because of empty or mem error results in exception
				
				if(_jni.isEnded() == 0){
					synchronized(this) {
						int size = _jni.getMoveArraySize();
						//Log.i("paintBoard", "# " + size);
						int move;
						for(int i = 0; i < size; i++){
							move = _jni.getMoveArrayAt(i);
							if(Move.getFrom(move) == m_iFrom){
								arrPos.add(Move.getTo(move));
							}
						}
					}
				}
			} catch(Exception e){
				System.gc();
			}
		}
		
		_view.paintBoard(_jni, arrSelPositions, arrPos);
		
		if(_layoutHistory != null){
			for(int i = 0; i < _layoutHistory.getChildCount(); i++){
				_arrPGNView.get(i).setSelected(i == _jni.getNumBoard() - 2);
			}
		}
	}
	public int getPlayMode(){
    	return _playMode;
    }

	public void flipBoard(){
		_view.flipBoard();
		updateState();
	}
	
	public void setFlippedBoard(boolean b){
		_view.setFlippedBoard(b);
	}
	public boolean getFlippedBoard(){
		return _view._flippedBoard;
	}
	
	@Override public void play(){
		if(_jni.isEnded() == 0 && _progressPlay != null){
			if(_progressPlay.getVisibility() == View.VISIBLE){
				_progressPlay.setVisibility(View.GONE);
				if (_butPlay != null) {
					_butPlay.setVisibility(View.VISIBLE);	
				}
				
			} else {
				_progressPlay.setVisibility(View.VISIBLE);
				if (_butPlay != null) {
					_butPlay.setVisibility(View.GONE);
				}
			}
			
		}
		super.play();
	}
	
	public boolean handleClickFromPositionString(String s){
		int index = Pos.fromString(s);
		if(_view._flippedBoard){
			index = 63 - index;
		}
		return handleClick(index);
	}
	
	@Override
	public boolean handleClick(int index)
	{
		if(false == m_bActive)
		{
			setMessage(R.string.msg_wait);
			return false;
		}
		
		final int iTo = _view.getFieldIndex(index);
		if(m_iFrom != -1){
			
			// Guess the move ===============================
			if(_viewAnimator != null){
				if(_viewAnimator.getDisplayedChild() == SUBVIEW_GUESS){
					if(wasMovePlayed(m_iFrom, iTo)){
						if(_imgStatusGuess != null){
							_imgStatusGuess.setImageResource(R.drawable.indicator_ok);
						}
						jumptoMove(_jni.getNumBoard());
						updateState();
						m_iFrom = -1;
						//Log.i("WAS MOVE PLAYED", "TRUE");
						return true;
					} else {
						if(_imgStatusGuess != null){
							_imgStatusGuess.setImageResource(R.drawable.indicator_error);
						}
						m_iFrom = -1;
						paintBoard();
						//Log.i("WAS MOVE PLAYED", "FALSE");
						return false;
					}
				}
			}
			
			// check if it is a promotion piece
			if(		_jni.pieceAt(BoardConstants.WHITE, m_iFrom) == BoardConstants.PAWN && 
					BoardConstants.ROW_TURN[BoardConstants.WHITE][m_iFrom] == 6 && 
					BoardConstants.ROW_TURN[BoardConstants.WHITE][iTo] == 7
					||
					_jni.pieceAt(BoardConstants.BLACK, m_iFrom) == BoardConstants.PAWN && 
					BoardConstants.ROW_TURN[BoardConstants.BLACK][m_iFrom] == 6 &&
					BoardConstants.ROW_TURN[BoardConstants.BLACK][iTo] == 7){
				
				final String[] items = _parent.getResources().getStringArray(R.array.promotionpieces);
				
				AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
				builder.setTitle(R.string.title_pick_promo);
				builder.setSingleChoiceItems(items, 0, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	dialog.dismiss();
				    	_jni.setPromo(4 - item);
				        boolean bValid = requestMove(m_iFrom, iTo);
						m_iFrom = -1;
						if(false == bValid)
							paintBoard();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
				
				if(_vibrator != null)
				{
					_vibrator.vibrate(40L);
				}
				
				return true;
			} else if(_jni.isAmbiguousCastle(m_iFrom, iTo) != 0){ // in case of Fischer
				
				AlertDialog.Builder builder = new AlertDialog.Builder(_parent);
				builder.setTitle(R.string.title_castle);
				builder.setPositiveButton(R.string.alert_yes, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	dialog.dismiss();
				    	requestMoveCastle(m_iFrom, iTo);
						m_iFrom = -1;
				    }
				});
				builder.setNegativeButton(R.string.alert_no, new DialogInterface.OnClickListener() {
				    public void onClick(DialogInterface dialog, int item) {
				    	dialog.dismiss();
				    	if(m_iFrom != iTo){
				    		requestMove(m_iFrom, iTo);
				    	}
						m_iFrom = -1;
						paintBoard();
				    }
				});
				AlertDialog alert = builder.create();
				alert.show();
				
				if(_vibrator != null)
				{
					_vibrator.vibrate(40L);
				}
				
				return true; // done, return from method!
			}
			//Log.i("ChessView", "====== not a special move");
		}
		// if to is same as from (not in case of Fischer random castle!
		if(m_iFrom == iTo){
			m_iFrom = -1;
			paintBoard();
			return false;
		}
		if(super.handleClick(iTo))
		{
			if(_vibrator != null)
			{
				_vibrator.vibrate(40L);
			}
			return true;
		}
		return false;
	}
	@Override
	public void setMessage(String sMsg)
	{
		UiUtils.doToast(_parent, sMsg);
		//_tvMessage.setText(sMsg);
		//m_textMessage.setText(sMsg);
	}
	@Override
	public void setEngineMessage(String sText)
	{
		if(_tvEngine != null){
			_tvEngine.setText(sText);
		}
	}
	@Override
	public void setMessage(int res){
		//_tvMessage.setText(res);
		UiUtils.doToast(_parent, _parent.getString(res));
	}
	
	public void setPlayMode(int mode){
		_playMode = mode;
	}
	
	/*
	public void adjustWidth(){
		// after resume of view, all images should force width of includeboard
		
		View v = (View)_parent.findViewById(R.id.LayoutBottomClock);
		if(v != null){
			int w = v.getWidth();
			//Log.i("ChessView", "w = " + w);
			View boardView = (View)_parent.findViewById(R.id.includeboard);
			w = boardView.getWidth();
			//Log.i("ChessView", "w = " + w);
			v.setMinimumWidth(w);
			v = (View)_parent.findViewById(R.id.LayoutTopClock);
			v.setMinimumWidth(w);
		}
		
		//
	}
	*/
	
	public void OnPause(SharedPreferences.Editor editor){

		if(_uci.isReady()){
			_uci.quit();
		} else {
			if(m_bActive == false){
				_jni.interrupt();
			}
		}		
		
		editor.putBoolean("flippedBoard", _view.getFlippedBoard());
		editor.putInt("levelMode", m_iLevelMode);
		editor.putInt("level", _selectedLevel);
		editor.putInt("levelPly", _selectedLevelPly);
		editor.putInt("playMode", _playMode);
		editor.putBoolean("autoflipBoard", _bAutoFlip);
		editor.putBoolean("showMoves", _bShowMoves);
		editor.putInt("boardNum", _jni.getNumBoard());
		if(_viewAnimator != null){
			editor.putInt("animatorViewNumber", _viewAnimator.getDisplayedChild());
		}	
		pauzeTimer();
		editor.putLong("clockTotalMillies", _lClockTotal);
		editor.putLong("clockWhiteMillies", _lClockWhite);
		editor.putLong("clockBlackMillies", _lClockBlack);
	}

	public void OnResume(SharedPreferences prefs){
		super.OnResume();
		
		_view.OnResume();		
		
		ChessViewBase._showCoords = prefs.getBoolean("showCoords", false);
		
		String sEngine = prefs.getString("UCIEngine", null);
		if(sEngine != null){
			String sEnginePath = "/data/data/jwtc.android.chess/" + sEngine;
			File f = new File(sEnginePath);
			if(f.exists()){
				_uci.init(sEnginePath);
				if(_tvEngine != null){
					_tvEngine.setText("UCI engine " + sEngine);
				}
			}
		}
		_view.setFlippedBoard(prefs.getBoolean("flippedBoard", false));
		_bAutoFlip = prefs.getBoolean("autoflipBoard", false);
		_bShowMoves = prefs.getBoolean("showMoves", true);
		_bShowLastMove = prefs.getBoolean("showLastMove", true);
		setLevelMode(prefs.getInt("levelMode", LEVEL_TIME));
		_selectedLevel = prefs.getInt("level", 2);
		_selectedLevelPly = prefs.getInt("levelPly", 2);
		//_playMode = prefs.getInt("playMode", HUMAN_PC);
		
		if(prefs.getBoolean("onLoadJumpToLastMove", false)){
			
		} else {
			jumptoMove(prefs.getInt("boardNum", 0));
		}
		
		_lClockTotal = prefs.getLong("clockTotalMillies", 0); 
		_lClockWhite = prefs.getLong("clockWhiteMillies", 0);
		_lClockBlack = prefs.getLong("clockBlackMillies", 0);
		continueTimer();
		
		ChessImageView._colorScheme = prefs.getInt("ColorScheme", 0);
		if(_viewAnimator != null){
			_viewAnimator.setDisplayedChild(prefs.getInt("animatorViewNumber", 0) % _viewAnimator.getChildCount());
		}
		
		
		
		///////////////////////////////////////////////////////////////////
		
		if(prefs.getBoolean("showECO", true) && _jArrayECO == null){
			try {
				long start = System.currentTimeMillis();
				InputStream in = _parent.getAssets().open("ECO.json");
				BufferedReader br = new BufferedReader(new InputStreamReader(in));
	
				StringBuffer sb = new StringBuffer();
				String line;
	
				while ((line = br.readLine()) != null) {
					sb.append(line + "\n");
				}
				
				in.close();
				
				_jArrayECO = new JSONArray(sb.toString());
				Log.i("ChessView", "ECO jArray - size " + _jArrayECO.length() + " load " + (System.currentTimeMillis() - start));
				
			} catch (Exception e) {
				
			}
		}
		
		/////////////////////////////////////////////////////////////////	
		
	}

	@Override
	public void updateState(){
		super.updateState();
		
		if(_progressPlay != null &&_progressPlay.getVisibility() == View.VISIBLE){
			if(m_bActive){
				_progressPlay.setVisibility(View.GONE);
				if (_butPlay != null) {
					_butPlay.setVisibility(View.VISIBLE);	
				}
			}
		} else {
			if(false == m_bActive){
				_progressPlay.setVisibility(View.VISIBLE);
				if (_butPlay != null) {
					_butPlay.setVisibility(View.GONE);
				}
			}
		}
		
		int i = _jni.getNumBoard()-2;
		
		if(_tvAnnotate != null){
			if(i >= 0 && i < _arrPGN.size()){
				_tvAnnotate.setText(_arrPGN.get(i)._sAnnotation);
				if(_tvAnnotateGuess != null){
					_tvAnnotateGuess.setText(_arrPGN.get(i)._sAnnotation);
				}
			}
			else {
				_tvAnnotate.setText("");
				if(_tvAnnotateGuess != null){
					_tvAnnotateGuess.setText("");
				}
			}
		}
		int turn, piece;
		if(_arrImageCaptured != null && _arrImageCaptured[0][0] != null){
		
			for(turn = 0; turn < 2; turn++){
				for(piece = 0; piece < 5; piece++){
					int iTmp = _jni.getNumCaptured(turn, piece);
					if (_arrImageCaptured[turn][piece] != null) {
						_arrImageCaptured[turn][piece].setVisibility(iTmp > 0 ? View.VISIBLE : View.INVISIBLE);
						_arrTextCaptured[turn][piece].setText(iTmp > 1 ? "" + iTmp : "");		
					}
				}
			}
		}
		int state = _jni.getState();
		int res = chessStateToR(state);
		turn = _jni.getTurn();
		
		if(turn == BoardConstants.WHITE){
			if(_view.getFlippedBoard()){
				if(_switchTurnOpp != null) _switchTurnOpp.setVisibility(View.VISIBLE);
				if(_switchTurnOpp != null) _switchTurnOpp.setDisplayedChild(1);
				if(_switchTurnMe != null)  _switchTurnMe.setVisibility(View.INVISIBLE);
			} else {
				if(_switchTurnMe != null)  _switchTurnMe.setVisibility(View.VISIBLE);
				if(_switchTurnMe != null)  _switchTurnMe.setDisplayedChild(1);
				if(_switchTurnOpp != null) _switchTurnOpp.setVisibility(View.INVISIBLE);
			}
		} else {
			if(_view.getFlippedBoard()){
				if(_switchTurnMe != null)  _switchTurnMe.setVisibility(View.VISIBLE);
				if(_switchTurnMe != null)  _switchTurnMe.setDisplayedChild(0);
				if(_switchTurnOpp != null) _switchTurnOpp.setVisibility(View.INVISIBLE);
				
			} else {
				if(_switchTurnOpp != null) _switchTurnOpp.setVisibility(View.VISIBLE);
				if(_switchTurnOpp != null) _switchTurnOpp.setDisplayedChild(0);
				if(_switchTurnMe != null)  _switchTurnMe.setVisibility(View.INVISIBLE);
			}
		}
		
		if(	turn == BoardConstants.WHITE && _view.getFlippedBoard() == false ||
			turn == BoardConstants.BLACK && _view.getFlippedBoard() == true	){
			
			if(state == BoardConstants.PLAY){
				if(_tvTitleMe != null) _tvTitleMe.setText(getMyName());
			} else {
				if(_tvTitleMe != null) _tvTitleMe.setText(String.format(_parent.getString(R.string.msg_state_format), _parent.getString(res)));
			}
			if(_tvTitleOpp != null) _tvTitleOpp.setText(getOppName());
		} else {
			if(state == BoardConstants.PLAY){
				if(_tvTitleOpp != null) _tvTitleOpp.setText(getOppName());
			} else {
				if(_tvTitleOpp != null) _tvTitleOpp.setText(String.format(_parent.getString(R.string.msg_state_format), _parent.getString(res)));
			}
			if(_tvTitleMe != null) _tvTitleMe.setText(getMyName());
		}
		
		if(_seekBar != null)
		{
			_seekBar.setMax(_arrPGN.size());
			_seekBar.setProgress(_jni.getNumBoard()-1);
		}
		
		//_imgTurnOpp.setImageResource(R.drawable.emo_im_surprised);
		
		//if(_tvEngineValue != null)
		//	_tvEngineValue.setText("BoardValue " + _jni.getBoardValue());
		
		//
		String sECO = getECOInfo(0, _jArrayECO);
		Log.i("ChessView-ECO", sECO == null ? "No ECO" : sECO);
		if(sECO != null && (_sPrevECO != null && !_sPrevECO.equals(sECO)) || _sPrevECO == null){
			if(sECO != null && sECO.trim().length() > 0){
                _btnCurrentOpening.setEnabled(true);
                _btnCurrentOpening.setText(sECO);
//				UiUtils.doToast(_parent, sECO);
			}
            //TODO: disable button when no opening
//		} else {
//            _btnCurrentOpening.setEnabled(false);
//            _btnCurrentOpening.setText("No Opening");
        }
		_sPrevECO = sECO;
	}
	
	
	private String getECOInfo(int level, JSONArray jArray){
		if(level < _arrPGN.size()){
			PGNEntry entry = _arrPGN.get(level);
			try {
				for(int i = 0; i < jArray.length(); i++){
					JSONObject jObj = (JSONObject)jArray.get(i);
					if(jObj.get("m").equals(entry._sMove)){
						
						String sCurrent = jObj.getString("e") + ": " + jObj.getString("n") + (jObj.getString("v").length() > 0 ? ", " + jObj.getString("v") : "");
						String sNext = null;
						
						if(jObj.has("a")){
							sNext = getECOInfo(level + 1, jObj.getJSONArray("a"));
						}
						if(sNext == null){
							return sCurrent;
						}
						return sNext;
					}
				}
			} catch(Exception ex){
				
			}
		}
		return null;
	}
	
	public String getMyName(){
		if(_view.getFlippedBoard())
			return getBlack();
		return getWhite();
	}
	public String getOppName(){
		if(_view.getFlippedBoard())
			return getWhite();
		return getBlack();
	}
	
	public void scrollToEnd(){
		if(_hScrollHistory != null){
			_hScrollHistory.post(new Runnable() {
	            public void run() {
	            	_hScrollHistory.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
	            }
	
	        }); 
		} else if(_vScrollHistory != null){
			_vScrollHistory.post(new Runnable() {
	            public void run() {
	            	_vScrollHistory.fullScroll(ScrollView.FOCUS_DOWN);
	            }
	
	        });
		}
	}
	public void scrollToStart(){
		if(_hScrollHistory != null){
			_hScrollHistory.post(new Runnable() {
	            public void run() {
	            	_hScrollHistory.fullScroll(HorizontalScrollView.FOCUS_LEFT);
	            }
	
	        }); 
		} else if(_vScrollHistory != null){
			_vScrollHistory.post(new Runnable() {
	            public void run() {
	            	_vScrollHistory.fullScroll(ScrollView.FOCUS_UP);
	            }
	
	        });
		}
	}
	public boolean hasVerticalScroll(){
		return (_vScrollHistory != null);
	}
	
	protected void dpadFirst(){
		if(_dpadPos == -1){
			_dpadPos = _jni.getTurn() == BoardConstants.BLACK ? BoardConstants.e8 : BoardConstants.e1;  
		}
	}
	public void dpadUp(){
		dpadFirst();
		if(_view.getFlippedBoard()){
			if(_dpadPos < 55){
				_dpadPos += 8;
				paintBoard();
			}
		} else {
			if(_dpadPos > 8){
				_dpadPos -= 8;
				paintBoard();
			}
		}
	}
	public void dpadDown(){
		dpadFirst();
		if(_view.getFlippedBoard()){
			if(_dpadPos > 8){
				_dpadPos -= 8;
				paintBoard();
			}
		} else {
			if(_dpadPos < 55){
				_dpadPos += 8;
				paintBoard();
			}
		}
	}
	public void dpadLeft(){
		dpadFirst();
		if(_view.getFlippedBoard()){
			if(_dpadPos < 63){
				_dpadPos++;
				paintBoard();
			}
		} else {
			if(_dpadPos > 1){
				_dpadPos--;
				paintBoard();
			}
		}
	}
	public void dpadRight(){
		dpadFirst();
		if(_view.getFlippedBoard()){
			if(_dpadPos > 1){
				_dpadPos--;
				paintBoard();
			}
		} else {
			if(_dpadPos < 63){
				_dpadPos++;
				paintBoard();
			}
		}
	}
	public void dpadSelect(){
		if(_dpadPos != -1){
			if(m_iFrom == -1){
				m_iFrom = _dpadPos;
				paintBoard();
			}
			else{ 
				if(_view.getFlippedBoard()){
					handleClick(_view.getFieldIndex(_dpadPos));
				} else {
					handleClick(_dpadPos);
				}
			}
			
		}
	}
	
	@Override
	public void playNotification(){
		int move = _jni.getMyMove();
		
		int from = Move.getFrom(move);
		int to = Move.getTo(move);
		int piece = Move.getPromotionPiece(move);
		
		String sMove = 	Pos.colToString(from).toUpperCase() + 
						". " + Pos.rowToString(from) + 
						". " + Pos.colToString(to).toUpperCase() + 
						". " + Pos.rowToString(to);
		if(piece != 0){
			sMove += " promote to ";
		}
//		_parent.soundNotification(sMove);
	}
}