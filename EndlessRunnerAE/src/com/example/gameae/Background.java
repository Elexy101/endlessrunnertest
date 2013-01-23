package com.example.gameae;

import org.andengine.engine.Engine;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.AutoParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureManager;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.vbo.VertexBufferObjectManager;

import android.content.Context;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;

public class Background extends BitmapTextureAtlas {
	private TextureRegion mParallaxLayer, mParallaxLayerFront,
			mParallaxLayerMid, mParallaxLayerBack;
	BitmapTextureAtlas mAutoParallaxBackgroundTexture;
	ITextureRegion mMountainTextureRegion, mCloudTextureRegion,
			mFloorTextureRegion;
	Sprite mountainsSprite, cloudSprite, floorSprite;
	Body floorBody;
	AutoParallaxBackground autoParallaxBackground;
	public boolean isBackgroundStopped;

	public Background(TextureManager pTextureManager, int pWidth, int pHeight,
			ITexture clouds, ITexture floor, ITexture mountains,
			VertexBufferObjectManager vob, Scene scene, Context ctx,
			Engine mEngine, PhysicsWorld mPhysicsWorld, int CAMERA_HEIGHT) {
		super(pTextureManager, pWidth, pHeight);
		isBackgroundStopped = false;
		mAutoParallaxBackgroundTexture = new BitmapTextureAtlas(
				pTextureManager, 1024, 618);
		mEngine.getTextureManager().loadTexture(mAutoParallaxBackgroundTexture);
		this.mParallaxLayerFront = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mAutoParallaxBackgroundTexture, ctx,
						"floor.png", 0, 0);
		this.mParallaxLayerBack = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mAutoParallaxBackgroundTexture, ctx,
						"cloud.png", 0, 0);
		this.mParallaxLayerMid = BitmapTextureAtlasTextureRegionFactory
				.createFromAsset(this.mAutoParallaxBackgroundTexture, ctx,
						"mountain.png", 0, 0);

		this.mCloudTextureRegion = TextureRegionFactory
				.extractFromTexture(clouds);
		this.mFloorTextureRegion = TextureRegionFactory
				.extractFromTexture(floor);
		this.mMountainTextureRegion = TextureRegionFactory
				.extractFromTexture(mountains);
		// -------------------------------------------------------------//
		// ----------------------- BACKGROUND ---------------------------//
		mountainsSprite = new Sprite(0, 0, mMountainTextureRegion, vob);
		cloudSprite = new Sprite(0, 0, mCloudTextureRegion, vob);
		floorSprite = new Sprite(0, CAMERA_HEIGHT, mFloorTextureRegion, vob);

		autoParallaxBackground = new AutoParallaxBackground(0, 0, 0, 5);
		// autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(0.0f,
		// new Sprite(0, CAMERA_HEIGHT-50, this.mParallaxLayerBack,
		// getVertexBufferObjectManager())));
		// autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-5.0f,
		// new Sprite(0, CAMERA_HEIGHT-50, this.mParallaxLayerMid,
		// getVertexBufferObjectManager())));
		// autoParallaxBackground.attachParallaxEntity(new
		// ParallaxEntity(-10.0f,
		// new Sprite(0, CAMERA_HEIGHT-50, this.mParallaxLayerFront,
		// getVertexBufferObjectManager())));

		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-1.0f,
				cloudSprite));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-6.0f,
				mountainsSprite));
		autoParallaxBackground.attachParallaxEntity(new ParallaxEntity(-40.0f,
				floorSprite));

		// autoParallaxBackground.attachParallaxEntity(new
		// ParallaxEntity(-25.0f,
		// new Sprite(0, mCamera.getHeight()
		// - this.mParallaxLayer.getHeight(), this.mParallaxLayer,
		// this.getVertexBufferObjectManager())));
		scene.setBackground(autoParallaxBackground);
		// ----------------------- BACKGROUND ---------------------------//
		// -------------------------------------------------------------//
		createFloorPhysics(mPhysicsWorld);
	}

	public void createFloorPhysics(PhysicsWorld mPhysicsWorld) {
		FixtureDef areaFixtureDef = PhysicsFactory.createFixtureDef(10, 0, 10);
		areaFixtureDef.isSensor = true;
		floorBody = PhysicsFactory.createBoxBody(mPhysicsWorld, floorSprite,
				BodyType.DynamicBody, areaFixtureDef);
		floorBody.setLinearVelocity(0, 0);
		floorBody.setUserData("Ground");
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				floorSprite, floorBody, false, false));
		final PhysicsHandler physicsHandlerCactus = new PhysicsHandler(floorSprite);
		floorSprite.registerUpdateHandler(physicsHandlerCactus);
	}

	public void stopBackgroundMovement() {
		this.autoParallaxBackground.setParallaxChangePerSecond(0);
		isBackgroundStopped = true;
	}
	
	public void restarBackgroundMovement(){
		this.autoParallaxBackground.setParallaxChangePerSecond(5);
		isBackgroundStopped = false;
	}
}
