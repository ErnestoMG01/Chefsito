package com.example.chefsito.ui.components

import android.annotation.SuppressLint
import android.graphics.Color
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthLogoWebView(
    modifier: Modifier = Modifier,
    assetName: String = "logo_card_static.html"
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )

                // Fondo transparente
                setBackgroundColor(Color.TRANSPARENT)

                // Ajustes de permisos y carga de archivos locales para WebView
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true

                // Ocultar barras de desplazamiento
                isVerticalScrollBarEnabled = false
                isHorizontalScrollBarEnabled = false
                settings.displayZoomControls = false

                webViewClient = WebViewClient()

                loadUrl("file:///android_asset/$assetName")
            }
        }
    )
}
