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

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.addFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN or
            WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
        )

        val layout = FrameLayout(this).apply { setBackgroundColor(Color.BLACK) }
        setContentView(layout)

        webView = WebView(this).apply {
            settings.apply {
                javaScriptEnabled = true
                domStorageEnabled = true
                allowFileAccess = true
                allowFileAccessFromFileURLs = true
                allowUniversalAccessFromFileURLs = true
                allowContentAccess = true
                cacheMode = WebSettings.LOAD_NO_CACHE
            }

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()
            loadUrl("file:///android_asset/game/index.html")
        }

        layout.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        val gmBtn = Button(this).apply {
            text = "GM"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(180, 255, 0, 0))
            textSize = 12f
            gravity = Gravity.CENTER
            setOnClickListener { toggleGM() }
        }
        val btnParams = FrameLayout.LayoutParams(140, 100)
        btnParams.gravity = Gravity.TOP or Gravity.END
        btnParams.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, btnParams)
    }

    private fun toggleGM() {
        val code = "" +
            "if(!window.gg){" +
            "window.gg={};" +
            "window.gg.show=function(){" +
            "var p=document.getElementById('ggp');" +
            "if(!p){" +
            "p=document.createElement('div');" +
            "p.id='ggp';" +
            "p.style.cssText='position:fixed;top:60px;right:10px;width:260px;background:rgba(0,0,0,0.95);color:white;padding:12px;border-radius:8px;z-index:99999;font-size:13px;max-height:85vh;overflow-y:auto;';" +
            "p.innerHTML=" +
            "'<div style=\"margin-bottom:8px;padding-bottom:6px;border-bottom:1px solid #555;font-size:15px;font-weight:bold;color:#ff6b6b\">GM</div>'+" +
            "'<button onclick=\"window.gg.cmd(\\'level\\')\" style=\"display:block;width:100%;margin:4px 0;background:#e74c3c;color:white;border:none;padding:10px;border-radius:6px\">Level +1</button>'+" +
            "'<button onclick=\"window.gg.cmd(\\'gold\\')\" style=\"display:block;width:100%;margin:4px 0;background:#e67e22;color:white;border:none;padding:10px;border-radius:6px\">Gold +10000</button>'+" +
            "'<button onclick=\"window.gg.cmd(\\'diamond\\')\" style=\"display:block;width:100%;margin:4px 0;background:#2980b9;color:white;border:none;padding:10px;border-radius:6px\">Diamond +10000</button>'+" +
            "'<button onclick=\"window.gg.cmd(\\'drop\\')\" style=\"display:block;width:100%;margin:4px 0;background:#27ae60;color:white;border:none;padding:10px;border-radius:6px\">Drop +10%</button>'+" +
            "'<button onclick=\"window.gg.cmd(\\'equip\\')\" style=\"display:block;width:100%;margin:4px 0;background:#8e44ad;color:white;border:none;padding:10px;border-radius:6px\">Get Equip</button>'+" +
            "'<button onclick=\"window.gg.cmd(\\'unlock\\')\" style=\"display:block;width:100%;margin:4px 0;background:#2c3e50;color:white;border:none;padding:10px;border-radius:6px\">Unlock All</button>'+" +
            "'<button onclick=\"this.parentElement.style.display=\\'none\\'\" style=\"display:block;width:100%;margin-top:8px;background:#555;color:white;border:none;padding:8px;border-radius:6px\">Close</button>';" +
            "document.body.appendChild(p);" +
            "}" +
            "p.style.display=(p.style.display==='none'||!p.style.display)?'block':'none';" +
            "};" +
            "window.gg.cmd=function(a){" +
            "var db=(typeof cc!=='undefined'&&cc.sys&&cc.sys.localStorage)?cc.sys.localStorage:window.localStorage;" +
            "var g=function(k,d){try{return JSON.parse(db.getItem(k))||d}catch(e){return db.getItem(k)||d}};" +
            "var s=function(k,v){try{db.setItem(k,typeof v==='string'?v:JSON.stringify(v))}catch(e){}}" +
            "if(a==='level'){var l=parseInt(g('player_level',0));s('player_level',l+1);alert('Level: '+(l+1));}" +
            "if(a==='gold'){var n=parseInt(g('player_gold',0));s('player_gold',n+10000);alert('Gold: '+(n+10000));}" +
            "if(a==='diamond'){var n=parseInt(g('player_diamond',0));s('player_diamond',n+10000);alert('Diamond: '+(n+10000));}" +
            "if(a==='drop'){var r=parseFloat(g('drop_rate',0));s('drop_rate',Math.min(r+0.1,1.0));alert('Drop: '+((r+0.1)*100).toFixed(0)+'%');}" +
            "if(a==='equip'){var items=['Epic Sword','Legend Armor','Mythic Ring'];var item=items[Math.floor(Math.random()*items.length)];var inv=g('player_inventory',[]);inv.push({name:item});s('player_inventory',inv);alert('Got: '+item);}" +
            "if(a==='unlock'){s('unlocked_stages',[1,2,3,4,5,6,7,8,9,10]);s('unlocked_skills','all');alert('Unlocked All!');}" +
            "};" +
            "}" +
            "window.gg.show();"
        webView.evaluateJavascript(code, null)
    }
}
