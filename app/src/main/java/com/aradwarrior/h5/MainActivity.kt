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
function getDM(){try{for(var k in window){if(window[k]&&window[k].CommonDataMgr)return window[k].CommonDataMgr.instance}for(var k in window){var v=window[k];if(v&&v.instance&&v.instance.Gold!==undefined)return v.instance}}catch(e){}return null}
var div=document.createElement('div');
div.id='_gmp';
div.style.cssText='position:fixed;top:60px;right:10px;width:260px;background:rgba(0,0,0,0.95);color:white;padding:12px;border-radius:8px;z-index:99999;font-size:13px;max-height:85vh;overflow-y:auto;display:none';
var title=document.createElement('div');
title.style.cssText='margin-bottom:8px;padding-bottom:6px;border-bottom:1px solid #555;font-size:15px;font-weight:bold;color:#ff6b6b';
title.textContent='GM CONTROL';
div.appendChild(title);
var info=document.createElement('div');
info.id='gmstat';
info.style.cssText='margin-bottom:8px;font-size:11px;color:#aaa;line-height:1.5';
info.textContent='loading...';
div.appendChild(info);
var btns=[['Level +1','level'],['Gold +1w','gold'],['Diamond +1w','diamond'],['Drop +10%','drop'],['Get Equip','equip'],['Unlock All','unlock']];
var colors=['#e74c3c','#e67e22','#2980b9','#27ae60','#8e44ad','#2c3e50'];
btns.forEach(function(b,i){
var btn=document.createElement('button');
btn.textContent=b[0];
btn.style.cssText='display:block;width:100%;margin:4px 0;background:'+colors[i]+';color:white;border:none;padding:10px;border-radius:6px;font-size:13px';
btn.onclick=function(){
var cmd=b[1];
var dm=getDM();
if(!dm){alert('DataMgr not found');return}
if(cmd=='level'){dm.Level=dm.Level||0;dm.Level++;alert('Level: '+dm.Level)}
else if(cmd=='gold'){dm.Gold=dm.Gold||0;dm.Gold+=10000;alert('Gold: '+dm.Gold)}
else if(cmd=='diamond'){dm.Diamond=dm.Diamond||0;dm.Diamond+=10000;alert('Diamond: '+dm.Diamond)}
else if(cmd=='drop'){dm.DropRate=Math.min((dm.DropRate||0)+0.1,1);alert('Drop: '+((dm.DropRate)*100).toFixed(0)+'%')}
else if(cmd=='equip'){var items=['Epic Sword','Legend Armor','Mythic Ring'];var item=items[Math.floor(Math.random()*items.length)];dm.Inventory=dm.Inventory||[];dm.Inventory.push({name:item});alert('Got: '+item)}
else if(cmd=='unlock'){dm.Unlocked=dm.Unlocked||{};for(var i=1;i<=10;i++)dm.Unlocked[i]=true;alert('Unlocked All!')}
refreshStat()};
div.appendChild(btn);
});
function refreshStat(){
var el=document.getElementById('gmstat');
var dm=getDM();
if(!el)return;
if(!dm){el.textContent='DataMgr not found - try playing a level first!';return}
el.innerHTML='Gold: '+(dm.Gold||0)+'<br>Diamond: '+(dm.Diamond||0)+'<br>Level: '+(dm.Level||0)+'<br>Drop: '+((dm.DropRate||0)*100).toFixed(0)+'%';
}
var closeBtn=document.createElement('button');
closeBtn.textContent='Close';
closeBtn.style.cssText='display:block;width:100%;margin-top:8px;background:#555;color:white;border:none;padding:8px;border-radius:6px;font-size:12px';
closeBtn.onclick=function(){div.style.display='none'};
div.appendChild(closeBtn);
document.body.appendChild(div);
window._gm=div;
refreshStat();
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
