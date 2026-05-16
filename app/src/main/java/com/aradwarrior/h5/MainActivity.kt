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
                mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
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

    private val gmJs = """
(function(){
var panel = document.getElementById('_gmPanel');
if(panel) { panel.style.display = panel.style.display === 'none' ? 'block' : 'none'; return; }

// 注入全局GM函数 - 每次读window上的最新数据
window.GM = {
  get dm() { return window.CommonDataMgr; },
  levelUp: function() { var d = window.CommonDataMgr; if(d){ d.Level = (d.Level||0)+1; } },
  addGold: function(n) { var d = window.CommonDataMgr; if(d){ d.Gold = (d.Gold||0)+(n||10000); } },
  addDiamond: function(n) { var d = window.CommonDataMgr; if(d){ d.Diamond = (d.Diamond||0)+(n||10000); } },
  addPower: function(n) { var d = window.CommonDataMgr; if(d){ d.Poewrpoewr = (d.Poewrpoewr||0)+(n||10); } },
  addExtra: function(n) { var d = window.CommonDataMgr; if(d){ d.Extrapoewr = (d.Extrapoewr||0)+(n||50); } },
  addRevive: function(n) { var d = window.CommonDataMgr; if(d){ d.ReviveCoin = (d.ReviveCoin||0)+(n||10); } },
  stat: function() {
    var d = window.CommonDataMgr;
    if(!d) return null;
    return { Gold:d.Gold||0, Diamond:d.Diamond||0, Level:d.Level||0, Power:d.Poewrpoewr||0, Extra:d.Extrapoewr||0, Revive:d.ReviveCoin||0 };
  }
};

panel = document.createElement('div');
panel.id = '_gmPanel';
panel.style.cssText = 'position:fixed;top:60px;right:12px;width:250px;background:rgba(0,0,0,0.92);color:#fff;padding:12px;border-radius:10px;z-index:99999;font:13px/1.5 sans-serif;max-height:85vh;overflow-y:auto';

var h = document.createElement('div');
h.style.cssText = 'margin-bottom:10px;padding-bottom:6px;border-bottom:1px solid #444;font-size:16px;font-weight:bold;color:#ff6b6b';
h.textContent = 'GM CONTROL';
panel.appendChild(h);

var st = document.createElement('div');
st.id = '_gmStat';
st.style.cssText = 'margin-bottom:8px;font-size:11px;color:#aaa;line-height:1.6';
panel.appendChild(st);

var btns = [
  ['Level +1', 'GM.levelUp()'],
  ['Gold +10w', 'GM.addGold(100000)'],
  ['Diamond +10w', 'GM.addDiamond(100000)'],
  ['Power +100', 'GM.addPower(100)'],
  ['Extra Power +500', 'GM.addExtra(500)'],
  ['Revive +100', 'GM.addRevive(100)'],
  ['Gold +9999999', 'GM.addGold(9999999)'],
  ['Diamond +9999999', 'GM.addDiamond(9999999)']
];
var colors = '#e74c3c,#e67e22,#f39c12,#27ae60,#2ecc71,#3498db,#9b59b6,#8e44ad'.split(',');

btns.forEach(function(b,i){
  var btn = document.createElement('button');
  btn.textContent = b[0];
  btn.style.cssText = 'display:block;width:100%;margin:3px 0;background:'+colors[i]+';color:#fff;border:none;padding:8px;border-radius:5px;font-size:12px;cursor:pointer';
  btn.onclick = function(){
    try {
      eval(b[1]);
      var s = GM.stat();
      if(s) document.getElementById('_gmStat').innerHTML = 'Gold: '+s.Gold+'<br>Diamond: '+s.Diamond+'<br>Level: '+s.Level+'<br>Power: '+s.Power+'<br>Extra: '+s.Extra+'<br>Revive: '+s.Revive;
    } catch(e){ alert('GM Error: '+e.message); }
  };
  panel.appendChild(btn);
});

var c = document.createElement('button');
c.textContent = 'Close';
c.style.cssText = 'display:block;width:100%;margin-top:8px;background:#555;color:#fff;border:none;padding:8px;border-radius:5px;font-size:12px';
c.onclick = function(){ panel.style.display = 'none'; };
panel.appendChild(c);

document.body.appendChild(panel);

// 初始状态
var s = GM.stat();
if(s) st.innerHTML = 'Gold: '+s.Gold+'<br>Diamond: '+s.Diamond+'<br>Level: '+s.Level+'<br>Power: '+s.Power+'<br>Extra: '+s.Extra+'<br>Revive: '+s.Revive;
else st.textContent = 'DataMgr not found. Enter game first!';
})();
    """.trimIndent()

    private fun toggleGM() {
        js(gmJs)
    }
}
