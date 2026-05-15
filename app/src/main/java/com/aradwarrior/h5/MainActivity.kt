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

            addJavascriptInterface(object {
                @JavascriptInterface
                fun gmDo(cmd: String) {
                    runOnUiThread {
                        Toast.makeText(this@MainActivity, "GM: " + cmd, Toast.LENGTH_SHORT).show()
                    }
                }
            }, "_GM")

            webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, url: String?) {
                    super.onPageFinished(view, url)
                    runOnUiThread {
                        webView.evaluateJavascript("""
                            (function() {
                                window.GM = {
                                    show: function() {
                                        var p = document.getElementById('gmp');
                                        if(!p) {
                                            p = document.createElement('div');
                                            p.id = 'gmp';
                                            p.style.cssText = 'position:fixed;top:60px;right:20px;width:280px;background:rgba(0,0,0,0.95);color:#fff;padding:15px;border-radius:8px;z-index:99999;font-size:12px;box-shadow:0 0 10px rgba(0,0,0,0.5);max-height:80vh;overflow-y:auto;';
                                            var b = [
                                                '<h3 style="margin:0 0 10px;border-bottom:1px solid #555;padding-bottom:5px;font-size:14px;">GM Control</h3>',
                                                '<button onclick="_GM.gmDo(\'level\')" style="display:block;width:100%;margin:3px 0;background:#d32f2f;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Level +1</button>',
                                                '<button onclick="_GM.gmDo(\'gold\')" style="display:block;width:100%;margin:3px 0;background:#f57c00;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Gold +1w</button>',
                                                '<button onclick="_GM.gmDo(\'diamond\')" style="display:block;width:100%;margin:3px 0;background:#1565c0;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Diamond +1w</button>',
                                                '<button onclick="_GM.gmDo(\'drop\')" style="display:block;width:100%;margin:3px 0;background:#388e3c;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Drop Rate +10%</button>',
                                                '<button onclick="_GM.gmDo(\'equip\')" style="display:block;width:100%;margin:3px 0;background:#7b1fa2;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Get Equipment</button>',
                                                '<button onclick="document.getElementById(\'gmp\').style.display=\'none\'" style="display:block;width:100%;margin-top:8px;background:#555;color:#fff;border:none;padding:10px;border-radius:4px;font-size:13px">Close</button>'
                                            ].join('');
                                            p.innerHTML = b;
                                            document.body.appendChild(p);
                                        }
                                        p.style.display = (p.style.display === 'none' || p.style.display === '') ? 'block' : 'none';
                                    }
                                };
                            })();
                        """.trimIndent(), null)
                    }
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
                webView.evaluateJavascript("if(window.GM)GM.show();else alert('GM not ready');", null)
            }
        }
        val btnParams = FrameLayout.LayoutParams(140, 100)
        btnParams.gravity = Gravity.TOP or Gravity.END
        btnParams.setMargins(0, 100, 0, 0)
        layout.addView(gmBtn, btnParams)
    }
}
