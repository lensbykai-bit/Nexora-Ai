package com.skytap.game;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.*;
import android.view.MotionEvent;
import android.view.View;
import java.util.*;

public class GameView extends View {
  enum S { HOME, PLAY, OVER }
  static class Pipe { float x,g; boolean pass; Pipe(float x,float g){this.x=x;this.g=g;} }
  final Paint p=new Paint(3), t=new Paint(3); final Path path=new Path(); final Random rnd=new Random();
  final ArrayList<Pipe> pipes=new ArrayList<>(); final SharedPreferences sp;
  S s=S.HOME; int w,h,score,best,coins,last; float bx,by,vy,spawn; long frame;
  final RectF play=new RectF(),retry=new RectF(),pause=new RectF(),coinBox=new RectF();

  public GameView(Context c){ super(c); sp=c.getSharedPreferences("skytap_save",0); best=sp.getInt("best",38); coins=sp.getInt("coins",126); last=sp.getInt("lastScore",12); t.setTypeface(Typeface.create(Typeface.DEFAULT,Typeface.BOLD)); setLayerType(View.LAYER_TYPE_SOFTWARE,null); }
  protected void onSizeChanged(int W,int H,int ow,int oh){w=W;h=H; reset(); float ps=w*.115f; pause.set(w*.04f,h*.04f,w*.04f+ps,h*.04f+ps); coinBox.set(w*.69f,h*.04f,w*.96f,h*.105f); play.set(w*.35f,h*.82f,w*.65f,h*.90f); retry.set(w*.32f,h*.91f,w*.68f,h*.97f);}
  void reset(){bx=w*.32f;by=h*.49f;vy=0;}
  void start(){s=S.PLAY;score=0;pipes.clear();reset();spawn=.25f;frame=System.nanoTime();invalidate();}
  void end(){last=score;if(score>best)best=score;save();s=S.OVER;invalidate();}
  void save(){sp.edit().putInt("best",best).putInt("coins",coins).putInt("lastScore",last).apply();}

  protected void onDraw(Canvas c){ super.onDraw(c); bg(c); if(s==S.HOME){demoPipes(c);bird(c,w*.35f,h*.52f,w*.082f);hud(c,Math.max(last,12),Math.max(best,38));hint(c);buttons(c);} else if(s==S.PLAY){tick();movingPipes(c);bird(c,bx,by,w*.055f);hud(c,score,best);if(score==0)hint(c);postInvalidateOnAnimation();} else {movingPipes(c);bird(c,bx,by,w*.055f);hud(c,score,best);txt(c,"GAME OVER",w*.5f,h*.29f,w*.065f,Color.WHITE);buttons(c);} }

  void bg(Canvas c){
    p.setShader(new LinearGradient(0,0,0,h,Color.rgb(42,162,244),Color.rgb(170,225,255),Shader.TileMode.CLAMP));c.drawRect(0,0,w,h,p);p.setShader(null);
    cloud(c,w*.05f,h*.18f,w*.10f);cloud(c,w*.91f,h*.22f,w*.09f);cloud(c,w*.10f,h*.48f,w*.12f);cloud(c,w*.86f,h*.55f,w*.11f);
    p.setColor(Color.rgb(184,226,158));c.drawOval(new RectF(-w*.12f,h*.70f,w*.55f,h*.92f),p);c.drawOval(new RectF(w*.45f,h*.69f,w*1.1f,h*.92f),p);
    p.setColor(Color.rgb(131,201,122));c.drawOval(new RectF(w*.12f,h*.74f,w*.91f,h*.92f),p);
    p.setShader(new LinearGradient(0,h*.74f,0,h*.90f,Color.rgb(156,219,255),Color.rgb(76,177,236),Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(w*.37f,h*.73f,w*.65f,h*.91f),w*.03f,w*.03f,p);p.setShader(null);
    windmill(c,w*.09f,h*.89f,w*.16f);tree(c,w*.88f,h*.88f,w*.18f);
    p.setColor(Color.rgb(165,230,80));c.drawRect(0,h*.90f,w,h*.94f,p);p.setColor(Color.rgb(110,76,50));c.drawRect(0,h*.94f,w,h,p);
    p.setColor(Color.rgb(139,98,62));for(int i=0;i<11;i++)c.drawCircle(w*(.04f+i*.095f),h*.975f,w*.025f,p);
    flower(c,w*.06f,h*.91f,w*.026f,Color.WHITE);flower(c,w*.15f,h*.92f,w*.018f,Color.WHITE);flower(c,w*.91f,h*.92f,w*.028f,Color.rgb(255,190,216));
  }
  void cloud(Canvas c,float x,float y,float r){p.setColor(Color.WHITE);c.drawCircle(x-r*.45f,y,r*.55f,p);c.drawCircle(x,y-r*.12f,r*.72f,p);c.drawCircle(x+r*.58f,y,r*.52f,p);c.drawRoundRect(new RectF(x-r,y,x+r*1.05f,y+r*.42f),r*.2f,r*.2f,p);}
  void windmill(Canvas c,float x,float y,float z){p.setColor(Color.rgb(239,235,220));path.reset();path.moveTo(x,y);path.lineTo(x+z*.22f,y-z*.72f);path.lineTo(x+z*.46f,y);path.close();c.drawPath(path,p);p.setColor(Color.rgb(145,92,56));c.drawRect(x+z*.17f,y-z*.48f,x+z*.28f,y-z*.05f,p);float hx=x+z*.225f,hy=y-z*.54f;p.setColor(Color.rgb(233,203,145));c.drawCircle(hx,hy,z*.07f,p);for(int i=0;i<4;i++){c.save();c.rotate(25+i*90,hx,hy);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(hx,hy-z*.025f,hx+z*.46f,hy+z*.025f),z*.02f,z*.02f,p);c.restore();}}
  void tree(Canvas c,float x,float y,float z){p.setColor(Color.rgb(125,85,55));c.drawRoundRect(new RectF(x-z*.05f,y-z*.42f,x+z*.05f,y),z*.02f,z*.02f,p);p.setColor(Color.rgb(248,168,205));c.drawCircle(x-z*.12f,y-z*.57f,z*.23f,p);c.drawCircle(x+z*.08f,y-z*.58f,z*.25f,p);c.drawCircle(x+z*.18f,y-z*.43f,z*.20f,p);c.drawCircle(x-z*.02f,y-z*.39f,z*.19f,p);}
  void flower(Canvas c,float x,float y,float r,int col){p.setColor(Color.rgb(70,165,65));c.drawRect(x-r*.06f,y,x+r*.06f,y+r,p);p.setColor(col);c.drawCircle(x-r*.45f,y,r*.38f,p);c.drawCircle(x+r*.45f,y,r*.38f,p);c.drawCircle(x,y-r*.42f,r*.38f,p);c.drawCircle(x,y+r*.42f,r*.38f,p);p.setColor(Color.rgb(255,208,60));c.drawCircle(x,y,r*.20f,p);}

  void demoPipes(Canvas c){float pw=w*.17f,ch=h*.03f;pipe(c,w*.14f,0,pw,h*.33f,true,ch);pipe(c,w*.70f,0,pw,h*.34f,true,ch);pipe(c,w*.16f,h*.73f,pw,h*.17f,false,ch);pipe(c,w*.68f,h*.74f,pw,h*.16f,false,ch);}
  void movingPipes(Canvas c){float pw=w*.17f,gap=h*.24f,ch=h*.03f;for(Pipe q:pipes){pipe(c,q.x,0,pw,q.g-gap/2,true,ch);pipe(c,q.x,q.g+gap/2,pw,h*.90f-(q.g+gap/2),false,ch);}}
  void pipe(Canvas c,float x,float y,float pw,float ph,boolean top,float ch){if(ph<=0)return;p.setShader(new LinearGradient(x,y,x+pw,y,Color.rgb(124,230,85),Color.rgb(45,158,56),Color.rgb(111,219,84),Shader.TileMode.CLAMP));c.drawRoundRect(new RectF(x,y,x+pw,y+ph),pw*.13f,pw*.13f,p);p.setShader(null);p.setColor(Color.argb(95,255,255,255));c.drawRoundRect(new RectF(x+pw*.15f,y+ph*.04f,x+pw*.31f,y+ph*.92f),pw*.07f,pw*.07f,p);RectF cap=top?new RectF(x-pw*.05f,y+ph-ch,x+pw*1.05f,y+ph+ch):new RectF(x-pw*.05f,y-ch,x+pw*1.05f,y+ch);p.setShader(new LinearGradient(cap.left,0,cap.right,0,Color.rgb(139,237,91),Color.rgb(50,165,57),Color.rgb(126,225,86),Shader.TileMode.CLAMP));c.drawRoundRect(cap,pw*.12f,pw*.12f,p);p.setShader(null);leaf(c,x+pw*.20f,y+ph*.52f,pw*.10f);}
  void leaf(Canvas c,float x,float y,float z){p.setColor(Color.rgb(77,176,67));path.reset();path.moveTo(x,y);path.quadTo(x-z,y-z*.45f,x-z*.35f,y-z);path.quadTo(x,y-z*.55f,x,y);path.close();c.drawPath(path,p);path.reset();path.moveTo(x,y);path.quadTo(x+z,y-z*.45f,x+z*.35f,y-z);path.quadTo(x,y-z*.55f,x,y);path.close();c.drawPath(path,p);}

  void bird(Canvas c,float x,float y,float r){
    p.setColor(Color.rgb(68,211,225));c.drawOval(new RectF(x-r*1.2f,y-r*.92f,x+r*1.12f,y+r*.92f),p);p.setColor(Color.rgb(249,247,239));c.drawOval(new RectF(x-r*.10f,y,x+r*.90f,y+r*.77f),p);
    p.setColor(Color.rgb(29,132,174));path.reset();path.moveTo(x-r*.15f,y-r*.1f);path.quadTo(x-r*1.35f,y-r*.4f,x-r*.88f,y+r*.58f);path.quadTo(x-r*.2f,y+r*.38f,x-r*.15f,y-r*.1f);path.close();c.drawPath(path,p);
    p.setColor(Color.rgb(251,215,58));path.reset();path.moveTo(x-r*.08f,y-r*.08f);path.quadTo(x-r*.85f,y-r*.08f,x-r*.72f,y+r*.25f);path.quadTo(x-r*.15f,y+r*.17f,x-r*.08f,y-r*.08f);path.close();c.drawPath(path,p);
    p.setColor(Color.rgb(68,211,225));tri(c,x-r*.18f,y-r*.78f,x+r*.10f,y-r*1.16f,x+r*.28f,y-r*.70f);tri(c,x+r*.10f,y-r*.80f,x+r*.40f,y-r*1.08f,x+r*.50f,y-r*.65f);
    p.setColor(Color.WHITE);c.drawCircle(x+r*.30f,y-r*.18f,r*.34f,p);c.drawCircle(x+r*.66f,y-r*.16f,r*.34f,p);p.setColor(Color.rgb(30,54,88));c.drawCircle(x+r*.36f,y-r*.14f,r*.15f,p);c.drawCircle(x+r*.72f,y-r*.12f,r*.15f,p);p.setColor(Color.WHITE);c.drawCircle(x+r*.40f,y-r*.19f,r*.045f,p);c.drawCircle(x+r*.76f,y-r*.17f,r*.045f,p);
    p.setColor(Color.rgb(255,158,39));tri(c,x+r*.93f,y,x+r*1.55f,y+r*.18f,x+r*.93f,y+r*.34f);p.setColor(Color.rgb(255,213,52));tri(c,x+r*.72f,y-r*.60f,x+r*1.10f,y-r*.94f,x+r*1.02f,y-r*.50f);
    sparkle(c,x-r*1.48f,y+r*.30f,r*.13f);sparkle(c,x-r*1.78f,y+r*.55f,r*.09f);
  }
  void tri(Canvas c,float x1,float y1,float x2,float y2,float x3,float y3){path.reset();path.moveTo(x1,y1);path.lineTo(x2,y2);path.lineTo(x3,y3);path.close();c.drawPath(path,p);}
  void sparkle(Canvas c,float x,float y,float r){p.setColor(Color.WHITE);c.drawRect(x-r,y-r*.12f,x+r,y+r*.12f,p);c.drawRect(x-r*.12f,y-r,x+r*.12f,y+r,p);}

  void hud(Canvas c,int sc,int be){
    p.setColor(Color.argb(80,255,255,255));c.drawOval(pause,p);p.setColor(Color.rgb(34,144,194));c.drawOval(new RectF(pause.left+w*.006f,pause.top+w*.006f,pause.right-w*.006f,pause.bottom-w*.006f),p);p.setColor(Color.WHITE);float cx=pause.centerX(),yy=pause.centerY(),bh=pause.height()*.25f,bw=pause.width()*.10f;c.drawRoundRect(new RectF(cx-bw*2.1f,yy-bh,cx-bw*.8f,yy+bh),bw,bw,p);c.drawRoundRect(new RectF(cx+bw*.8f,yy-bh,cx+bw*2.1f,yy+bh),bw,bw,p);
    RectF box=new RectF(w*.35f,h*.04f,w*.66f,h*.20f);p.setColor(Color.argb(55,255,255,255));c.drawRoundRect(box,w*.035f,w*.035f,p);p.setColor(Color.argb(175,36,138,219));c.drawRoundRect(new RectF(box.left+w*.005f,box.top+w*.005f,box.right-w*.005f,box.bottom-w*.005f),w*.032f,w*.032f,p);txt(c,"SCORE",box.centerX(),box.top+box.height()*.23f,w*.035f,Color.WHITE);txt(c,""+sc,box.centerX(),box.top+box.height()*.66f,w*.115f,Color.WHITE);RectF bp=new RectF(box.left+w*.03f,box.bottom-h*.043f,box.right-w*.03f,box.bottom-h*.009f);p.setColor(Color.argb(105,14,66,130));c.drawRoundRect(bp,bp.height()/2,bp.height()/2,p);crown(c,bp.left+bp.height()*.75f,bp.centerY(),bp.height()*.28f);txt(c,"BEST "+be,bp.centerX()+w*.025f,bp.centerY()+bp.height()*.18f,w*.035f,Color.WHITE);
    p.setColor(Color.argb(60,255,255,255));c.drawRoundRect(coinBox,coinBox.height()/2,coinBox.height()/2,p);RectF in=new RectF(coinBox.left+w*.004f,coinBox.top+w*.004f,coinBox.right-w*.004f,coinBox.bottom-w*.004f);p.setColor(Color.argb(165,38,126,197));c.drawRoundRect(in,in.height()/2,in.height()/2,p);float rr=in.height()*.34f,ccx=in.left+in.height()*.60f;p.setColor(Color.rgb(255,191,48));c.drawCircle(ccx,in.centerY(),rr,p);star(c,ccx,in.centerY(),rr*.52f);txt(c,""+coins,in.left+in.width()*.56f,in.centerY()+in.height()*.18f,w*.043f,Color.WHITE);float pr=rr*.96f,px=in.right-in.height()*.47f;p.setColor(Color.rgb(131,219,76));c.drawCircle(px,in.centerY(),pr,p);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(px-pr*.55f,in.centerY()-pr*.12f,px+pr*.55f,in.centerY()+pr*.12f),pr*.1f,pr*.1f,p);c.drawRoundRect(new RectF(px-pr*.12f,in.centerY()-pr*.55f,px+pr*.12f,in.centerY()+pr*.55f),pr*.1f,pr*.1f,p);
  }
  void crown(Canvas c,float x,float y,float z){p.setColor(Color.rgb(255,198,54));path.reset();path.moveTo(x-z,y+z*.55f);path.lineTo(x-z,y-z*.18f);path.lineTo(x-z*.4f,y+z*.05f);path.lineTo(x,y-z*.52f);path.lineTo(x+z*.4f,y+z*.05f);path.lineTo(x+z,y-z*.18f);path.lineTo(x+z,y+z*.55f);path.close();c.drawPath(path,p);}
  void star(Canvas c,float x,float y,float r){p.setColor(Color.rgb(235,155,27));path.reset();for(int i=0;i<10;i++){double a=-Math.PI/2+i*Math.PI/5;float q=i%2==0?r:r*.43f,xx=x+(float)Math.cos(a)*q,yy=y+(float)Math.sin(a)*q;if(i==0)path.moveTo(xx,yy);else path.lineTo(xx,yy);}path.close();c.drawPath(path,p);}

  void hint(Canvas c){RectF b=new RectF(w*.56f,h*.48f,w*.92f,h*.59f);p.setColor(Color.argb(242,255,250,241));c.drawRoundRect(b,w*.035f,w*.035f,p);p.setColor(Color.rgb(68,195,219));feather(c,b.left+w*.055f,b.centerY(),w*.045f);txt(c,"TAP TO",b.left+b.width()*.67f,b.top+b.height()*.43f,w*.039f,Color.rgb(26,94,145));txt(c,"FLY!",b.left+b.width()*.67f,b.top+b.height()*.79f,w*.058f,Color.rgb(55,189,214));finger(c,w*.50f,h*.68f);}
  void feather(Canvas c,float x,float y,float z){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.006f);p.setColor(Color.rgb(77,194,220));c.drawLine(x-z*.1f,y+z*.8f,x+z*.12f,y-z*.8f,p);c.drawLine(x,y-z*.5f,x+z*.55f,y-z*.15f,p);c.drawLine(x,y-z*.15f,x+z*.55f,y+z*.10f,p);c.drawLine(x,y+z*.15f,x+z*.40f,y+z*.35f,p);c.drawLine(x,y-z*.42f,x-z*.38f,y-z*.05f,p);c.drawLine(x,y-z*.05f,x-z*.42f,y+z*.25f,p);p.setStyle(Paint.Style.FILL);}
  void finger(Canvas c,float x,float y){p.setStyle(Paint.Style.STROKE);p.setStrokeWidth(w*.008f);p.setColor(Color.WHITE);c.drawRoundRect(new RectF(x-w*.018f,y-h*.015f,x+w*.018f,y+h*.042f),w*.014f,w*.014f,p);c.drawLine(x,y-h*.038f,x,y+h*.015f,p);c.drawLine(x-w*.035f,y-h*.03f,x-w*.022f,y-h*.03f,p);c.drawLine(x+w*.022f,y-h*.03f,x+w*.035f,y-h*.03f,p);c.drawLine(x,y-h*.058f,x,y-h*.046f,p);p.setStyle(Paint.Style.FILL);}
  void buttons(Canvas c){button(c,play,true);button(c,retry,false);}
  void button(Canvas c,RectF r,boolean go){p.setShader(new LinearGradient(0,r.top,0,r.bottom,go?Color.rgb(255,222,75):Color.rgb(55,196,232),go?Color.rgb(244,181,24):Color.rgb(28,134,211),Shader.TileMode.CLAMP));c.drawRoundRect(r,w*.04f,w*.04f,p);p.setShader(null);p.setColor(Color.argb(80,255,255,255));c.drawRoundRect(new RectF(r.left+w*.012f,r.top+h*.006f,r.right-w*.012f,r.centerY()),w*.03f,w*.03f,p);if(go){p.setColor(Color.WHITE);tri(c,r.centerX()-w*.025f,r.centerY()-h*.018f,r.centerX()-w*.025f,r.centerY()+h*.018f,r.centerX()+w*.035f,r.centerY());}else txt(c,"RETRY",r.centerX(),r.centerY()+r.height()*.17f,w*.047f,Color.WHITE);}
  void txt(Canvas c,String q,float x,float y,float size,int col){t.setTextAlign(Paint.Align.CENTER);t.setTextSize(size);t.setColor(col);c.drawText(q,x,y,t);}

  void tick(){long n=System.nanoTime();float dt=frame==0?.016f:Math.min(.035f,(n-frame)/1e9f);frame=n;vy+=h*1.45f*dt;by+=vy*dt;spawn-=dt;if(spawn<=0){pipes.add(new Pipe(w*1.05f,h*(.28f+rnd.nextFloat()*.36f)));spawn=1.55f;}float spd=w*.34f,pw=w*.17f,gap=h*.24f,r=w*.045f;Iterator<Pipe> it=pipes.iterator();while(it.hasNext()){Pipe q=it.next();q.x-=spd*dt;if(!q.pass&&q.x+pw<bx){q.pass=true;score++;coins++;save();}if(q.x+pw<-w*.08f)it.remove();boolean xx=bx+r>q.x&&bx-r<q.x+pw,yy=by-r<q.g-gap/2||by+r>q.g+gap/2;if(xx&&yy){end();return;}}if(by-r<0||by+r>h*.90f)end();}
  public boolean onTouchEvent(MotionEvent e){if(e.getAction()!=MotionEvent.ACTION_DOWN)return true;float x=e.getX(),y=e.getY();if(s==S.HOME){if(play.contains(x,y)||retry.contains(x,y))start();}else if(s==S.PLAY){if(pause.contains(x,y)){last=score;save();s=S.HOME;invalidate();}else vy=-h*.47f;}else{if(play.contains(x,y)||retry.contains(x,y))start();else{s=S.HOME;invalidate();}}return true;}
}
