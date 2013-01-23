package com.example.gameae;

import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.IEntity;
import org.andengine.entity.modifier.EntityModifier;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.modifier.IModifier;
import org.andengine.util.modifier.IModifier.DeepCopyNotSupportedException;

import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class Cactus extends Sprite {
	Body body;
	float velocityX, velocityY;
	float cactusX, cactusY;
	int CAMERA_HEIGHT, CAMERA_WIDTH;
	Sprite cactus;
	PhysicsWorld mPhysicsWorld;
	int mcactusMJCount = 0;
	ITextureRegion cactusTextureRegion;
	VertexBufferObjectManager pTiledSpriteVertexBufferObject;
	Scene scene;
	Body groundBody;
	float pixelToMeteRatio = 64;
	MouseJoint bodyMJ;
	float lastY;
	boolean touching = false;

	public Cactus(float pX, float pY, float pWidth, float pHeight,
			ITextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pTiledSpriteVertexBufferObject,
			final PhysicsWorld mPhysicsWorld, Scene scene,
			final int CAMERA_HEIGHT, int CAMERA_WIDTH) {
		super(pX, pY, pTiledTextureRegion, pTiledSpriteVertexBufferObject);
		this.cactusX = pX;
		this.cactusY = pY;
		cactusTextureRegion = pTiledTextureRegion;
		this.pTiledSpriteVertexBufferObject = pTiledSpriteVertexBufferObject;
		this.scene = scene;
		cactus = new Sprite(pX, pY, pTiledTextureRegion,
				getVertexBufferObject());

		IUpdateHandler upd = new IUpdateHandler() {

			@Override
			public void reset() {
				// TODO Auto-generated method stub

			}

			@Override
			public void onUpdate(float pSecondsElapsed) {
				// playerX = 100;
				// playerY = CAMERA_HEIGHT - 140;
				updatePosition(null, (float) 100, (float) CAMERA_HEIGHT - 140);

			}
		};

		cactus.registerUpdateHandler(upd);

		FixtureDef areaFixtureDef = PhysicsFactory
				.createFixtureDef(20, 0, 0.5f);
		// areaFixtureDef.isSensor = true;
		body = PhysicsFactory.createBoxBody(mPhysicsWorld, cactus,
				BodyType.DynamicBody, areaFixtureDef);
		body.setFixedRotation(true);
		cactus.setUserData("Cactus");

		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(cactus,
				body, true, true));
		scene.attachChild(cactus);
		groundBody = PhysicsFactory.createBoxBody(mPhysicsWorld, cactus,
				BodyType.DynamicBody, areaFixtureDef);

		this.mPhysicsWorld = mPhysicsWorld;
		lastY = cactus.getY();
		this.CAMERA_HEIGHT = CAMERA_HEIGHT;
		this.CAMERA_WIDTH = CAMERA_WIDTH;

	}

	public void updatePosition(TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

		final float widthD2 = cactus.getWidth() / 2;
		final float heightD2 = cactus.getHeight() / 2;
		int PIXEL_TO_METER_RATIO_DEFAULT = 32;
		final float angle = body.getAngle(); // keeps the
												// body
												// angle
		float xCoord = (cactusX + widthD2) / PIXEL_TO_METER_RATIO_DEFAULT;
		float YCoord = (pTouchAreaLocalY + heightD2)
				/ PIXEL_TO_METER_RATIO_DEFAULT;

		movecactus(pTouchAreaLocalY, xCoord, YCoord, angle);

	}

	private void movecactus(float pTouchAreaLocalY, float xCoord, float YCoord,
			float angle) {

		final Vector2 v2 = Vector2Pool.obtain(xCoord, YCoord);
		body.setTransform(v2, angle);
		Vector2Pool.recycle(v2);
		lastY = cactus.getY();
		touching = true;

	}

	public MouseJoint createMouseJoint(float x, float y) {
		Vector2 position = body.getWorldPoint(new Vector2(x
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, y
				/ PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT));

		MouseJointDef jointDef = new MouseJointDef();
		jointDef.bodyA = groundBody;
		jointDef.bodyB = body;
		jointDef.dampingRatio = 0.2f;
		jointDef.frequencyHz = 30;
		jointDef.maxForce = (float) (200.0f * body.getMass());
		jointDef.collideConnected = true;
		jointDef.target.set(position);

		return (MouseJoint) mPhysicsWorld.createJoint(jointDef);
		// Vector2 vector = new Vector2(x / pixelToMeteRatio, y /
		// pixelToMeteRatio);
		// BodyDef groundBodyDef = new BodyDef();
		// groundBodyDef.position.set(x, y);
		// // groundBodyDef.position.set(vector);
		// groundBody = mPhysicsWorld.createBody(groundBodyDef);
		// // ====================================
		// // final Body boxBody =
		// this.mPhysicsWorld.getPhysicsConnectorManager()
		// // .findBodyByShape(cactus);
		// final Body boxBody = (Body) bodyMJ.getUserData();
		// // Vector2 v = boxBody.getWorldPoint(new Vector2(x /
		// pixelToMeteRatio, y
		// // / pixelToMeteRatio));
		//
		// final MouseJointDef mouseJointDef = new MouseJointDef();
		//
		// final Vector2 localPoint = Vector2Pool.obtain(x, y);
		// Vector2Pool.recycle(localPoint);
		// body.setTransform(localPoint, y);
		//
		// mouseJointDef.bodyA = groundBody;
		// mouseJointDef.bodyB = boxBody;
		// mouseJointDef.dampingRatio = 1f;
		// mouseJointDef.frequencyHz = 30;
		// mouseJointDef.maxForce = (float) (200.0f * boxBody.getMass());
		// mouseJointDef.collideConnected = true;
		// mouseJointDef.target.set(x, y);
		// // mouseJointDef.target.set(v);
		// return (MouseJoint) this.mPhysicsWorld.createJoint(mouseJointDef);
	}

	// public void addcactusMJ(final float pX, final float pY) {
	// this.mcactusMJCount++;
	// Debug.d("cactusMJs: " + this.mcactusMJCount);
	//
	// final AnimatedSprite cactusMJ;
	// final Body body;
	// FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);
	//
	// if (this.mcactusMJCount % 2 == 0) {
	// cactusMJ = new AnimatedSprite(cactusX, cactusY,
	// cactusTextureRegion, pTiledSpriteVertexBufferObject);
	// body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, cactusMJ,
	// BodyType.DynamicBody, FIXTURE_DEF);
	// } else {
	// cactusMJ = new AnimatedSprite(cactusX, cactusY,
	// cactusTextureRegion, pTiledSpriteVertexBufferObject);
	// body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, cactusMJ,
	// BodyType.DynamicBody, FIXTURE_DEF);
	// }
	// cactusMJ.setUserData(body);
	// cactusMJ.animate(200);
	//
	// this.scene.registerTouchArea(cactusMJ);
	// this.scene.attachChild(cactusMJ);
	//
	// this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
	// cactusMJ, body, true, true));
	// }

	// ===========================================================
	// Methods
	// ===========================================================

}