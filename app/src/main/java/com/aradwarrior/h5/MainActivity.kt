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

        val layout = FrameLayout(this).apply {
            setBackgroundColor(Color.BLACK)
        }
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

            addJavascriptInterface(object {
                @JavascriptInterface
                fun doAction(action: String) {
                    runOnUiThread {
                        val title = when (action) {
                            "level" -> "✨ 等级已设为 999"
                            "gold" -> "💰 获得金币 99999"
                            "diamond" -> "💎 获得钻石 99999"
                            "unlock" -> "🔓 已解锁全部内容"
                            else -> action
                        }
                        webView.evaluateJavascript("alert('$title');", null)
                    }
                }
            }, "_GM")

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

    private fun injectGM() {
        webView.evaluateJavascript("""
            window.gmToggle = function() { _GM.doAction('toggle'); };
            window.GM = {
                show: function() {
                    var p = document.getElementById('gmp');
                    if(!p) {
                        p = document.createElement('div');
                        p.id = 'gmp';
                        p.style.cssText = 'position:fixed;top:60px;right:20px;width:260px;background:rgba(0,0,0,0.95);color:#fff;padding:15px;border-radius:8px;z-index:99999;font-size:12px;box-shadow:0 0 10px rgba(0,0,0,0.5);max-height:80vh;overflow-y:auto;';
                        p.innerHTML = '<h3 style="margin:0 0 10px;border-bottom:1px solid #555;padding-bottom:5px;font-size:14px;">🔥 GM 控制台</h3>'+
                            '<div style="display:grid;grid-template-columns:1fr 1fr;gap:5px">'+
                            '<button onclick="_GM.doAction(\'level\')" style="background:#d32f2f;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px">等级 999</button>'+
                            '<button onclick="_GM.doAction(\'gold\')" style="background:#f57c00;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px">金币 +9w</button>'+
                            '<button onclick="_GM.doAction(\'diamond\')" style="background:#1565c0;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px">钻石 +9w</button>'+
                            '<button onclick="_GM.doAction(\'unlock\')" style="background:#7b1fa2;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px">解锁全部</button>'+
                            '</div>'+
                            '<button onclick="document.getElementById(\'gmp\').style.display=\'none\'" style="width:100%;margin-top:10px;background:#555;color:#fff;border:none;padding:8px;border-radius:4px;font-size:12px">关闭</button>';
                        document.body.appendChild(p);
                    }
                    p.style.display = p.style.display==='none'?'block':'none';
                }
            };
        """.trimIndent(), null)
    }

    private fun toggleGM() {
        webView.evaluateJavascript("window.GM&&GM.show();", null)
    }
}
