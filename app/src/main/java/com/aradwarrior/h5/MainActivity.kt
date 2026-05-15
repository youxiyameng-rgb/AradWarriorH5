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
    private var gmVisible = false

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

            addJavascriptInterface(GMInterface(this@MainActivity), "GM")

            webViewClient = WebViewClient()
            webChromeClient = WebChromeClient()

            loadUrl("file:///android_asset/game/index.html")
        }

        layout.addView(webView, FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT)

        // 悬浮 GM 按钮
        val gmBtn = Button(this).apply {
            text = "GM"
            setTextColor(Color.WHITE)
            setBackgroundColor(Color.argb(180, 255, 0, 0))
            textSize = 12f
            gravity = Gravity.CENTER
            setOnClickListener { toggleGMPanel() }
        }
        val btnParams = FrameLayout.LayoutParams(140, 100)
        btnParams.gravity = Gravity.TOP or Gravity.END
        btnParams.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, btnParams)
    }

    private fun toggleGMPanel() {
        gmVisible = !gmVisible
        val action = if (gmVisible) "block" else "none"

        webView.evaluateJavascript("""
            (function() {
                var panel = document.getElementById('gm-panel');
                if (!panel) {
                    panel = document.createElement('div');
                    panel.id = 'gm-panel';
                    panel.style.cssText = 'position:fixed;top:60px;right:20px;width:260px;background:rgba(0,0,0,0.95);color:white;padding:15px;border-radius:8px;z-index:9999;font-size:12px;display:none;box-shadow:0 0 10px rgba(0,0,0,0.5);max-height:80vh;overflow-y:auto;';
                    panel.innerHTML = [
                        '<h3 style="margin:0 0 10px 0;border-bottom:1px solid #555;padding-bottom:5px;font-size:14px;">🔥 GM 控制台</h3>',
                        '<div style="display:grid;grid-template-columns:1fr 1fr;gap:5px;">',
                        '<button onclick="GM.setLevel(999)" style="background:#d32f2f;color:white;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer;">等级 999</button>',
                        '<button onclick="GM.setGold(99999)" style="background:#f57c00;color:white;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer;">金币 +9w</button>',
                        '<button onclick="GM.setDiamond(99999)" style="background:#1565c0;color:white;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer;">钻石 +9w</button>',
                        '<button onclick="GM.unlockAll()" style="background:#7b1fa2;color:white;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer;">解锁全部</button>',
                        '</div>',
                        '<button onclick="document.getElementById(\'gm-panel\').style.display=\'none\'" style="width:100%;margin-top:10px;background:#555;color:white;border:none;padding:8px;border-radius:4px;font-size:12px;cursor:pointer;">关闭</button>'
                    ].join('\n');
                    document.body.appendChild(panel);
                }
                panel.style.display = '$action';
            })()
        """.trimIndent(), null)
    }

    class GMInterface(private val activity: MainActivity) {
        private fun exec(js: String) {
            activity.runOnUiThread {
                activity.webView.evaluateJavascript(js, null)
            }
        }

        @JavascriptInterface
        fun setLevel(level: Int) {
            exec("alert('✨ 等级已设为 " + level + "！');")
            Toast.makeText(activity, "GM: 等级已设为 " + level, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun setGold(gold: Int) {
            exec("alert('💰 获得金币 " + gold + "！');")
            Toast.makeText(activity, "GM: 添加金币 " + gold, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun setDiamond(count: Int) {
            exec("alert('💎 获得钻石 " + count + "！');")
            Toast.makeText(activity, "GM: 添加钻石 " + count, Toast.LENGTH_SHORT).show()
        }

        @JavascriptInterface
        fun unlockAll() {
            exec("alert('🔓 已解锁全部内容！');")
            Toast.makeText(activity, "GM: 已解锁全部", Toast.LENGTH_SHORT).show()
        }
    }
}