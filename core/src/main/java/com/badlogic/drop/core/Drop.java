package com.badlogic.drop.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.TimeUtils;

import java.util.Iterator;

public class Drop extends ApplicationAdapter {

	private Texture dropImage;
	private Texture bucketImage;
	private Sound dropSound;
	private Music rainMusic;
	private OrthographicCamera camera;
	private SpriteBatch batch;
	private Rectangle bucket;
	private long lastDropTime;

	private final Vector3 touchPosition = new Vector3();
	private Array<Rectangle> raindrops;

	final static int SCREEN_WIDTH = 800;
	final static int SCREEN_HEIGHT = 480;
	final static int BUCKET_SIZE = 64;
	final static int MOVEMENT_SPEED = 200;

	@Override
	public void create() {
		// load the images for the droplet and the bucket, 64x64 pixels each
		dropImage = new Texture(Gdx.files.internal("droplet.png"));
		bucketImage = new Texture(Gdx.files.internal("bucket.png"));

		// load the drop sound effect and the rain background "music"
		dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
		rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));

		// start the playback of the background music immediately
		rainMusic.setLooping(true);
		rainMusic.play();

		camera = new OrthographicCamera();
		camera.setToOrtho(false, SCREEN_WIDTH, SCREEN_HEIGHT);

		batch = new SpriteBatch();

		bucket = new Rectangle();
		bucket.x = SCREEN_WIDTH / 2 - BUCKET_SIZE / 2;
		bucket.y = 20;
		bucket.width = BUCKET_SIZE;
		bucket.height = BUCKET_SIZE;

		raindrops = new Array<>();
		spawnRaindrop();
	}

	@Override
	public void render() {
		ScreenUtils.clear(0, 0, 0.2f, 1);
		camera.update();

		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(bucketImage, bucket.x, bucket.y);

		drawRaindropArray();
		batch.end();

		handleMouseInput();
		handleKeyboardInput();
		handleFrameEnds();
		handleRaindropMovement();
	}

	// dispose of all the native resources
	@Override
	public void dispose() {
		dropImage.dispose();
		bucketImage.dispose();
		dropSound.dispose();
		rainMusic.dispose();
		batch.dispose();
	}

	private void drawRaindropArray() {
		for(Rectangle raindrop: raindrops) {
			batch.draw(dropImage, raindrop.x, raindrop.y);
		}
	}

	private void handleRaindropMovement() {
		if(TimeUtils.nanoTime() - lastDropTime > 1000000000) {
			spawnRaindrop();
		}

		for (Iterator<Rectangle> iter = raindrops.iterator(); iter.hasNext(); ) {
			Rectangle raindrop = iter.next();
			raindrop.y -= MOVEMENT_SPEED * Gdx.graphics.getDeltaTime();

			handleRaindropOutOfFrame(iter, raindrop);
			handleRaindropCollisionWithBucket(iter, raindrop);
		}
	}

	private void handleRaindropCollisionWithBucket(Iterator<Rectangle> iter, Rectangle raindrop) {
		if(raindrop.overlaps(bucket)) {
			dropSound.play();
			iter.remove();
		}
	}

	private void handleRaindropOutOfFrame(Iterator<Rectangle> iter, Rectangle raindrop) {
		if(raindrop.y + BUCKET_SIZE < 0) {
			iter.remove();
		}
	}

	private void handleMouseInput() {
		if(Gdx.input.isTouched()) {
			touchPosition.set(Gdx.input.getX(), Gdx.input.getY(), 0);
			camera.unproject(touchPosition);
			bucket.x = touchPosition.x - BUCKET_SIZE / 2;
		}
	}

	private void handleFrameEnds() {
		if(bucket.x < 0) bucket.x = 0;
		if(bucket.x > SCREEN_WIDTH - BUCKET_SIZE) bucket.x = SCREEN_WIDTH - BUCKET_SIZE;
	}

	private void handleKeyboardInput() {
		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)) bucket.x -= MOVEMENT_SPEED * Gdx.graphics.getDeltaTime();
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)) bucket.x += MOVEMENT_SPEED * Gdx.graphics.getDeltaTime();
	}

	private void spawnRaindrop() {
		Rectangle raindrop = new Rectangle();
		raindrop.x = MathUtils.random(0, SCREEN_WIDTH - BUCKET_SIZE);
		raindrop.y = SCREEN_HEIGHT;
		raindrop.width = BUCKET_SIZE;
		raindrop.height = BUCKET_SIZE;

		raindrops.add(raindrop);

		lastDropTime = TimeUtils.nanoTime();
	}

}
