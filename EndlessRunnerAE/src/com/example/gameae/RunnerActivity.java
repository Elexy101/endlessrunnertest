package com.example.gameae;

import java.io.IOException;
import java.io.InputStream;

import org.andengine.engine.camera.Camera;
import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.physics.PhysicsHandler;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.text.Text;
import org.andengine.entity.util.FPSLogger;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsConnectorManager;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.ITexture;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.bitmap.BitmapTexture;
import org.andengine.opengl.texture.region.ITextureRegion;
import org.andengine.opengl.texture.region.TextureRegionFactory;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.ui.activity.SimpleBaseGameActivity;
import org.andengine.util.adt.io.in.IInputStreamOpener;
import org.andengine.util.color.Color;
import org.andengine.util.debug.Debug;

import android.view.Display;
import android.widget.Toast;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.joints.MouseJoint;

public class RunnerActivity extends SimpleBaseGameActivity {

	private Camera mCamera;
	private Scene scene;
	public static int CAMERA_WIDTH;
	public static int CAMERA_HEIGHT;
	private BitmapTextureAtlas mBitmapTextureAtlas;
	private ITextureRegion mBackgroundTextureRegionR,
			mBackgroundTextureRegionL, mCactusTR;
	private TiledTextureRegion mPlayerTextureRegion;
	private BitmapTextureAtlas sheetBitmapTextureAtlas;
	public static Background mAutoParallaxBackgroundTexture;
	private ITexture floor, clouds, mountains;

	float playerX;
	float playerY;
	int life;
	boolean isJumping = false;
	PhysicsWorld mPhysicsWorld;
	Player player;
	MouseJoint playerMJ;
	public static Body floorBody;
	Sprite bgL, bgR;
	private boolean isInsideZoone = false;
	private boolean isContactPlayerGround = false;
	Text myText;
	Cactus cactus;

	@Override
	public EngineOptions onCreateEngineOptions() {
		final Display display = getWindowManager().getDefaultDisplay();
		CAMERA_HEIGHT = display.getHeight();
		CAMERA_WIDTH = display.getWidth();
		// -------------------------------------------------------------//
		// ----------------------- CAMERA ---------------------------//
		this.mCamera = new Camera(0, 0, 1024, 618);
		// ----------------------- CAMERA ---------------------------//
		// -------------------------------------------------------------//
		return new EngineOptions(true, ScreenOrientation.LANDSCAPE_FIXED,
				new RatioResolutionPolicy(1024, 618), this.mCamera);

	}

	@Override
	protected void onCreateResources() {
		try {
			/* Load all the textures this game needs. */
			mBitmapTextureAtlas = new BitmapTextureAtlas(
					this.getTextureManager(), 200, 200);
			sheetBitmapTextureAtlas = new BitmapTextureAtlas(
					getTextureManager(), 512, 512);
			this.mBitmapTextureAtlas.load();

			// 1 - Set up bitmap textures
			ITexture backgroundTextureL = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("touchleft.png");
						}
					});
			ITexture backgroundTextureR = new BitmapTexture(
					this.getTextureManager(), new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("touchright.png");
						}
					});
			floor = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("floor2.png");
						}
					});
			clouds = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("cloud.png");
						}
					});
			mountains = new BitmapTexture(this.getTextureManager(),
					new IInputStreamOpener() {
						@Override
						public InputStream open() throws IOException {
							return getAssets().open("mountain.png");
						}
					});

			// 2 - Load bitmap textures into VRAM
			backgroundTextureL.load();
			backgroundTextureR.load();
			floor.load();
			clouds.load();
			mountains.load();

			// 3 - Set up texture regions
			this.mBackgroundTextureRegionL = TextureRegionFactory
					.extractFromTexture(backgroundTextureL);
			this.mBackgroundTextureRegionR = TextureRegionFactory
					.extractFromTexture(backgroundTextureR);

			mBitmapTextureAtlas = new BitmapTextureAtlas(
					this.getTextureManager(), 512, 512);
			mBitmapTextureAtlas.load();

			this.mPlayerTextureRegion = BitmapTextureAtlasTextureRegionFactory
					.createTiledFromAsset(this.mBitmapTextureAtlas, this,
							"jaimitopequeno.png", 0, 0, 2, 1);

			mBitmapTextureAtlas = new BitmapTextureAtlas(
					this.getTextureManager(), 70, 100);
			mBitmapTextureAtlas.load();
			this.mCactusTR = BitmapTextureAtlasTextureRegionFactory
					.createFromAsset(this.mBitmapTextureAtlas, this,
							"cactus.png", 0, 0);

			mEngine.getTextureManager().loadTexture(sheetBitmapTextureAtlas);

		} catch (IOException e) {
			Debug.e(e);
		}

	}

	@Override
	protected Scene onCreateScene() {
		life = 100;
		mPhysicsWorld = new PhysicsWorld(new Vector2(0, 0), false);
		this.mEngine.registerUpdateHandler(new FPSLogger());
		this.scene = new Scene();
		// -------------------------------------------------------------//
		// ----------------------- BACKGROUND ---------------------------//
		// createWorldLimits();
		mAutoParallaxBackgroundTexture = new Background(getTextureManager(),
				1024, 618, clouds, floor, mountains,
				getVertexBufferObjectManager(), scene,
				this.getApplicationContext(), mEngine, mPhysicsWorld,
				CAMERA_HEIGHT);

		new BitmapTextureAtlas(getTextureManager(), 1024, 618);

		// ----------------------- BACKGROUND ---------------------------//
		// -------------------------------------------------------------//
		// ----------------------- PLAYER ---------------------------//
		playerX = 100;
		playerY = CAMERA_HEIGHT - 140;
		player = new Player(playerX, playerY, 0, 0, mPlayerTextureRegion,
				this.getVertexBufferObjectManager(), mPhysicsWorld, scene,
				CAMERA_HEIGHT, CAMERA_WIDTH);

		cactus = new Cactus(playerX, 450, mCactusTR.getWidth(),
				mCactusTR.getHeight(), mCactusTR,
				getVertexBufferObjectManager(), mPhysicsWorld, scene,
				CAMERA_HEIGHT, CAMERA_WIDTH);
		FixtureDef areaFixtureDef = PhysicsFactory.createFixtureDef(1, 0.5f,
				0.5f);
		areaFixtureDef.isSensor = true;
		Body cactusBody = PhysicsFactory.createBoxBody(mPhysicsWorld, cactus,
				BodyType.DynamicBody, areaFixtureDef);
		cactusBody.setUserData("Cactus");
		mPhysicsWorld.registerPhysicsConnector(new PhysicsConnector(cactus,
				cactusBody, true, true));
		// ----------------------- PLAYER ---------------------------//
		// -------------------------------------------------------------//
		// ----------------------- TOUCH AREA ---------------------------//
		bgL = null;
		bgL = new Sprite(0, 0, mBackgroundTextureRegionL,
				getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
				player.updatePosition(pSceneTouchEvent, pTouchAreaLocalX,
						pTouchAreaLocalY);
				return true;
			}
		};
		bgR = null;
		bgR = new Sprite(bgL.getWidth(), 0, mBackgroundTextureRegionR,
				getVertexBufferObjectManager()) {
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					final float pTouchAreaLocalX, final float pTouchAreaLocalY) {

				return true;
			}
		};
		// ----------------------- TOUCH AREA ---------------------------//
		// -------------------------------------------------------------//
		// ----------------------- PHYSICS ---------------------------//
		FontFactory.setAssetBasePath("font/");
		ITexture atlas = new BitmapTextureAtlas(getTextureManager(), 256, 256,
				TextureOptions.BILINEAR);
		final Font font = FontFactory.createFromAsset(this.getFontManager(),
				atlas, getAssets(), "Droid.ttf", (float) 32, true,
				Color.BLACK.getABGRPackedInt());
		font.load();
		myText = new Text(300, 40, font, "HP (Life): " + String.valueOf(life)
				+ "%", "High Score: 999999999".length(),
				getVertexBufferObjectManager());
		HUD hud = new HUD();
		hud.attachChild(myText);
		mCamera.setHUD(hud);
		scene.attachChild(bgL);
		scene.registerTouchArea(bgL);
		scene.attachChild(bgR);
		scene.registerTouchArea(bgR);
		scene.setTouchAreaBindingOnActionDownEnabled(true);
		scene.registerUpdateHandler(mPhysicsWorld);
		// scene.attachChild(new Box2dDebugRenderer(mPhysicsWorld));

		// mCamera.setChaseEntity(ply);
		mPhysicsWorld.setContactListener(createContactListener());

		final PhysicsHandler physicsHandlerCactus = new PhysicsHandler(cactus);
		cactus.registerUpdateHandler(physicsHandlerCactus);

		// ----------------------- PHYSICS ---------------------------//
		// -------------------------------------------------------------//

		return this.scene;
	}

	private ContactListener createContactListener() {
		ContactListener contactListener = new ContactListener() {
			@Override
			public void beginContact(Contact contact) {
				final Fixture x1 = contact.getFixtureA();
				final Fixture x2 = contact.getFixtureB();

				try {
					boolean isCactusA = x2.getBody().getUserData()
							.equals("Cactus");
					boolean isCactusB = x2.getBody().getUserData()
							.equals("Cactus");
					boolean isGroundA = x2.getBody().getUserData()
							.equals("Ground");
					boolean isGroundB = x1.getBody().getUserData()
							.equals("Ground");
					boolean isPlayerA = x2.getBody().getUserData()
							.equals("Player");
					boolean isPlayerB = x1.getBody().getUserData()
							.equals("Player");
					boolean contactPlayerGround = (isGroundA && isPlayerA)
							|| (isGroundA && isPlayerB)
							|| (isGroundB && isPlayerB)
							|| (isGroundB && isPlayerA);
					boolean contactPlayerCactus = (isCactusA && isPlayerA)
							|| (isCactusA && isPlayerB)
							|| (isCactusB && isPlayerB)
							|| (isCactusB && isPlayerA);

					if (contactPlayerCactus) {
						RunnerActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getBaseContext(),
										"Contact with cactus!",
										Toast.LENGTH_LONG).show();

							}
						});

						isInsideZoone = true;
						// final Vector2 velocity = Vector2Pool.obtain(0, 0);
						// body.setLinearVelocity(velocity);
						// Vector2Pool.recycle(velocity);
						life = life - 10;
						myText.setText("HP (Life): " + String.valueOf(life)
								+ "%");

					} else if (contactPlayerGround) {
						RunnerActivity.this.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								Toast.makeText(getBaseContext(),
										"Contact with ground!",
										Toast.LENGTH_LONG).show();
								isContactPlayerGround = true;

							}
						});

						RunnerActivity.mAutoParallaxBackgroundTexture
								.stopBackgroundMovement();

					} else {
						RunnerActivity.mAutoParallaxBackgroundTexture
								.restarBackgroundMovement();
					}

				} catch (Exception e) {
					if (RunnerActivity.mAutoParallaxBackgroundTexture.isBackgroundStopped) {
						RunnerActivity.mAutoParallaxBackgroundTexture
								.restarBackgroundMovement();
					}
				}

			}

			@Override
			public void endContact(Contact contact) {

			}

			@Override
			public void preSolve(Contact contact, Manifold oldManifold) {
				PhysicsConnectorManager mPC = mPhysicsWorld
						.getPhysicsConnectorManager();
				Body targetBody = mPC.findBodyByShape(cactus);
				Body ballBody = mPC.findBodyByShape(player);

				contact.setEnabled(true);

			}

			@Override
			public void postSolve(Contact contact, ContactImpulse impulse) {

			}
		};
		return contactListener;
	}

	// @Override
	// public boolean onSceneTouchEvent(Scene pScene, TouchEvent
	// pSceneTouchEvent) {
	// if (this.mPhysicsWorld != null) {
	// switch (pSceneTouchEvent.getAction()) {
	// case TouchEvent.ACTION_DOWN:
	// player.addplayerMJ(player.getX(), pSceneTouchEvent.getY());
	// return true;
	// case TouchEvent.ACTION_MOVE:
	// if (this.playerMJ != null) {
	// final Vector2 vec = Vector2Pool
	// .obtain(pSceneTouchEvent.getX()
	// / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
	// pSceneTouchEvent.getY()
	// / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT);
	// this.playerMJ.setTarget(vec);
	// Vector2Pool.recycle(vec);
	// }
	// return true;
	// case TouchEvent.ACTION_UP:
	// if (this.playerMJ != null) {
	// this.mPhysicsWorld.destroyJoint(this.playerMJ);
	// this.playerMJ = null;
	// }
	// return true;
	// }
	// return false;
	// }
	// return false;
	// }
	//
	// @Override
	// public boolean onAreaTouched(final TouchEvent pSceneTouchEvent,
	// final ITouchArea pTouchArea, final float pTouchAreaLocalX,
	// final float pTouchAreaLocalY) {
	// if (pSceneTouchEvent.isActionDown()) {
	// final IShape face = (IShape) pTouchArea;
	// /*
	// * If we have a active MouseJoint, we are just moving it around
	// * instead of creating a second one.
	// */
	// if (this.playerMJ == null) {
	// // this.mEngine.enableVibrator(this);
	// // this.mEngine.vibrate(100);
	// this.playerMJ = player.createMouseJoint(face, pTouchAreaLocalX,
	// pTouchAreaLocalY);
	// }
	// return true;
	// }
	// return false;
	// }

	// public boolean onAreaTouchedFinal(final TouchEvent pSceneTouchEvent,
	// final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
	// if (this.mPhysicsWorld != null) {
	//
	// if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_DOWN) {
	// if (playerMJ == null) {
	// // this.mEngine.vibrate(100);
	// playerMJ = player.createMouseJoint(pTouchAreaLocalX,
	// pTouchAreaLocalY);
	// }
	// }
	//
	// if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_MOVE) {
	//
	// this.runOnUpdateThread(new Runnable() {
	// @Override
	// public void run() {
	//
	// if (playerMJ != null) { // If the MJ is active move it
	// // ..
	//
	// // =========================================
	// // MOVE THE MOUSEJOINT WITH THE FINGER..
	// // =========================================
	// // Vector2 vec = new Vector2(pSceneTouchEvent.getX()
	// // / player.pixelToMeteRatio,
	// // pSceneTouchEvent.getY()
	// // / player.pixelToMeteRatio);
	// Vector2 vec = new Vector2(250, 254);
	// playerMJ.setTarget(vec);
	//
	// }
	// }
	// });
	// return true;
	// }
	//
	// // ===========================================
	// // RELEASE THE FINGER FROM THE SCENE..
	// // ===========================================
	// if (pSceneTouchEvent.getAction() == MotionEvent.ACTION_UP
	// || pSceneTouchEvent.getAction() == MotionEvent.ACTION_CANCEL) {
	//
	// this.runOnUpdateThread(new Runnable() {
	// @Override
	// public void run() {
	//
	// if (playerMJ != null) {
	// // ======================================
	// // DESTROY OUR MOUSEJOINT
	// // ======================================
	// mPhysicsWorld.destroyJoint(playerMJ);
	// // groundBody.setTransform(pSceneTouchEvent.getX(),
	// // pSceneTouchEvent.getY(), groundBody.get);
	// mPhysicsWorld.destroyBody(player.groundBody);
	// playerMJ = null;
	// }
	//
	// }
	// });
	//
	// // groundBody.setLinearVelocity(0,0);
	// return true;
	// }
	// }
	// return false;
	// }

	private void createWorldLimits() {

		final VertexBufferObjectManager vertexBufferObjectManager = this
				.getVertexBufferObjectManager();
		final Rectangle ground = new Rectangle(0, CAMERA_HEIGHT - 2,
				CAMERA_WIDTH, 2, vertexBufferObjectManager);
		final Rectangle roof = new Rectangle(0, 0, CAMERA_WIDTH, 2,
				vertexBufferObjectManager);
		final Rectangle left = new Rectangle(0, 0, 2, CAMERA_HEIGHT,
				vertexBufferObjectManager);
		final Rectangle right = new Rectangle(CAMERA_WIDTH - 2, 0, 2,
				CAMERA_HEIGHT, vertexBufferObjectManager);

		final FixtureDef wallFixtureDef = PhysicsFactory.createFixtureDef(0,
				0.5f, 0.5f);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, ground,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, roof,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, left,
				BodyType.StaticBody, wallFixtureDef);
		PhysicsFactory.createBoxBody(this.mPhysicsWorld, right,
				BodyType.StaticBody, wallFixtureDef);

		this.scene.attachChild(ground);
		this.scene.attachChild(roof);
		this.scene.attachChild(left);
		this.scene.attachChild(right);
	}

}
