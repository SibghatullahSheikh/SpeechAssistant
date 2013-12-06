package jp.bizenkou.karakama.speechassistant;

import jp.bizenkou.karakama.speechassistant.R;
import jp.bizenkou.karakama.speechassistant.MainActivity.fsrController;
import jp.bizenkou.karakama.speechassistant.min3d.Shared;
import jp.bizenkou.karakama.speechassistant.min3d.Utils;
import jp.bizenkou.karakama.speechassistant.min3d.core.Object3dContainer;
import jp.bizenkou.karakama.speechassistant.min3d.core.RendererActivity;
import jp.bizenkou.karakama.speechassistant.min3d.objectPrimitives.SkyBox;
import jp.bizenkou.karakama.speechassistant.min3d.parser.IParser;
import jp.bizenkou.karakama.speechassistant.min3d.parser.Parser;
import jp.bizenkou.karakama.speechassistant.min3d.vos.Light;
import jp.bizenkou.karakama.speechassistant.min3d.vos.LightType;
import jp.bizenkou.karakama.speechassistant.min3d.vos.Number3d;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class AndroidRobot3DView extends RendererActivity implements
		View.OnClickListener, SensorEventListener {

	private final float FILTERING_FACTOR = .3f;

	private SkyBox mSkyBox;
	private SensorManager mSensorManager;
	private Sensor mAccelerometer;
	private Sensor mProximity;
	private Number3d mAccVals;

	private Object3dContainer androidRobot3DObject;
	private Light myLight = new Light();
	private int mOrientation = Configuration.ORIENTATION_LANDSCAPE;
	private float OUT_POSITION = 6f;
	private float IN_POSITION = 0f;
	public float mTargetPosition = OUT_POSITION;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		mAccelerometer = mSensorManager
				.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		mProximity = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);

		mAccVals = new Number3d();

		mOrientation = this.getResources().getConfiguration().orientation;

	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		mSensorManager.unregisterListener(this);
	}

	/** Called when the activity is first created. */
	@Override
	public void initScene() {
		myLight.position.setAll(300, 150, 150);

		myLight = new Light();
		myLight.type(LightType.DIRECTIONAL);

		scene.lights().add(myLight);
		scene.backgroundColor().setAll(255, 255, 255, 1);

		mSkyBox = new SkyBox(5.0f, 2);
		mSkyBox.addTexture(SkyBox.Face.North, R.drawable.wood_back, "north");
		mSkyBox.addTexture(SkyBox.Face.East, R.drawable.wood_right, "east");
		mSkyBox.addTexture(SkyBox.Face.South, R.drawable.wood_back, "south");
		mSkyBox.addTexture(SkyBox.Face.West, R.drawable.wood_left, "west");
		mSkyBox.addTexture(SkyBox.Face.Up, R.drawable.ceiling, "up");
		mSkyBox.addTexture(SkyBox.Face.Down, R.drawable.floor, "down");
		mSkyBox.scale().y = 0.8f;
		mSkyBox.scale().z = 2.0f;
		scene.addChild(mSkyBox);

		IParser myParser = Parser.createParser(Parser.Type.MAX_3DS,
				getResources(),
				"jp.bizenkou.karakama.SampleTypeD:raw/squared_robot_3ds", false);
		myParser.parse();

		androidRobot3DObject = myParser.getParsedObject();
		androidRobot3DObject.rotation().y = 270;

		// We scale the robot, check this value if you want to target another
		// scale
		androidRobot3DObject.scale().x = androidRobot3DObject.scale().y = androidRobot3DObject
				.scale().z = .75f;
		androidRobot3DObject.position().y = -0.75f;
		androidRobot3DObject.position().z = mTargetPosition;
		scene.addChild(androidRobot3DObject);

		// Preloading the textures with the textureManager()
		Bitmap b;
		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business);
		Shared.textureManager().addTextureId(b, "squared_robot_body_business");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_1);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_1");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_2);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_2");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_3);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_3");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_4);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_4");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_5);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_5");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_6);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_6");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_7);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_7");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_8);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_8");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_9);
		Shared.textureManager()
				.addTextureId(b, "squared_robot_body_business_9");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_10);
		Shared.textureManager().addTextureId(b,
				"squared_robot_body_business_10");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_body_business_11);
		Shared.textureManager().addTextureId(b,
				"squared_robot_body_business_11");
		b.recycle();

		b = Utils.makeBitmapFromResourceId(R.drawable.squared_robot_arm);
		Shared.textureManager().addTextureId(b, "squared_robot_arm");
		b.recycle();

		b = Utils.makeBitmapFromResourceId(R.drawable.squared_robot_foot);
		Shared.textureManager().addTextureId(b, "squared_robot_foot");
		b.recycle();

		b = Utils.makeBitmapFromResourceId(R.drawable.squared_robot_antenna);
		Shared.textureManager().addTextureId(b, "squared_robot_antenna");
		b.recycle();

		b = Utils.makeBitmapFromResourceId(R.drawable.squared_robot_head);
		Shared.textureManager().addTextureId(b, "squared_robot_head");
		b.recycle();

		b = Utils
				.makeBitmapFromResourceId(R.drawable.squared_robot_head_business);
		Shared.textureManager().addTextureId(b, "squared_robot_head_business");
		b.recycle();

		loadAllTextures();

		mSensorManager.registerListener(this, mAccelerometer,
				SensorManager.SENSOR_DELAY_UI);
		mSensorManager.registerListener(this, mProximity,
				SensorManager.SENSOR_DELAY_UI);

	}

	/** Updates child and their textures with the textures to be loaded */
	public void loadAllTextures() {
		int numChildren = androidRobot3DObject.numChildren();
		for (int i = 0; i < numChildren; i++) {
			String name = androidRobot3DObject.getChildAt(i).name();

			// The name is either extracted from the _mtl file
			// or directly from the *.3ds file
			// The name can be given directly into Blender
			if (name.indexOf("body") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById("squared_robot_body_business");
			}

			if (name.indexOf("head") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById("squared_robot_head");
			}

			if (name.indexOf("foot") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById("squared_robot_foot");
			}

			if (name.indexOf("arm") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById("squared_robot_arm");
			}

			if (name.indexOf("antenna") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById("squared_robot_antenna");
			}
		}

	}

	/** Changing the body texture of the robot */
	public void changeBodyTexture(String aTextureName) {
		// If you want, you can target a specific part of the robot
		// and only change the texture of this part by using the
		// getChildByName("")
		/*
		 * Passing through all the children of the robot and applying a new
		 * texture on them
		 */
		for (int i = 0; i < androidRobot3DObject.numChildren(); i++) {
			String name = androidRobot3DObject.getChildAt(i).name();

			if (name.indexOf("body") != -1) {
				androidRobot3DObject.getChildAt(i).textures().clear();
				androidRobot3DObject.getChildAt(i).textures()
						.addById(aTextureName);
			}
		}
	}

	/** Changes the texture of the head */
	public void changeHeadTexture(String aTextureName) {
		if (androidRobot3DObject.getChildByName("head") != null) {
			androidRobot3DObject.getChildByName("head").textures().removeAll();
			androidRobot3DObject.getChildByName("head").textures()
					.addById(aTextureName);
		}
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		{
			if (event.getAction() == MotionEvent.ACTION_UP) {

				onSpeechStart(null);
			}
		}
		return true;

	}

	public void changeTextureFromLevel(int level) {
		switch (level) {
		case 0:
			changeBodyTexture("squared_robot_body_business");
			break;
		case 1:
			changeBodyTexture("squared_robot_body_business_1");
			break;
		case 2:
			changeBodyTexture("squared_robot_body_business_2");
			break;
		case 3:
			changeBodyTexture("squared_robot_body_business_3");
			break;
		case 4:
			changeBodyTexture("squared_robot_body_business_4");
			break;
		case 5:
			changeBodyTexture("squared_robot_body_business_5");
			break;
		case 6:
			changeBodyTexture("squared_robot_body_business_6");
			break;
		case 7:
			changeBodyTexture("squared_robot_body_business_7");
			break;
		case 8:
			changeBodyTexture("squared_robot_body_business_8");
			break;
		case 9:
			changeBodyTexture("squared_robot_body_business_9");
			break;
		case 10:
			changeBodyTexture("squared_robot_body_business_10");
			break;
		case 11:
			changeBodyTexture("squared_robot_body_business_11");
			break;
		default:
			changeBodyTexture("squared_robot_body_business");
			break;
		}
	}

	/**
	 * 開始ボタン押下
	 * 
	 * @param view
	 *            ビュー
	 */
	public void onSpeechStart(final View view) {
		mTargetPosition = IN_POSITION;
	}

	/**
	 * 開始ボタン押下
	 * 
	 * @param view
	 *            ビュー
	 */
	public void onSpeechEnd(final View view) {
		mTargetPosition = OUT_POSITION;
	}

	@Override
	public void updateScene() {

		// androidRobot3DObject.rotation().y += rotationDirection;
		float positionZ = androidRobot3DObject.position().z;
		if (positionZ == mTargetPosition) {
			float rotationY = androidRobot3DObject.rotation().y;
			if (IN_POSITION == mTargetPosition) {
				if (rotationY == 90) {

				} else if (rotationY > 90) {
					androidRobot3DObject.rotation().y = (rotationY - 5) < 90 ? 90
							: (rotationY - 5);
				}
			} else {
				if (rotationY == 270) {
				} else if (rotationY < 270) {
					androidRobot3DObject.rotation().y = (rotationY + 5) > 270 ? 270
							: (rotationY + 5);
				}
			}
		} else {
			if (positionZ > mTargetPosition) {
				androidRobot3DObject.position().z = (positionZ - 0.1f) < mTargetPosition ? mTargetPosition
						: (positionZ - 0.1f);
			} else if (positionZ < mTargetPosition) {
				androidRobot3DObject.position().z = (positionZ + 0.1f) > mTargetPosition ? mTargetPosition
						: (positionZ + 0.1f);
			}
		}
		myLight.position.setZ(myLight.position.getZ() + 1);
	}

	@Override
	public void onClick(View v) {
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
	}

	private boolean isFirstTime = true;

	@Override
	public void onSensorChanged(SensorEvent event) {
		if (Sensor.TYPE_ACCELEROMETER == event.sensor.getType()) {

			if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {// 横状態
				mAccVals.x = (float) (-event.values[1] * FILTERING_FACTOR + mAccVals.x
						* (1.0 - FILTERING_FACTOR));
				mAccVals.y = (float) (event.values[0] * FILTERING_FACTOR + mAccVals.y
						* (1.0 - FILTERING_FACTOR));
			} else {
				mAccVals.x = (float) (-event.values[0] * FILTERING_FACTOR + mAccVals.x
						* (1.0 - FILTERING_FACTOR));
				mAccVals.y = (float) (event.values[1] * FILTERING_FACTOR + mAccVals.y
						* (1.0 - FILTERING_FACTOR));
			}

			scene.camera().position.x = mAccVals.x * .1f;
			scene.camera().position.y = mAccVals.y * .1f;
			scene.camera().target.x = -scene.camera().position.x;
			scene.camera().target.y = -scene.camera().position.y;
		} else if (Sensor.TYPE_PROXIMITY == event.sensor.getType()) {
			if (event.values[0] > 0 && !isFirstTime) {
				onSpeechStart(null);
			}else if(event.values[0] > 0 && isFirstTime){
				isFirstTime = false;
			}
		}
	}
}