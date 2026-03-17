package androidX.SkTeamProject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class AnimatedParticleView extends View {
    // constant variable
    public static final int MODE_SWIRL   = 0;
    public static final int MODE_BOUNCE  = 1;
    public static final int MODE_EXPLODE = 2;
    public static final int MODE_WAVE    = 3;
    public static final int MODE_MATRIX  = 4;
    public static final int MODE_SNOW    = 5;

    int   animationMode     = 0;
    int   backgroundColor   = Color.rgb(17, 17, 19);
    int   lineColor         = Color.rgb(255, 40, 40);
    int   particleColor     = Color.rgb(255, 40, 40);
    float lineLength        = 230.0f;
    float lineThickness     = 2.0f;
    float maxVelocity       = 0.4f;
    int   particleCount     = 50;
    float particleRadiusMax = 10.0f;
    float particleRadiusMin = 5.0f;

    Paint paint;
    Random random;
    GestureDetector gestureDetector;
    List<Particle> particles;
    int width, height;

    // Matrix fields
    List<MatrixColumn> matrixColumns   = new ArrayList<>();
    int   matrixColor      = Color.rgb(0, 255, 70);
    int   matrixHeadColor  = Color.rgb(180, 255, 180);
    float matrixFontSize   = 36.0f;
    float matrixSpeed      = 8.0f;
    int   matrixTrailLength = 18;
    int   matrixColumnCount = 0; 
    int   matrixBackgroundColor = Color.BLACK;
    Paint matrixPaint;

    // Snow fields
    int   snowColor       = Color.WHITE;
    float snowSpeedMin    = 2.0f;
    float snowSpeedMax    = 6.0f;
    float snowRadiusMin   = 4.0f;
    float snowRadiusMax   = 12.0f;
    float snowWindSpeed   = 0.3f; 
    int   snowCount       = 80;
    List<Snowflake> snowflakes = new ArrayList<>();

    // matrix chars
    private static final String MATRIX_CHARS =
        "アイウエオカキクケコサシスセソタチツテトナニヌネノハヒフヘホマミムメモヤユヨラリルレロワヲン" +
        "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789@#$%&*<>?!";

    // -------------------------------------------------------
    // Constructors
    // -------------------------------------------------------

    public AnimatedParticleView(Context context) {
        super(context);
        init(context, null);
    }

    public AnimatedParticleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public AnimatedParticleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs);
    }

    // -------------------------------------------------------
    // Init
    // -------------------------------------------------------

    private void init(Context context, AttributeSet attrs) {
        particles   = new ArrayList<>();
        paint       = new Paint(Paint.ANTI_ALIAS_FLAG);
        random      = new Random();
        matrixPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matrixPaint.setTypeface(Typeface.MONOSPACE);
        matrixPaint.setTextSize(matrixFontSize);

        gestureDetector = new GestureDetector(context,
            new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
            });

    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width  = w;
        height = h;
        if (width > 0 && height > 0) {
            particles.clear();
            for (int i = 0; i < particleCount; i++) {
                particles.add(new Particle());
            }
            initMatrixColumns();
            initSnowflakes();
            invalidate();
        }
    }

    // -------------------------------------------------------
    // onDraw
    // -------------------------------------------------------

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (animationMode == MODE_MATRIX) {
            canvas.drawColor(matrixBackgroundColor);
            drawMatrix(canvas);
        } else if (animationMode == MODE_SNOW) {
            canvas.drawColor(backgroundColor);
            for (Snowflake s : snowflakes) {
                s.update();
                s.draw(canvas, paint);
            }
        } else {
            canvas.drawColor(backgroundColor);
            for (Particle p : particles) {
                p.updatePosition();
                p.draw(canvas, paint);
            }
            drawLines(canvas);
        }
        invalidate();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return gestureDetector.onTouchEvent(event) || super.onTouchEvent(event);
    }

    private void initSnowflakes() {
        snowflakes.clear();
        if (width <= 0 || height <= 0) return;
        for (int i = 0; i < snowCount; i++) {
            snowflakes.add(new Snowflake(true));
        }
    }

    private void drawLines(Canvas canvas) {
        paint.setStrokeWidth(lineThickness);
        for (int i = 0; i < particles.size(); i++) {
            for (int j = i + 1; j < particles.size(); j++) {
                float dx   = particles.get(j).x - particles.get(i).x;
                float dy   = particles.get(j).y - particles.get(i).y;
                float dist = (float) Math.sqrt(dx * dx + dy * dy);
                if (dist < lineLength) {
                    float alpha = (1.0f - dist / lineLength) * 255.0f;
                    paint.setColor(Color.argb(
                        (int) alpha,
                        Color.red(lineColor),
                        Color.green(lineColor),
                        Color.blue(lineColor)));
                    canvas.drawLine(
                        particles.get(i).x, particles.get(i).y,
                        particles.get(j).x, particles.get(j).y,
                        paint);
                }
            }
        }
    }

    // -------------------------------------------------------
    // Matrix
    // -------------------------------------------------------

    private void initMatrixColumns() {
        matrixColumns.clear();
        if (width <= 0) return;
        int colCount = (matrixColumnCount > 0) ? matrixColumnCount : (int) (width / matrixFontSize);
        for (int i = 0; i < colCount; i++) {
            matrixColumns.add(new MatrixColumn(i));
        }
    }

    private void drawMatrix(Canvas canvas) {
        paint.setColor(Color.argb(40, 0, 0, 0));
        canvas.drawRect(0, 0, width, height, paint);
        matrixPaint.setTextSize(matrixFontSize);
        for (MatrixColumn col : matrixColumns) {
            col.update();
            col.draw(canvas, matrixPaint);
        }
    }

    // -------------------------------------------------------
    // Setters
    // -------------------------------------------------------

    public void setAnimationMode(int mode) {
        this.animationMode = mode;
        if (mode == MODE_MATRIX && matrixColumns.isEmpty() && width > 0) {
            initMatrixColumns();
        }
        invalidate();
    }

    @Override
    public void setBackgroundColor(int color) {
        this.backgroundColor = color;
        invalidate();
    }

    public void setLineColor(int color) {
        this.lineColor = color;
        invalidate();
    }

    public void setParticleColor(int color) {
        this.particleColor = color;
        invalidate();
    }

    public void setParticleRadiusRange(float min, float max) {
        this.particleRadiusMin = min;
        this.particleRadiusMax = max;
        invalidate();
    }

    public void setParticleCount(int count) {
        this.particleCount = count;
        invalidate();
    }

    @Deprecated
    public void setparticleCount(int count) {
        setParticleCount(count);
    }

    public void setMatrixColor(int color) {
        this.matrixColor = color;
        invalidate();
    }

    public void setMatrixHeadColor(int color) {
        this.matrixHeadColor = color;
        invalidate();
    }

    public void setMatrixFontSize(float size) {
        this.matrixFontSize = size;
        matrixPaint.setTextSize(size);
        if (width > 0) initMatrixColumns();
        invalidate();
    }

    public void setMatrixSpeed(float speed) {
        this.matrixSpeed = speed;
        invalidate();
    }

    public void setMatrixTrailLength(int length) {
        this.matrixTrailLength = length;
        if (width > 0) initMatrixColumns();
        invalidate();
    }

    public void setMatrixCount(int count) {
        this.matrixColumnCount = count;
        if (width > 0) initMatrixColumns();
        invalidate();
    }

    public void setMatrixBackgroundColor(int color) {
        this.matrixBackgroundColor = color;
        invalidate();
    }



    // -------------------------------------------------------
    // Snow Setters
    // -------------------------------------------------------

    public void setSnowColor(int color) {
        this.snowColor = color;
        invalidate();
    }

    public void setSnowCount(int count) {
        this.snowCount = count;
        if (width > 0) initSnowflakes();
        invalidate();
    }

    public void setSnowSpeed(float min, float max) {
        this.snowSpeedMin = min;
        this.snowSpeedMax = max;
        if (width > 0) initSnowflakes();
        invalidate();
    }

    public void setSnowRadius(float min, float max) {
        this.snowRadiusMin = min;
        this.snowRadiusMax = max;
        if (width > 0) initSnowflakes();
        invalidate();
    }

    public void setSnowWindSpeed(float wind) {
        this.snowWindSpeed = wind;
        invalidate();
    }

    // -------------------------------------------------------
    // Inner class: Particle
    // -------------------------------------------------------

    private class Particle {
        float x, y;
        float velocityX, velocityY;
        float radius, originalRadius;

        Particle() {
            x = random.nextFloat() * width;
            y = random.nextFloat() * height;
            setInitialVelocity();
            originalRadius = particleRadiusMin +
                random.nextFloat() * (particleRadiusMax - particleRadiusMin);
            radius = originalRadius;
        }

        void setInitialVelocity() {
            velocityX = (random.nextFloat() - 0.5f) * 2.0f * maxVelocity;
            velocityY = (random.nextFloat() - 0.5f) * 2.0f * maxVelocity;
        }

        void draw(Canvas canvas, Paint p) {
            p.setColor(particleColor);
            canvas.drawCircle(x, y, radius, p);
        }

        void updatePosition() {
            switch (animationMode) {
                case MODE_SWIRL:
                    velocityX += (random.nextFloat() - 0.5f) * 0.2f;
                    velocityY += (random.nextFloat() - 0.5f) * 0.2f;
                    break;
                case MODE_BOUNCE:
                    if ((x + velocityX > width  && velocityX > 0) ||
                        (x + velocityX < 0      && velocityX < 0)) velocityX *= -1;
                    if ((y + velocityY > height && velocityY > 0) ||
                        (y + velocityY < 0      && velocityY < 0)) velocityY *= -1;
                    break;
                case MODE_EXPLODE:
                    velocityX *= 1.05f;
                    velocityY *= 1.05f;
                    if (Math.abs(velocityX) > maxVelocity * 2.0f) setInitialVelocity();
                    break;
                case MODE_WAVE:
                    velocityY = (float) Math.sin(x / 50.0f) * maxVelocity;
                    velocityX = maxVelocity;
                    break;
            }
            x += velocityX;
            y += velocityY;
            if (x > width)  x = 0;
            if (x < 0)      x = width;
            if (y > height) y = 0;
            if (y < 0)      y = height;
        }
    }

    // -------------------------------------------------------
    // Inner class: MatrixColumn
    // -------------------------------------------------------

    private class MatrixColumn {
        int   index;
        float headY;
        float speed;
        char[] chars;
        int   charTimer;
        int   delayFrames;

        MatrixColumn(int index) {
            this.index = index;
            reset(true);
        }

        void reset(boolean randomStart) {
            speed       = matrixSpeed * (0.6f + random.nextFloat() * 0.8f);
            chars       = new char[matrixTrailLength];
            for (int i = 0; i < matrixTrailLength; i++) chars[i] = randomChar();
            charTimer   = 0;
            delayFrames = random.nextInt(60);
            headY = randomStart
                ? -random.nextFloat() * height * 1.5f
                : -matrixFontSize * matrixTrailLength;
        }

        char randomChar() {
            return MATRIX_CHARS.charAt(random.nextInt(MATRIX_CHARS.length()));
        }

        void update() {
            if (delayFrames > 0) { delayFrames--; return; }
            headY += speed;
            if (++charTimer >= 3) {
                charTimer = 0;
                chars[random.nextInt(matrixTrailLength)] = randomChar();
            }
            if (headY - matrixTrailLength * matrixFontSize > height) reset(false);
        }

        void draw(Canvas canvas, Paint p) {
            if (delayFrames > 0) return;
            float x = index * matrixFontSize;
            for (int i = 0; i < matrixTrailLength; i++) {
                float charY = headY - i * matrixFontSize;
                if (charY < -matrixFontSize || charY > height + matrixFontSize) continue;
                if (i == 0) {
                    p.setColor(matrixHeadColor);
                    p.setAlpha(255);
                } else {
                    float ratio = 1.0f - (float) i / matrixTrailLength;
                    p.setColor(matrixColor);
                    p.setAlpha((int) (ratio * ratio * 220));
                }
                canvas.drawText(String.valueOf(chars[i]), x, charY, p);
            }
            p.setAlpha(255);
        }
    }

    // -------------------------------------------------------
    // Inner class: Snowflake
    // -------------------------------------------------------

    private class Snowflake {
        float x, y;
        float speed;
        float radius;
        float drift; 
        float driftAngle;
        float alpha; 

        Snowflake(boolean randomY) {
            reset(randomY);
        }

        void reset(boolean randomY) {
            x         = random.nextFloat() * width;
            y         = randomY ? random.nextFloat() * height : -snowRadiusMax;
            speed     = snowSpeedMin + random.nextFloat() * (snowSpeedMax - snowSpeedMin);
            radius    = snowRadiusMin + random.nextFloat() * (snowRadiusMax - snowRadiusMin);
            drift     = (random.nextFloat() - 0.5f) * snowWindSpeed * 2;
            driftAngle = random.nextFloat() * (float)(Math.PI * 2);
            alpha     = 150 + random.nextInt(105); // 150-255
        }

        void update() {
            y += speed;

            driftAngle += 0.02f;
            x += drift + (float)(Math.sin(driftAngle) * snowWindSpeed);

            if (x < -radius)        x = width + radius;
            if (x > width + radius) x = -radius;

            if (y > height + radius) reset(false);
        }

        void draw(Canvas canvas, Paint p) {
            p.setColor(snowColor);
            p.setAlpha((int) alpha);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(radius * 0.3f);

            for (int i = 0; i < 3; i++) {
                float angle = (float)(i * Math.PI / 3);
                float dx = (float)(Math.cos(angle) * radius);
                float dy = (float)(Math.sin(angle) * radius);
                canvas.drawLine(x - dx, y - dy, x + dx, y + dy, p);

                float branchLen = radius * 0.4f;
                float bAngle1 = (float)(angle + Math.PI / 4);
                float bAngle2 = (float)(angle - Math.PI / 4);
                canvas.drawLine(x + dx, y + dy,
                    x + dx - (float)(Math.cos(bAngle1) * branchLen),
                    y + dy - (float)(Math.sin(bAngle1) * branchLen), p);
                canvas.drawLine(x + dx, y + dy,
                    x + dx - (float)(Math.cos(bAngle2) * branchLen),
                    y + dy - (float)(Math.sin(bAngle2) * branchLen), p);
                canvas.drawLine(x - dx, y - dy,
                    x - dx + (float)(Math.cos(bAngle1) * branchLen),
                    y - dy + (float)(Math.sin(bAngle1) * branchLen), p);
                canvas.drawLine(x - dx, y - dy,
                    x - dx + (float)(Math.cos(bAngle2) * branchLen),
                    y - dy + (float)(Math.sin(bAngle2) * branchLen), p);
            }

            p.setAlpha(255);
            p.setStyle(Paint.Style.FILL);
            p.setStrokeWidth(0);
        }
    }

}
