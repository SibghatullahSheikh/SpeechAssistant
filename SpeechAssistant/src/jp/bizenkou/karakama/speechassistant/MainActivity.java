package jp.bizenkou.karakama.speechassistant;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Locale;

import jp.bizenkou.karakama.speechassistant.R;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewPropertyAnimator;
import android.view.animation.DecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fuetrek.fsr.FSRServiceEventListener;
import com.fuetrek.fsr.FSRServiceOpen;
import com.fuetrek.fsr.FSRServiceEnum.BackendType;
import com.fuetrek.fsr.FSRServiceEnum.EventType;
import com.fuetrek.fsr.FSRServiceEnum.Ret;
import com.fuetrek.fsr.entity.AbortInfoEntity;
import com.fuetrek.fsr.entity.ConstructorEntity;
import com.fuetrek.fsr.entity.RecognizeEntity;
import com.fuetrek.fsr.entity.ResultInfoEntity;
import com.fuetrek.fsr.entity.StartRecognitionEntity;
import com.fuetrek.fsr.exception.AbortException;
import com.fuetrek.fsr.exception.OperationException;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

class SyncObj {
	boolean isDone = false;

	synchronized void wait_() {
		try {
			// wait_()より前にnotify_()が呼ばれた場合の対策としてisDoneフラグをチェックしている
			while (isDone == false) {
				wait(1000);
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	synchronized void notify_() {
		isDone = true;
		notify();
	}
}

public class MainActivity extends AndroidRobot3DView implements
		TextToSpeech.OnInitListener {

	private TextToSpeech mTts;

	private Handler handler_;
	// private Button buttonStart_;
	// private ProgressBar progressLevel_;
	// private TextView textResult_;
	private fsrController controller_ = new fsrController();

	// BackendTypeはBackendType.D固定
	private static final BackendType backendType_ = BackendType.D;

	// Context
	private Activity activity_ = null;
	private FSRServiceOpen fsr_;

	// FSRServiceの待ち処理でブロッキングする実装としている為、
	// UI更新を妨げないよう別スレッドとしている。
	public class fsrController extends Thread implements
			FSRServiceEventListener {
		public boolean isStarted = false;
		public boolean isEnd = false;

		SyncObj event_CompleteConnect_ = new SyncObj();
		SyncObj event_CompleteDisconnect_ = new SyncObj();
		SyncObj event_EndRecognition_ = new SyncObj();
		Ret ret_;
		String result_;

		// 認識完了時の処理
		// (UIスレッドで動作させる為にRunnable()を使用している)
		final Runnable notifyFinished = new Runnable() {
			public void run() {
				try {
					// 念のためスレッドの完了を待つ
					controller_.join();
				} catch (InterruptedException e) {
				}

				if (mPostJson == null) {
					mPostJson = new Post();
				}
				mPostJson.utt = controller_.result_;
				showToast(mPostJson.utt);
				isEnd = true;
				PostFetcher fetcher = new PostFetcher();
				fetcher.execute();

				// textResult_.append("***Result***"
				// + System.getProperty("line.separator"));
				// textResult_.append(controller_.result_);
				// buttonStart_.setEnabled(true);

			}
		};

		// 認識処理
		@Override
		public void run() {
			result_ = "";
			try {
				result_ = execute();
			} catch (Exception e) {
				onSpeechEnd(null);
				result_ = "(error)";
				e.printStackTrace();
			}
			handler_.post(notifyFinished);
		}

		/**
		 * 認識処理
		 * 
		 * 現状は毎回インスタンス生成～destroy()を実施しているが、
		 * 繰り返し認識させる場合は、以下のように制御した方がオーバーヘッドが少なくなる
		 * アプリ起動時：インスタンス生成～connectSession()
		 * 認識要求時　：startRecognition()～getSessionResult() アプリ終了時：destroy()
		 * 
		 * @throws Exception
		 */
		public String execute() throws Exception {

			try {
				isStarted = true;
				final ConstructorEntity construct = new ConstructorEntity();
				construct.setContext(activity_);

				// 別途発行されるAPIキーを設定してください(以下の値はダミーです)
				construct
						.setApiKey("51412f36432e676678353435352f6c737048624578734b4d354c4343737231727579353171507964596543");

				construct.setSpeechTime(10000);
				construct.setRecordSize(240);
				construct.setRecognizeTime(10000);

				// インスタンス生成
				// (thisは FSRServiceEventListenerをimplementsしている。)
				if (null == fsr_) {
					fsr_ = new FSRServiceOpen(this, this, construct);
				}

				// connect
				fsr_.connectSession(backendType_);
				event_CompleteConnect_.wait_();
				if (ret_ != Ret.RetOk) {
					Exception e = new Exception("filed connectSession.");
					throw e;
				}

				// 認識開始

				final StartRecognitionEntity startRecognitionEntity = new StartRecognitionEntity();
				startRecognitionEntity.setAutoStart(false);
				startRecognitionEntity.setAutoStop(true); // falseにする場合はUIからstopRecognition()実行する仕組みが必要
				startRecognitionEntity.setVadOffTime((short) 500);
				startRecognitionEntity.setListenTime(0);
				startRecognitionEntity.setLevelSensibility(10);

				// 認識開始
				fsr_.startRecognition(backendType_, startRecognitionEntity);

				mTargetPosition = 0;

				// 認識完了待ち
				// (setAutoStop(true)なので発話終了を検知して自動停止する)
				event_EndRecognition_.wait_();

				// 認識結果の取得
				RecognizeEntity recog = fsr_
						.getSessionResultStatus(backendType_);
				String result = "(no result)";
				if (recog.getCount() > 0) {
					ResultInfoEntity info = fsr_.getSessionResult(backendType_,
							1);
					result = info.getText();
				}

				// 切断
				fsr_.disconnectSession(backendType_);
				event_CompleteDisconnect_.wait_();

				return result;
			} catch (Exception e) {
				errorLog(e);
				throw e;
			} finally {
				if (fsr_ != null) {
					fsr_.destroy();
					fsr_ = null;
				}
			}
		}

		@Override
		public void notifyAbort(Object arg0, AbortInfoEntity arg1) {
			Exception e = new Exception("Abort!!");
			errorLog(e);
		}

		@Override
		public void notifyEvent(final Object appHandle,
				final EventType eventType, final BackendType backendType,
				Object eventData) {
			switch (eventType) {

			case CompleteConnect:
				// 接続完了
				ret_ = (Ret) eventData;
				event_CompleteConnect_.notify_();

				if (mPostJson != null && mPostJson.mode != null
						&& mPostJson.mode.equals("srtr")) {
					handler_.post(new Runnable() {

						@Override
						public void run() {
							mCountDownTextView.setText("のこり10秒");
							mCountDownTimer.start();
						}
					});
				}

				break;

			case CompleteDisconnect:
				// 切断完了
				event_CompleteDisconnect_.notify_();
				if (mPostJson != null && mPostJson.mode != null
						&& mPostJson.mode.equals("srtr")) {
					mCountDownTimer.cancel();
				}
				break;

			case NotifyEndRecognition:
				// 認識完了
				event_EndRecognition_.notify_();
				break;
			case CompleteStop:
				Log.w("eventType", "CompleteStop");
				break;
			case NotifyAutoStopSilence:
				Log.w("eventType", "NotifyAutoStopSilence");
				break;
			case NotifyAutoStopTimeout:
				Log.w("eventType", "NotifyAutoStopTimeout");
				try {
					fsr_.cancelRecognition();
				} catch (AbortException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (OperationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				fsr_ = null;
				mPostJson = null;
				onSpeechEnd(null);
				break;
			case NotifyLevel:
				// レベルメータ更新
				int level = (Integer) eventData;
				// progressLevel_.setProgress(level);
				Log.w("NotifyLevel", "level:" + level);
				changeTextureFromLevel(level > 11 ? 11 : level);
				changeWaveFromLevel(level > 11 ? 11 : level);
				break;
			default:
				break;
			}
		}

		private void changeWaveFromLevel(int level) {
			for (int i = 0; i < level; i++) {
				if (View.GONE == mWaveViews[i].getVisibility()) {
					animateWave(mWaveViews[i], i);
				}
			}
		}

		private void animateWave(final ImageView imageView, final int level) {
			handler_.post(new Runnable() {

				@Override
				public void run() {
					imageView.setTranslationY(mHeight);
					imageView.setAlpha(1f);
					imageView.setScaleX(1f);
					imageView.setVisibility(View.VISIBLE);
					ViewPropertyAnimator animator = imageView.animate();
					animator.translationY(mHeight / 1.5f).alpha(0f).scaleX(2f)
							.setDuration(200 * (12 - level))
							.setInterpolator(new DecelerateInterpolator())
							.setListener(new AnimatorListener() {

								@Override
								public void onAnimationStart(Animator animation) {
								}

								@Override
								public void onAnimationRepeat(Animator animation) {
								}

								@Override
								public void onAnimationEnd(Animator animation) {
									imageView.setVisibility(View.GONE);
								}

								@Override
								public void onAnimationCancel(Animator animation) {
								}
							}).start();

				}
			});
		}

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		// Don't forget to shutdown!
		if (mTts != null) {
			mTts.stop();
			mTts.shutdown();
		}
		if (fsr_ != null) {
			fsr_.destroy();
			fsr_ = null;
		}

		super.onDestroy();
	}

	private ImageView mWaveView1;
	private ImageView mWaveView2;
	private ImageView mWaveView3;
	private ImageView mWaveView4;
	private ImageView mWaveView5;
	private ImageView mWaveView6;
	private ImageView mWaveView7;
	private ImageView mWaveView8;
	private ImageView mWaveView9;
	private ImageView mWaveView10;
	private ImageView mWaveView11;

	private ImageView[] mWaveViews = new ImageView[] { mWaveView1, mWaveView2,
			mWaveView3, mWaveView4, mWaveView5, mWaveView6, mWaveView7,
			mWaveView8, mWaveView9, mWaveView10, mWaveView11 };

	private TextView mCountDownTextView;
	private CountDownTimer mCountDownTimer;

	private int mHeight = 0;

	@Override
	protected void onCreateSetContentView() {
		super.onCreateSetContentView();

		setContentView(_glSurfaceView);

		mHeight = getResources().getDisplayMetrics().heightPixels;

		FrameLayout child1 = (FrameLayout) getLayoutInflater().inflate(
				R.layout.wave, null);
		mCountDownTextView = (TextView) getLayoutInflater().inflate(
				R.layout.count_down, null);

		FrameLayout decoreView = (FrameLayout) getWindow().getDecorView();
		decoreView.addView(child1);
		decoreView.addView(mCountDownTextView);
		mCountDownTextView.setText("");
		mWaveViews[0] = (ImageView) child1.findViewById(R.id.wave1);
		mWaveViews[1] = (ImageView) child1.findViewById(R.id.wave2);
		mWaveViews[2] = (ImageView) child1.findViewById(R.id.wave3);
		mWaveViews[3] = (ImageView) child1.findViewById(R.id.wave4);
		mWaveViews[4] = (ImageView) child1.findViewById(R.id.wave5);
		mWaveViews[5] = (ImageView) child1.findViewById(R.id.wave6);
		mWaveViews[6] = (ImageView) child1.findViewById(R.id.wave7);
		mWaveViews[7] = (ImageView) child1.findViewById(R.id.wave8);
		mWaveViews[8] = (ImageView) child1.findViewById(R.id.wave9);
		mWaveViews[9] = (ImageView) child1.findViewById(R.id.wave10);
		mWaveViews[10] = (ImageView) child1.findViewById(R.id.wave11);

		mCountDownTimer = new CountDownTimer(11000, 1000) {

			public void onTick(long millisUntilFinished) {
				mCountDownTextView.setText("のこり" + millisUntilFinished / 1000
						+ "秒");
			}

			public void onFinish() {
				say("時間切れ！\nあなたの負け");
				mCountDownTextView.setText("時間切れ！あなたの負け");
			}
		};
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Initialize text-to-speech. This is an asynchronous operation.
		// The OnInitListener (second argument) is called after initialization
		// completes.
		mTts = new TextToSpeech(MainActivity.this, MainActivity.this);

		handler_ = new Handler();

		activity_ = this;

		// コントロール初期化
		// progressLevel_.setMax(100);
		// textResult_.setTextSize(28.0f);

	}

	/**
	 * 開始ボタン押下
	 * 
	 * @param view
	 *            ビュー
	 */
	public void onSpeechStart(final View view) {
		// textResult_.setText("");
		// buttonStart_.setEnabled(false);
		if (controller_ != null) {
			if (fsr_ != null && controller_.isStarted && !controller_.isEnd) {
				handler_.post(new Runnable() {

					@Override
					public void run() {
						// 処理中だったら何もせず返却
						try {
							fsr_.stopRecognition();
						} catch (AbortException e) {
							e.printStackTrace();
							fsr_ = null;
							onSpeechEnd(null);
						} catch (OperationException e) {
							e.printStackTrace();
							fsr_ = null;
							onSpeechEnd(null);
						}
					}
				});

				return;
			}
		}
		controller_ = new fsrController();
		controller_.start();
		mCountDownTextView.setText("");
	}

	/**
	 * エラーログ出力
	 */
	public final void errorLog(final Exception e) {
		final String text = (e.getCause() != null) ? e.getCause().toString()
				: e.toString();
		Log.e("Exception", "" + text);

	}

	/**
	 * トーストを表示する。
	 */
	public final void showToast(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}

	@Override
	public void onInit(int status) {
		// status can be either TextToSpeech.SUCCESS or TextToSpeech.ERROR.
		if (status == TextToSpeech.SUCCESS) {
			// Set preferred language to US english.
			// Note that a language may not be available, and the result will
			// indicate this.
			int result = mTts.setLanguage(Locale.JAPAN);
			// Try this someday for some interesting results.
			// int result mTts.setLanguage(Locale.FRANCE);
			if (result == TextToSpeech.LANG_MISSING_DATA
					|| result == TextToSpeech.LANG_NOT_SUPPORTED) {
				// Lanuage data is missing or the language is not supported.
			} else {
				// Greet the user.
				say("");
			}
		} else {
			// Initialization failed.
		}
	}

	private void say(String text) {
		if (text != null && !TextUtils.isEmpty(text)) {
			mTts.speak(text, TextToSpeech.QUEUE_FLUSH, null);
			handler_.postDelayed(new Runnable() {
				@Override
				public void run() {
					onSpeechStart(null);
				}
			}, text.length() * 200);
		}
	}

	private Post mPostJson;

	private void handlePostsList(Post postJson) {
		this.mPostJson = postJson;

		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				if (fsr_ == null) {
					Toast.makeText(MainActivity.this, mPostJson.utt,
							Toast.LENGTH_LONG).show();
				} else {
					Toast.makeText(MainActivity.this,
							mPostJson.utt + " status:" + fsr_.getStatus(),
							Toast.LENGTH_LONG).show();
				}

				if (mPostJson.yomi != null
						&& !TextUtils.isEmpty(mPostJson.yomi)) {
					say(mPostJson.yomi);
				} else {
					say(mPostJson.utt);
				}
			}
		});
	}

	private void failedLoadingPosts() {
		runOnUiThread(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(MainActivity.this,
						"Failed to load Posts. Have a look at LogCat.",
						Toast.LENGTH_SHORT).show();
			}
		});
	}

	private class PostFetcher extends AsyncTask<Void, Void, String> {
		private static final String TAG = "PostFetcher";
		public static final String SERVER_URL = "https://api.apigw.smt.docomo.ne.jp/dialogue/v1/dialogue?APIKEY=51412f36432e676678353435352f6c737048624578734b4d354c4343737231727579353171507964596543";

		@Override
		protected String doInBackground(Void... params) {
			try {
				// Create an HTTP client
				HttpClient client = new DefaultHttpClient();
				HttpPost post = new HttpPost(SERVER_URL);
				post.setHeader("Content-Type",
						"application/json; charset=utf-8");
				PostRequest pr = new PostRequest();
				pr.utt = mPostJson.utt;
				if (mPostJson.context != null) {
					pr.context = mPostJson.context;
				}
				if (mPostJson.mode != null) {
					pr.mode = mPostJson.mode;
				}
				pr.nickname = "千里";
				pr.nickname_y = "チサト";
				pr.sex = "男";
				pr.bloodtype = "B";
				pr.birthdateY = 1985;
				pr.birthdateM = 11;
				pr.birthdateD = 24;
				pr.age = 28;
				pr.constellations = "射手座";
				pr.place = "東京";

				String jsonEn = new Gson().toJson(pr);
				post.setEntity(new ByteArrayEntity(jsonEn.toString().getBytes(
						"UTF8")));
				// Perform the request and check the status code
				HttpResponse response = client.execute(post);
				StatusLine statusLine = response.getStatusLine();
				if (statusLine.getStatusCode() == 200) {
					HttpEntity entity = response.getEntity();
					InputStream content = entity.getContent();

					try {
						// Read the server response and attempt to parse it as
						// JSON
						Reader reader = new InputStreamReader(content);

						GsonBuilder gsonBuilder = new GsonBuilder();
						gsonBuilder.setDateFormat("M/d/yy hh:mm a");
						Gson gson = gsonBuilder.create();
						Post postJson = gson.fromJson(reader, Post.class);
						content.close();

						handlePostsList(postJson);
					} catch (Exception ex) {
						Log.e(TAG, "Failed to parse JSON due to: " + ex);
						failedLoadingPosts();
					}
				} else {
					Log.e(TAG, "Server responded with status code: "
							+ statusLine.getStatusCode());
					failedLoadingPosts();
				}
			} catch (Exception ex) {
				Log.e(TAG, "Failed to send HTTP POST request due to: " + ex);
				failedLoadingPosts();
			}
			return null;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.activity_main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// TODO Auto-generated method stub
		int action = item.getItemId();
		switch (action) {
		case R.id.menu_siritori:
			if (mPostJson == null) {
				mPostJson = new Post();
			}
			mPostJson.utt = "しりとりしよう";
			PostFetcher fetcher = new PostFetcher();
			fetcher.execute();
			break;
		case R.id.menu_unnsei:
			if (mPostJson == null) {
				mPostJson = new Post();
			}
			mPostJson.utt = "今日の運勢は？";
			fetcher = new PostFetcher();
			fetcher.execute();
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

}
