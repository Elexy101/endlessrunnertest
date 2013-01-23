package com.example.gameae;

import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.entity.scene.IOnAreaTouchListener;
import org.andengine.entity.scene.ITouchArea;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.AnimatedSprite;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.Vector2Pool;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.texture.region.ITiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;

import android.util.Log;
import android.view.MotionEvent;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;
import com.badlogic.gdx.physics.box2d.joints.MouseJointDef;

public class Player extends AnimatedSprite {
	Body body;
	float velocityX, velocityY;
	float playerX, playerY;
	int CAMERA_HEIGHT, CAMERA_WIDTH;
	AnimatedSprite ply;
	PhysicsWorld mPhysicsWorld;
	int mplayerMJCount = 0;
	ITiledTextureRegion playerTextureRegion;
	VertexBufferObjectManager pTiledSpriteVertexBufferObject;
	Scene scene;
	Body groundBody;
	float pixelToMeteRatio = 64;
	MouseJoint bodyMJ;
	float lastY;
	boolean touching = false;
	float angleNormal;

	public Player(float pX, float pY, float pWidth, float pHeight,
			ITiledTextureRegion pTiledTextureRegion,
			VertexBufferObjectManager pTiledSpriteVertexBufferObject,
			final PhysicsWorld mPhysicsWorld, Scene scene, int CAMERA_HEIGHT,
			int CAMERA_WIDTH) {
		super(pX, pY, pWidth, pHeight, pTiledTextureRegion,
				pTiledSpriteVertexBufferObject);
		this.playerX = pX;
		this.playerY = pY;
		playerTextureRegion = pTiledTextureRegion;
		this.pTiledSpriteVertexBufferObject = pTiledSpriteVertexBufferObject;
		this.scene = scene;
		ply = new AnimatedSprite(playerX, playerY, pTiledTextureRegion,
				pTiledSpriteVertexBufferObject) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				updatePosition(pSceneTouchEvent, pTouchAreaLocalX,
						pTouchAreaLocalY);
				return true;
			}
		};
		FixtureDef areaFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
				0.5f);
		areaFixtureDef.isSensor = true;
		body = PhysicsFactory.createBoxBody(mPhysicsWorld, ply,
				BodyType.DynamicBody, areaFixtureDef);

		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(ply, body,
				true, true));
		angleNormal = body.getAngle();
		body.setFixedRotation(true);
		ply.animate(200);
		ply.setUserData("Player");
		body.setUserData("Player");

		// final PhysicsHandler physicsHandler = new PhysicsHandler(ply);
		// ply.registerUpdateHandler(physicsHandler);
		scene.attachChild(ply);

		this.mPhysicsWorld = mPhysicsWorld;
		lastY = ply.getY();
		this.CAMERA_HEIGHT = CAMERA_HEIGHT;
		this.CAMERA_WIDTH = CAMERA_WIDTH;

	}

	public void updatePositionVelocity(float pX, float pY) {
		// Vector2 velocity = new Vector2(0, -pY); // experiment with the
		// numbers!!
		// body.setLinearVelocity(velocity);
		body.setTransform(pX, -pY, 0);
	}

	public void updatePosition(TouchEvent pSceneTouchEvent,
			final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP) {
			touching = false;
		}
		if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_MOVE) {
			final float widthD2 = ply.getWidth() / 2;
			final float heightD2 = ply.getHeight() / 2;
			int PIXEL_TO_METER_RATIO_DEFAULT = 32;
			float xCoord = (playerX + widthD2) / PIXEL_TO_METER_RATIO_DEFAULT;
			float YCoord = (pTouchAreaLocalY + heightD2)
					/ PIXEL_TO_METER_RATIO_DEFAULT;

			// Checking bounds
			if (!touching) {
				if (Math.abs(pTouchAreaLocalY - lastY) >= 40) {
					// YCoord = (CAMERA_HEIGHT + heightD2)
					// / PIXEL_TO_METER_RATIO_DEFAULT;
				} else {

					movePlayer(pTouchAreaLocalY, xCoord, YCoord);
				}
			} else {
				movePlayer(pTouchAreaLocalY, xCoord, YCoord);
			}

		}
	}

	private void movePlayer(float pTouchAreaLocalY, float xCoord, float YCoord) {

		float angle = body.getAngle();

		if (pTouchAreaLocalY <= CAMERA_HEIGHT - 25 && pTouchAreaLocalY >= 20) {
			final Vector2 v2 = Vector2Pool.obtain(xCoord, YCoord);
			body.setTransform(v2, angle);
			Vector2Pool.recycle(v2);
			lastY = ply.getY();
			touching = true;
		}
		changeAnglePlayer();
	}
	
	public void changeAnglePlayer(){
		float angle = body.getAngle();
		if (body.getPosition().y <= CAMERA_HEIGHT - 25) {
			if (angleNormal == angle) {
				final Vector2 v2 = Vector2Pool.obtain(body.getPosition().x, body.getPosition().y);
				body.setTransform(v2, angle + 10);
				Vector2Pool.recycle(v2);
				lastY = ply.getY();
				touching = true;
			}
		} else if (body.getPosition().y >= 20) {
			if (angleNormal == angle) {
				final Vector2 v2 = Vector2Pool.obtain(body.getPosition().x, body.getPosition().y);
				body.setTransform(v2, angle - 10);
				Vector2Pool.recycle(v2);
				lastY = ply.getY();
				touching = true;
			}
		}

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
		// // .findBodyByShape(ply);
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

	public void addplayerMJ(final float pX, final float pY) {
		this.mplayerMJCount++;
		Debug.d("playerMJs: " + this.mplayerMJCount);

		final AnimatedSprite playerMJ;
		final Body body;
		FixtureDef FIXTURE_DEF = PhysicsFactory.createFixtureDef(1, 0.5f, 0.5f);

		if (this.mplayerMJCount % 2 == 0) {
			playerMJ = new AnimatedSprite(playerX, playerY,
					playerTextureRegion, pTiledSpriteVertexBufferObject);
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, playerMJ,
					BodyType.DynamicBody, FIXTURE_DEF);
		} else {
			playerMJ = new AnimatedSprite(playerX, playerY,
					playerTextureRegion, pTiledSpriteVertexBufferObject);
			body = PhysicsFactory.createBoxBody(this.mPhysicsWorld, playerMJ,
					BodyType.DynamicBody, FIXTURE_DEF);
		}
		playerMJ.setUserData(body);
		playerMJ.animate(200);

		this.scene.registerTouchArea(playerMJ);
		this.scene.attachChild(playerMJ);

		this.mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(
				playerMJ, body, true, true));
	}

	// ===========================================================
	// Methods
	// ===========================================================

}