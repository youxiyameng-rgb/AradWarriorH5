package com.aradwarrior.h5

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var gmInjected = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        val layout = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(layout)
        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true; domStorageEnabled = true
                allowFileAccess = true; allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true; allowContentAccess = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }
            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl("file:///android_asset/game/index.html")
        }
        layout.addView(webView, FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT))
        val gmBtn = Button(this).apply {
            text = "GM"; setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(180, 255, 0, 0)); textSize = 12f; gravity = Gravity.CENTER
            setOnClickListener { toggleGM() }
        }
        val p = FrameLayout.LayoutParams(140, 100)
        p.gravity = Gravity.TOP or Gravity.END
        p.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, p)
    }

    private fun js(s: String) = runOnUiThread { webView.evaluateJavascript(s, null) }

    private fun toggleGM() {
        js("""
(function(){
if(!window._gm){
var store = (typeof cc!='undefined'&&cc.sys&&cc.sys.localStorage)?cc.sys.localStorage:window.localStorage;
function get(k,d){try{return JSON.parse(store.getItem(k))||d}catch(e){return store.getItem(k)||d}}
function set(k,v){try{store.setItem(k,typeof v=='string'?v:JSON.stringify(v))}catch(e){}}
var div=document.createElement('div');
div.id='_gmp';
div.style.cssText='position:fixed;top:60px;right:10px;width:260px;background:rgba(0,0,0,0.95);color:white;padding:12px;border-radius:8px;z-index:99999;font-size:13px;max-height:85vh;overflow-y:auto;display:none';
var title=document.createElement('div');
title.style.cssText='margin-bottom:8px;padding-bottom:6px;border-bottom:1px solid #555;font-size:15px;font-weight:bold;color:#ff6b6b';
title.textContent='GM CONTROL';
div.appendChild(title);
var btns=[['Level +1','level'],['Gold +1w','gold'],['Diamond +1w','diamond'],['Drop +10%','drop'],['Get Equip','equip'],['Unlock All','unlock']];
var colors=['#e74c3c','#e67e22','#2980b9','#27ae60','#8e44ad','#2c3e50'];
btns.forEach(function(b,i){
var btn=document.createElement('button');
btn.textContent=b[0];
btn.style.cssText='display:block;width:100%;margin:4px 0;background:'+colors[i]+';color:white;border:none;padding:10px;border-radius:6px;font-size:13px';
btn.onclick=function(){
var cmd=b[1];
if(cmd=='level'){var l=parseInt(get('player_level',0));set('player_level',l+1);alert('Level: '+(l+1))}
else if(cmd=='gold'){var n=parseInt(get('player_gold',0));set('player_gold',n+10000);alert('Gold: '+(n+10000))}
else if(cmd=='diamond'){var n=parseInt(get('player_diamond',0));set('player_diamond',n+10000);alert('Diamond: '+(n+10000))}
else if(cmd=='drop'){var r=parseFloat(get('drop_rate',0));set('drop_rate',Math.min(r+0.1,1.0));alert('Drop: '+((r+0.1)*100).toFixed(0)+'%')}
else if(cmd=='equip'){var items=['Epic Sword','Legend Armor','Mythic Ring'];var item=items[Math.floor(Math.random()*items.length)];var inv=get('player_inventory',[]);inv.push({name:item});set('player_inventory',inv);alert('Got: '+item)}
else if(cmd=='unlock'){set('unlocked_stages',[1,2,3,4,5,6,7,8,9,10]);set('unlocked_skills','all');alert('Unlocked All!')}
};
div.appendChild(btn);
});
var closeBtn=document.createElement('button');
closeBtn.textContent='Close';
closeBtn.style.cssText='display:block;width:100%;margin-top:8px;background:#555;color:white;border:none;padding:8px;border-radius:6px;font-size:12px';
closeBtn.onclick=function(){div.style.display='none'};
div.appendChild(closeBtn);
document.body.appendChild(div);
window._gm=div;
}
window._gm.style.display=(window._gm.style.display=='none'||!window._gm.style.display)?'block':'none';
})();
        """.trimIndent())
    }
}
