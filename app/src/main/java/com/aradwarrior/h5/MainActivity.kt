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

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    injectGM()
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
            setOnClickListener { toggleGM() }
        }
        val btnParams = FrameLayout.LayoutParams(140, 100)
        btnParams.gravity = Gravity.TOP or Gravity.END
        btnParams.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, btnParams)
    }

    private fun js(code: String) {
        runOnUiThread { webView.evaluateJavascript(code, null) }
    }

    private fun injectGM() {
        js("""
(function(){
window.GM = window.GM || {};
var LS = (typeof cc !== 'undefined' && cc.sys && cc.sys.localStorage) ? cc.sys.localStorage : window.localStorage;
if(!GM._ready) {
GM._ready = true;
GM.level = 0;
GM.dropRate = 0;
GM.show = function(){
var p = document.getElementById('gmp');
if(!p){
p = document.createElement('div');
p.id = 'gmp';
p.style.cssText = 'position:fixed;top:60px;right:20px;width:280px;background:rgba(0,0,0,0.95);color:#fff;padding:15px;border-radius:8px;z-index:99999;font-size:12px;box-shadow:0 0 10px rgba(0,0,0,0.5);max-height:80vh;overflow-y:auto;';
var info = GM.getInfo();
p.innerHTML =
'<h3 style="margin:0 0 10px;border-bottom:1px solid #555;padding-bottom:5px;font-size:14px;">🔥 GM 控制台</h3>'+
'<div style="margin-bottom:10px;padding:8px;background:#222;border-radius:4px;font-size:11px;line-height:1.6" id="gminfo">'+info+'</div>'+
'<div style="display:grid;grid-template-columns:1fr 1fr;gap:5px">'+
'<button onclick="GM.addLevel()" style="background:#d32f2f;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">等级 +1</button>'+
'<button onclick="GM.addGold()" style="background:#f57c00;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">金币 +1w</button>'+
'<button onclick="GM.addDiamond()" style="background:#1565c0;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">钻石 +1w</button>'+
'<button onclick="GM.addDropRate()" style="background:#388e3c;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">爆率 +10%</button>'+
'<button onclick="GM.addEquip()" style="background:#7b1fa2;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">获得装备</button>'+
'<button onclick="GM.unlockAll()" style="background:#e91e63;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">解锁全部</button>'+
'</div>'+
'<button onclick="document.getElementById(\\'gmp\\').style.display=\\'none\\'" style="width:100%;margin-top:10px;background:#555;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer">关闭</button>';
document.body.appendChild(p);
}
p.style.display = p.style.display==='none'?'block':'none';
GM.refreshInfo();
};
GM.getInfo = function(){
var keys = LS? 'localStorage: OK' : 'localStorage: N/A';
var data = '';
try{
var allKeys = Object.keys(LS || {});
var gameKeys = allKeys.filter(function(k){return k.indexOf('player')>=0||k.indexOf('game')>=0||k.indexOf('data')>=0||k.indexOf('save')>=0;});
data = '游戏数据: ' + (gameKeys.length>0 ? gameKeys.join(', ') : '未检测到');
if(GM.level>0) data += '<br>GM等级加成: +'+GM.level;
if(GM.dropRate>0) data += '<br>GM爆率加成: +'+(GM.dropRate*100)+'%';
}catch(e){data='读取数据失败';}
return keys + '<br>' + data;
};
GM.refreshInfo = function(){
var el = document.getElementById('gminfo');
if(el) el.innerHTML = GM.getInfo();
};
GM.addLevel = function(){
GM.level++;
try{
var cur = parseInt(LS.getItem('player_level')||LS.getItem('level')||'0')||0;
LS.setItem('player_level', String(cur+1));
}catch(e){}
alert('✨ 等级 +1 (GM加成: +'+GM.level+')');
GM.refreshInfo();
};
GM.addGold = function(){
try{
var cur = parseInt(LS.getItem('player_gold')||LS.getItem('gold')||'0')||0;
LS.setItem('player_gold', String(cur+10000));
}catch(e){}
alert('💰 金币 +10000');
GM.refreshInfo();
};
GM.addDiamond = function(){
try{
var cur = parseInt(LS.getItem('player_diamond')||LS.getItem('diamond')||'0')||0;
LS.setItem('player_diamond', String(cur+10000));
}catch(e){}
alert('💎 钻石 +10000');
GM.refreshInfo();
};
GM.addDropRate = function(){
GM.dropRate = Math.min(GM.dropRate+0.1, 1.0);
try{ LS.setItem('drop_rate', String(GM.dropRate)); }catch(e){}
alert('📈 爆率 +10% (当前: '+(GM.dropRate*100)+'%)');
GM.refreshInfo();
};
GM.addEquip = function(){
var equips = ['史诗武器·无影剑','传说防具·重力套','神器项链·灵魂猎者','史诗戒指·骨戒','传说手镯·哈尼克'];
var item = equips[Math.floor(Math.random()*equips.length)];
try{
var inv = JSON.parse(LS.getItem('player_inventory')||LS.getItem('inventory')||'[]')||[];
inv.push({name:item,type:'equipment',rarity:'legendary'});
LS.setItem('player_inventory', JSON.stringify(inv));
}catch(e){}
alert('🎁 获得装备: '+item);
GM.refreshInfo();
};
GM.unlockAll = function(){
try{ LS.setItem('unlocked_stages', JSON.stringify([1,2,3,4,5,6,7,8,9,10])); }catch(e){}
try{ LS.setItem('unlocked_skills', 'all'); }catch(e){}
alert('🔓 已解锁全部内容');
GM.refreshInfo();
};
}
})();
        """.trimIndent())
    }

    private fun toggleGM() {
        js("window.GM&&GM.show();")
    }
}
