package com.skytap.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView extends View {
    private enum Screen { HOME, PLAYING, GAME_OVER }
    private static class Obstacle {
        float x, width, height; boolean passed;
        Obstacle(float x, float width, float height) { this.x=x; this.width=width; this.height=height; }
    }

    private final Paint p = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint txt = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Path path = new Path();
    private final Random rng = new Random();
    private final SharedPreferences prefs;
    private final List<Obstacle> obstacles = new ArrayList<>();
    private final RectF play = new RectF(), home = new RectF(), pause = new RectF();

    private Screen screen = Screen.HOME;
    private int w,h,score,best,coins;
    private float groundY, px, py, vy, spawn=1f, time=0f;
    private long lastFrame;
    private boolean grounded=true;

    public GameView(Context context) {
        super(context);
        prefs=context.getSharedPreferences("human_runner_5d", Context.MODE_PRIVATE);
        best=prefs.getInt("best",0); coins=prefs.getInt("coins",126);
        txt.setTypeface(Typeface.create(Typeface.DEFAULT, Typeface.BOLD));
        setLayerType(View.LAYER_TYPE_SOFTWARE,null);
    }

    @Override protected void onSizeChanged(int width,int height,int oldw,int oldh){
        w=width; h=height; groundY=h*.80f; px=w*.27f; py=groundY;
        pause.set(w*.035f,h*.035f,w*.145f,h*.09f);
        play.set(w*.22f,h*.68f,w*.78f,h*.755f);
        home.set(w*.22f,h*.78f,w*.78f,h*.855f);
    }

    private void start(){
        screen=Screen.PLAYING; score=0; obstacles.clear(); py=groundY; vy=0; grounded=true;
        spawn=.85f; lastFrame=System.nanoTime(); invalidate();
    }
    private void finish(){ if(score>best) best=score; save(); screen=Screen.GAME_OVER; invalidate(); }
    private void save(){ prefs.edit().putInt("best",best).putInt("coins",coins).apply(); }
    private void jump(){ if(grounded){ vy=-h*.77f; grounded=false; } }

    @Override protected void onDraw(Canvas c){
        super.onDraw(c);
        if(screen==Screen.PLAYING) update(); else time+=.012f;
        world(c); road(c); obstacles(c); human(c,px,py,time,screen==Screen.PLAYING); hud(c);
        if(screen==Screen.HOME) homeScreen(c);
        else if(screen==Screen.GAME_OVER) gameOver(c);
        if(screen!=Screen.GAME_OVER) postInvalidateOnAnimation();
    }

    private void update(){
        long now=System.nanoTime(); float dt=lastFrame==0?.016f:Math.min(.035f,(now-lastFrame)/1_000_000_000f); lastFrame=now; time+=dt;
        vy+=h*2.1f*dt; py+=vy*dt; if(py>=groundY){py=groundY;vy=0;grounded=true;}
        spawn-=dt; if(spawn<=0){ obstacles.add(new Obstacle(w*1.08f,w*(.09f+rng.nextFloat()*.045f),h*(.07f+rng.nextFloat()*.05f))); spawn=1.35f+rng.nextFloat()*.5f; }
        float speed=w*.43f; RectF player=new RectF(px-w*.032f,py-h*.125f,px+w*.038f,py-h*.006f);
        Iterator<Obstacle> it=obstacles.iterator();
        while(it.hasNext()){
            Obstacle o=it.next(); o.x-=speed*dt;
            if(!o.passed && o.x+o.width<px){o.passed=true;score++;coins++;save();}
            if(o.x+o.width<-w*.08f){it.remove();continue;}
            RectF box=new RectF(o.x,groundY-o.height,o.x+o.width,groundY);
            if(RectF.intersects(player,box)){finish();return;}
        }
    }

    private void world(Canvas c){
        p.setShader(new LinearGradient(0,0,0,h,new int[]{Color.rgb(22,51,83),Color.rgb(72,137,173),Color.rgb(232,181,116)},new float[]{0f,.56f,1f},Shader.TileMode.CLAMP));
        c.drawRect(0,0,w,h,p); p.setShader(null);
        p.setShader(new RadialGradient(w*.76f,h*.18f,w*.34f,Color.argb(190,255,224,153),Color.argb(0,255,224,153),Shader.TileMode.CLAMP));
        c.drawCircle(w*.76f,h*.18f,w*.34f,p); p.setShader(null);
        float d1=(time*w*.018f)%w; mountains(c,-d1,h*.49f,Color.rgb(59,83,96)); mountains(c,w-d1,h*.49f,Color.rgb(59,83,96));
        float d2=(time*w*.035f)%w; hills(c,-d2,h*.59f); hills(c,w-d2,h*.59f);
        float d3=(time*w*.058f)%w; city(c,-d3,h*.66f); city(c,w-d3,h*.66f);
        p.setShader(new LinearGradient(0,h*.43f,0,h*.72f,Color.argb(0,255,255,255),Color.argb(90,235,219,190),Shader.TileMode.CLAMP));
        c.drawRect(0,h*.43f,w,h*.72f,p); p.setShader(null);
    }

    private void mountains(Canvas c,float o,float base,int color){
        p.setColor(color); path.reset(); path.moveTo(o,base); path.lineTo(o+w*.18f,base-h*.29f); path.lineTo(o+w*.34f,base-h*.09f);
        path.lineTo(o+w*.52f,base-h*.25f); path.lineTo(o+w*.70f,base-h*.07f); path.lineTo(o+w*.86f,base-h*.20f); path.lineTo(o+w,base);
        path.lineTo(o+w,base+h*.12f); path.lineTo(o,base+h*.12f); path.close(); c.drawPath(path,p);
        p.setColor(Color.argb(70,255,255,255)); c.drawCircle(o+w*.18f,base-h*.29f,w*.018f,p); c.drawCircle(o+w*.52f,base-h*.25f,w*.014f,p);
    }
    private void hills(Canvas c,float o,float base){
        p.setColor(Color.rgb(67,109,88)); c.drawOval(new RectF(o-w*.1f,base-h*.16f,o+w*.55f,base+h*.1f),p);
        c.drawOval(new RectF(o+w*.35f,base-h*.13f,o+w*1.04f,base+h*.1f),p);
    }
    private void city(Canvas c,float o,float base){
        for(int i=0;i<9;i++){
            float bw=w*(.065f+(i%3)*.014f), bh=h*(.07f+(i%4)*.023f), x=o+i*w*.12f;
            p.setColor(i%2==0?Color.rgb(45,60,65):Color.rgb(56,71,73)); c.drawRect(x,base-bh,x+bw,base,p);
            p.setColor(Color.argb(105,255,204,123));
            for(int r=0;r<3;r++)for(int q=0;q<2;q++){float wx=x+bw*(.22f+q*.42f),wy=base-bh+bh*(.2f+r*.25f);c.drawRect(wx,wy,wx+bw*.1f,wy+bh*.07f,p);}
        }
    }

    private void road(Canvas c){
        p.setShader(new LinearGradient(0,h*.67f,0,groundY,Color.rgb(94,82,66),Color.rgb(45,46,46),Shader.TileMode.CLAMP)); c.drawRect(0,h*.67f,w,groundY,p); p.setShader(null);
        p.setShader(new LinearGradient(0,groundY,0,h,Color.rgb(39,40,41),Color.rgb(15,18,20),Shader.TileMode.CLAMP)); c.drawRect(0,groundY,w,h,p); p.setShader(null);
        p.setColor(Color.argb(75,255,255,255)); c.drawRect(0,groundY-h*.005f,w,groundY,p);
        float off=(time*w*.58f)%(w*.20f); p.setColor(Color.argb(145,244,212,127));
        for(int i=-1;i<8;i++){float x=i*w*.20f-off;c.drawRoundRect(new RectF(x,h*.91f,x+w*.1f,h*.918f),w*.004f,w*.004f,p);}
        p.setShader(new LinearGradient(0,h*.84f,0,h,Color.argb(0,0,0,0),Color.argb(115,0,0,0),Shader.TileMode.CLAMP)); c.drawRect(0,h*.84f,w,h,p); p.setShader(null);
    }

    private void obstacles(Canvas c){ for(Obstacle o:obstacles) barrier(c,o); }
    private void barrier(Canvas c,Obstacle o){
        RectF r=new RectF(o.x,groundY-o.height,o.x+o.width,groundY);
        p.setShader(new LinearGradient(r.left,r.top,r.right,r.bottom,Color.rgb(101,104,105),Color.rgb(45,49,52),Shader.TileMode.CLAMP)); c.drawRoundRect(r,w*.012f,w*.012f,p);p.setShader(null);
        p.setColor(Color.argb(90,255,255,255)); c.drawRect(r.left+w*.012f,r.top+h*.008f,r.right-w*.012f,r.top+h*.015f,p);
        p.setColor(Color.rgb(230,172,54));
        for(int i=0;i<3;i++){float yy=r.top+o.height*(.18f+i*.27f);path.reset();path.moveTo(r.left,yy);path.lineTo(r.left+o.width*.22f,yy-o.height*.15f);path.lineTo(r.left+o.width*.40f,yy-o.height*.15f);path.lineTo(r.left+o.width*.18f,yy);path.close();c.drawPath(path,p);}
    }

    private void human(Canvas c,float x,float foot,float t,boolean running){
        float s=w*.085f, lift=groundY-foot, bob=running?(float)Math.sin(t*16f)*h*.0025f:0, phase=running?(float)Math.sin(t*13f):0;
        float y=foot+bob, sh=1f-Math.min(.55f,lift/(h*.35f));
        p.setColor(Color.argb((int)(105*sh),0,0,0)); c.drawOval(new RectF(x-s*.58f*sh,groundY-h*.006f,x+s*.62f*sh,groundY+h*.012f),p);
        c.save(); c.translate(x,y);
        limb(c,-s*.05f,-s*.56f,-s*.14f+phase*s*.18f,s*.01f,s*.15f,Color.rgb(38,48,61));
        limb(c,-s*.12f,-s*1.22f,-s*.38f-phase*s*.12f,-s*.75f,s*.105f,Color.rgb(45,66,84));
        path.reset();path.moveTo(-s*.30f,-s*1.37f);path.lineTo(s*.24f,-s*1.33f);path.lineTo(s*.30f,-s*.60f);path.lineTo(-s*.22f,-s*.58f);path.close();
        p.setShader(new LinearGradient(-s*.3f,0,s*.3f,0,new int[]{Color.rgb(29,53,73),Color.rgb(78,112,139),Color.rgb(24,43,60)},new float[]{0f,.52f,1f},Shader.TileMode.CLAMP));c.drawPath(path,p);p.setShader(null);
        p.setColor(Color.rgb(211,221,220));c.drawRoundRect(new RectF(-s*.05f,-s*1.29f,s*.10f,-s*.73f),s*.035f,s*.035f,p);
        limb(c,s*.10f,-s*.58f,s*.18f-phase*s*.20f,s*.01f,s*.16f,Color.rgb(28,38,49));
        p.setColor(Color.rgb(25,28,32)); c.drawRoundRect(new RectF(-s*.32f+phase*s*.12f,-s*.02f,s*.05f+phase*s*.12f,s*.11f),s*.04f,s*.04f,p); c.drawRoundRect(new RectF(s*.02f-phase*s*.14f,-s*.02f,s*.40f-phase*s*.14f,s*.11f),s*.04f,s*.04f,p);
        limb(c,s*.18f,-s*1.18f,s*.42f+phase*s*.14f,-s*.72f,s*.105f,Color.rgb(62,88,108));
        p.setColor(Color.rgb(197,145,108));c.drawCircle(s*.43f+phase*s*.14f,-s*.70f,s*.10f,p);
        p.setShader(new LinearGradient(-s*.1f,0,s*.12f,0,Color.rgb(168,116,88),Color.rgb(218,168,130),Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(-s*.09f,-s*1.52f,s*.09f,-s*1.30f),s*.05f,s*.05f,p);p.setShader(null);
        p.setShader(new RadialGradient(-s*.08f,-s*1.82f,s*.48f,Color.rgb(232,181,141),Color.rgb(171,113,82),Shader.TileMode.CLAMP));c.drawOval(new RectF(-s*.28f,-s*1.97f,s*.28f,-s*1.45f),p);p.setShader(null);
        p.setColor(Color.rgb(33,28,27));path.reset();path.moveTo(-s*.29f,-s*1.78f);path.quadTo(-s*.22f,-s*2.08f,s*.22f,-s*1.98f);path.lineTo(s*.29f,-s*1.82f);path.quadTo(s*.10f,-s*1.92f,-s*.29f,-s*1.78f);path.close();c.drawPath(path,p);
        p.setColor(Color.rgb(61,45,41));c.drawCircle(s*.10f,-s*1.79f,s*.025f,p);p.setColor(Color.argb(75,255,255,255));c.drawOval(new RectF(-s*.17f,-s*1.91f,-s*.02f,-s*1.68f),p);
        p.setShader(new LinearGradient(-s*.4f,0,-s*.12f,0,Color.rgb(32,40,47),Color.rgb(80,94,101),Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(-s*.39f,-s*1.29f,-s*.14f,-s*.72f),s*.07f,s*.07f,p);p.setShader(null);
        c.restore();
    }
    private void limb(Canvas c,float x1,float y1,float x2,float y2,float width,int color){p.setStyle(Paint.Style.STROKE);p.setStrokeCap(Paint.Cap.ROUND);p.setStrokeWidth(width);p.setColor(color);c.drawLine(x1,y1,x2,y2,p);p.setStyle(Paint.Style.FILL);}

    private void hud(Canvas c){
        p.setColor(Color.argb(145,18,27,34));c.drawRoundRect(pause,w*.018f,w*.018f,p); drawText(c,"Ⅱ",pause.centerX(),pause.centerY()+h*.010f,w*.055f,Color.WHITE);
        RectF s=new RectF(w*.35f,h*.032f,w*.66f,h*.155f);p.setColor(Color.argb(130,10,16,21));c.drawRoundRect(s,w*.025f,w*.025f,p);
        p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.004f);p.setColor(Color.argb(120,255,220,158));c.drawRoundRect(s,w*.025f,w*.025f,p);p.setStyle(Paint.Style.FILL);
        drawText(c,"SCORE",s.centerX(),s.top+s.height()*.24f,w*.031f,Color.rgb(239,224,202));drawText(c,""+score,s.centerX(),s.top+s.height()*.65f,w*.09f,Color.WHITE);drawText(c,"BEST "+best,s.centerX(),s.bottom-s.height()*.09f,w*.025f,Color.rgb(245,190,83));
        RectF q=new RectF(w*.71f,h*.038f,w*.96f,h*.095f);p.setColor(Color.argb(140,15,23,28));c.drawRoundRect(q,q.height()/2,q.height()/2,p);float r=q.height()*.32f,cx=q.left+q.height()*.62f;
        p.setShader(new RadialGradient(cx-r*.2f,q.centerY()-r*.2f,r,Color.rgb(255,229,115),Color.rgb(213,135,36),Shader.TileMode.CLAMP));c.drawCircle(cx,q.centerY(),r,p);p.setShader(null);drawText(c,"$",cx,q.centerY()+r*.34f,w*.024f,Color.rgb(115,68,25));drawText(c,""+coins,q.left+q.width()*.64f,q.centerY()+q.height()*.17f,w*.039f,Color.WHITE);
    }

    private void homeScreen(Canvas c){
        p.setShader(new LinearGradient(0,h*.14f,0,h*.37f,Color.argb(0,0,0,0),Color.argb(120,0,0,0),Shader.TileMode.CLAMP));c.drawRect(0,h*.14f,w,h*.37f,p);p.setShader(null);
        drawText(c,"HUMAN RUNNER",w*.5f,h*.23f,w*.075f,Color.WHITE);drawText(c,"CINEMATIC 5D LOOK",w*.5f,h*.275f,w*.032f,Color.rgb(247,198,107));drawText(c,"Tap to jump",w*.5f,h*.61f,w*.034f,Color.WHITE);
        button(c,play,"PLAY",Color.rgb(208,140,53),Color.rgb(249,199,104));button(c,home,"START AGAIN",Color.rgb(45,89,118),Color.rgb(75,139,174));
    }
    private void gameOver(Canvas c){
        p.setColor(Color.argb(160,7,10,12));c.drawRoundRect(new RectF(w*.11f,h*.24f,w*.89f,h*.61f),w*.035f,w*.035f,p);drawText(c,"RUN ENDED",w*.5f,h*.33f,w*.065f,Color.WHITE);drawText(c,"Score  "+score,w*.5f,h*.42f,w*.048f,Color.rgb(247,198,107));drawText(c,"Best  "+best,w*.5f,h*.48f,w*.039f,Color.WHITE);button(c,play,"PLAY AGAIN",Color.rgb(208,140,53),Color.rgb(249,199,104));button(c,home,"HOME",Color.rgb(45,89,118),Color.rgb(75,139,174));
    }
    private void button(Canvas c,RectF r,String label,int a,int b){p.setColor(Color.argb(75,0,0,0));c.drawRoundRect(new RectF(r.left+w*.008f,r.top+h*.005f,r.right+w*.008f,r.bottom+h*.005f),w*.025f,w*.025f,p);p.setShader(new LinearGradient(0,r.top,0,r.bottom,b,a,Shader.TileMode.CLAMP));c.drawRoundRect(r,w*.025f,w*.025f,p);p.setShader(null);drawText(c,label,r.centerX(),r.centerY()+r.height()*.17f,w*.043f,Color.WHITE);}
    private void drawText(Canvas c,String s,float x,float y,float size,int color){txt.setTextAlign(Paint.Align.CENTER);txt.setTextSize(size);txt.setColor(color);c.drawText(s,x,y,txt);}

    @Override public boolean onTouchEvent(MotionEvent e){
        if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();
        if(screen==Screen.HOME){if(play.contains(x,y)||home.contains(x,y))start();return true;}
        if(screen==Screen.PLAYING){if(pause.contains(x,y)){screen=Screen.HOME;save();invalidate();}else jump();return true;}
        if(screen==Screen.GAME_OVER){if(play.contains(x,y))start();else if(home.contains(x,y)){screen=Screen.HOME;invalidate();}return true;}
        return true;
    }
}
