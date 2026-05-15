package com.aradwarrior.h5

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.WindowManager
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Button
import android.widget.FrameLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    @SuppressLint("SetJavaScriptEnabled", "AddJavascriptInterface")
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

            addJavascriptInterface(GMBridge(), "_GM")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    view?.evaluateJavascript(GM_JS, null)
                }
            }
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
            setOnClickListener {
                webView.evaluateJavascript("if(window.GM)GM.show();", null)
            }
        }
        val btnParams = FrameLayout.LayoutParams(140, 100)
        btnParams.gravity = Gravity.TOP or Gravity.END
        btnParams.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, btnParams)
    }

    inner class GMBridge {
        @JavascriptInterface
        fun toast(msg: String) {
            runOnUiThread { Toast.makeText(this@MainActivity, "GM: $msg", Toast.LENGTH_SHORT).show() }
        }
    }

    companion object {
        private val GM_JS = """
(function(){
if(window.GM)return;
var LS = (typeof cc !== 'undefined' && cc.sys && cc.sys.localStorage) ? cc.sys.localStorage : window.localStorage;
var db = LS;

window.GM = {};

GM.get = function(k, def) {
    try { return JSON.parse(db.getItem(k)) || def; } catch(e) { return db.getItem(k) || def; }
};
GM.set = function(k, v) {
    try { db.setItem(k, typeof v === 'string' ? v : JSON.stringify(v)); } catch(e) {}
    _GM.toast(k + ' updated');
};

GM.show = function() {
    var p = document.getElementById('gmp');
    if(!p) {
        p = document.createElement('div');
        p.id = 'gmp';
        p.style.cssText = 'position:fixed;top:60px;right:10px;width:260px;background:rgba(20,20,20,0.97);color:#fff;padding:12px;border-radius:8px;z-index:99999;font-size:13px;box-shadow:0 4px 20px rgba(0,0,0,0.6);max-height:85vh;overflow-y:auto;';
        p.innerHTML =
            '<div style="margin-bottom:8px;padding-bottom:6px;border-bottom:1px solid #444;font-size:15px;font-weight:bold;color:#ff6b6b">GM CONTROL</div>' +
            '<div style="margin-bottom:8px;font-size:11px;color:#aaa;line-height:1.5" id="gminfo">loading...</div>' +
            '<button class="gmbtn" data-cmd="level" style="display:block;width:100%;margin:4px 0;background:#e74c3c;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Level +1</button>' +
            '<button class="gmbtn" data-cmd="gold" style="display:block;width:100%;margin:4px 0;background:#e67e22;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Gold +10000</button>' +
            '<button class="gmbtn" data-cmd="diamond" style="display:block;width:100%;margin:4px 0;background:#2980b9;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Diamond +10000</button>' +
            '<button class="gmbtn" data-cmd="drop" style="display:block;width:100%;margin:4px 0;background:#27ae60;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Drop Rate +10%</button>' +
            '<button class="gmbtn" data-cmd="equip" style="display:block;width:100%;margin:4px 0;background:#8e44ad;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Get Random Equipment</button>' +
            '<button class="gmbtn" data-cmd="unlock" style="display:block;width:100%;margin:4px 0;background:#2c3e50;color:#fff;border:none;padding:10px;border-radius:6px;font-size:13px">Unlock All</button>' +
            '<button onclick="this.parentElement.style.display=\\'none\\'" style="display:block;width:100%;margin-top:8px;background:#555;color:#fff;border:none;padding:8px;border-radius:6px;font-size:12px">Close</button>';
        document.body.appendChild(p);
        p.addEventListener('click', function(e) {
            var btn = e.target.closest('.gmbtn');
            if(btn) GM.exec(btn.getAttribute('data-cmd'));
        });
    }
    p.style.display = (p.style.display === 'none') ? 'block' : 'none';
    GM.refresh();
};

GM.exec = function(cmd) {
    switch(cmd) {
        case 'level':
            var lv = parseInt(GM.get('player_level', 0));
            GM.set('player_level', lv + 1);
            break;
        case 'gold':
            var g = parseInt(GM.get('player_gold', 0));
            GM.set('player_gold', g + 10000);
            break;
        case 'diamond':
            var d = parseInt(GM.get('player_diamond', 0));
            GM.set('player_diamond', d + 10000);
            break;
        case 'drop':
            var dr = parseFloat(GM.get('drop_rate', 0));
            GM.set('drop_rate', Math.min(dr + 0.1, 1.0));
            break;
        case 'equip':
            var items = ['Epic Sword','Legendary Armor','Mythic Ring','Ancient Necklace','Godly Shield'];
            var item = items[Math.floor(Math.random() * items.length)];
            var inv = GM.get('player_inventory', []);
            inv.push({name:item, time:Date.now()});
            GM.set('player_inventory', inv);
            break;
        case 'unlock':
            GM.set('unlocked_stages', [1,2,3,4,5,6,7,8,9,10]);
            GM.set('unlocked_skills', 'all');
            break;
    }
    GM.refresh();
};

GM.refresh = function() {
    var el = document.getElementById('gminfo');
    if(!el) return;
    var info = [];
    try {
        info.push('Level: ' + GM.get('player_level', 0));
        info.push('Gold: ' + GM.get('player_gold', 0));
        info.push('Diamond: ' + GM.get('player_diamond', 0));
        info.push('Drop Rate: ' + (parseFloat(GM.get('drop_rate', 0)) * 100).toFixed(0) + '%');
        var inv = GM.get('player_inventory', []);
        info.push('Equipment: ' + inv.length + ' items');
        var stages = GM.get('unlocked_stages', []);
        info.push('Unlocked: ' + (stages.length > 0 ? stages.length + ' stages' : 'none'));
    } catch(e) {
        info.push('Error reading data');
    }
    el.innerHTML = info.join('<br>');
};
})();
        """.trimIndent()
    }
}
