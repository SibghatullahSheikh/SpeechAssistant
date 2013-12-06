package jp.bizenkou.karakama.speechassistant;

import jp.bizenkou.karakama.speechassistant.R;
import jp.bizenkou.karakama.speechassistant.min3d.Shared;
import jp.bizenkou.karakama.speechassistant.min3d.Utils;
import jp.bizenkou.karakama.speechassistant.min3d.core.Object3dContainer;
import jp.bizenkou.karakama.speechassistant.min3d.core.RendererActivity;
import jp.bizenkou.karakama.speechassistant.min3d.objectPrimitives.Rectangle;
import jp.bizenkou.karakama.speechassistant.min3d.parser.IParser;
import jp.bizenkou.karakama.speechassistant.min3d.parser.Parser;
import jp.bizenkou.karakama.speechassistant.min3d.vos.Color4;
import jp.bizenkou.karakama.speechassistant.min3d.vos.Light;
import android.graphics.Bitmap;

public class ExampleFog extends RendererActivity {
	private Object3dContainer objModel;

	@Override
	public void initScene() {
		Light light = new Light();
		scene.lights().add(light);
		scene.camera().position.x = 0;
		scene.camera().position.y = 0;
		scene.camera().position.z = 10;

		Bitmap b = Utils.makeBitmapFromResourceId(R.drawable.barong);
		Shared.textureManager().addTextureId(b, "barong", false);
		b.recycle();

		b = Utils.makeBitmapFromResourceId(R.drawable.wood);
		Shared.textureManager().addTextureId(b, "wood", false);
		b.recycle();

		scene.lights().add(new Light());

		IParser parser = Parser
				.createParser(
						Parser.Type.OBJ,
						getResources(),
						"jp.bizenkou.karakama.SampleTypeD:raw/droid_obj",
						true);
		parser.parse();

		objModel = parser.getParsedObject();
		objModel.scale().x = objModel.scale().y = objModel.scale().z = .7f;
		scene.addChild(objModel);

		Color4 planeColor = new Color4(255, 255, 255, 255);
		Rectangle east = new Rectangle(40, 12, 2, 2, planeColor);
		Rectangle west = new Rectangle(40, 12, 2, 2, planeColor);
		Rectangle up = new Rectangle(40, 12, 2, 2, planeColor);
		Rectangle down = new Rectangle(40, 12, 2, 2, planeColor);

		east.position().x = -6;
		east.rotation().y = -90;
		east.position().z = -20;
		east.lightingEnabled(false);
		east.textures().addById("wood");

		west.position().x = 6;
		west.rotation().y = 90;
		west.position().z = -20;
		west.lightingEnabled(false);
		west.textures().addById("wood");

		up.rotation().x = -90;
		up.rotation().z = 90;
		up.position().y = 6;
		up.position().z = -20;
		up.lightingEnabled(false);
		up.textures().addById("wood");

		down.rotation().x = 90;
		down.rotation().z = 90;
		down.position().y = -6;
		down.position().z = -20;
		down.lightingEnabled(false);
		down.textures().addById("wood");

		scene.addChild(east);
		scene.addChild(west);
		scene.addChild(up);
		scene.addChild(down);

		scene.fogColor(new Color4(0, 0, 0, 255));
		scene.fogNear(10);
		scene.fogFar(40);
		scene.fogEnabled(true);
	}

	@Override
	public void updateScene() {
		objModel.position().z += .25;
//		objModel.rotation().x++;
//		objModel.rotation().y++;
		if (objModel.position().z > scene.camera().position.z)
			objModel.position().z = -40;
	}
}