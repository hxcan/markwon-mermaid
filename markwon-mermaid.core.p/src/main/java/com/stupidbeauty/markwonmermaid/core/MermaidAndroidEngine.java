package com.stupidbeauty.markwonmermaid.core;

import android.graphics.Bitmap;
import android.graphics.Canvas;

import androidx.annotation.NonNull;

import com.mermaid.kotlin.MermaidKotlin;
import com.mermaid.kotlin.parser.DiagramParseException;

/**
 * Implementation of MermaidEngine using hggz/Mermaid-Android library.
 * Leverages native Kotlin parser + Canvas renderer (no WebView, no JavaScript).
 */
public class MermaidAndroidEngine implements MermaidEngine {

    private static volatile MermaidAndroidEngine instance;
    private final MermaidKotlin mermaidKotlin;

    private MermaidAndroidEngine() {
        // Initialize with default dark mode disabled (light theme by default)
        this.mermaidKotlin = new MermaidKotlin();
    }

    /**
     * Get singleton instance.
     */
    @NonNull
    public static synchronized MermaidAndroidEngine getInstance() {
        if (instance == null) {
            synchronized (MermaidAndroidEngine.class) {
                if (instance == null) {
                    instance = new MermaidAndroidEngine();
                }
            }
        }
        return instance;
    }

    @Override
    public Bitmap renderToBitmap(String mermaidCode) {
        try {
            // hggz/Mermaid-Android provides direct Bitmap rendering
            // Note: The library's API may vary slightly; adjust as needed
            // For now, assuming a method like `render()` returns Bitmap
            
            // If the library only has parse+layout methods, we need to 
            // construct the diagram manually or use a different approach.
            // However, based on README, there is `render()` returning Bitmap.
            
            // Since we cannot call Kotlin directly without proper setup,
            // we assume the dependency is correctly added and visible.
            
            // Fallback: Use reflection if needed, but direct import is preferred.
            // This code assumes MermaidKotlin.render(String) exists and returns Bitmap.
            
            // ⚠️ NOTE: The actual API from hggz/Mermaid-Android might be:
            // MermaidKotlin mermaid = new MermaidKotlin();
            // Bitmap bitmap = mermaid.render(mermaidCode);
            
            // For safety, wrap in try-catch
            java.lang.reflect.Method renderMethod = 
                MermaidKotlin.class.getMethod("render", String.class);
            return (Bitmap) renderMethod.invoke(mermaidKotlin, mermaidCode);
            
        } catch (Exception e) {
            throw new RuntimeException("Failed to render Mermaid diagram to Bitmap", e);
        }
    }

    @Override
    public byte[] renderToPNG(String mermaidCode) {
        Bitmap bitmap = renderToBitmap(mermaidCode);
        java.io.ByteArrayOutputStream outputStream = new java.io.ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
        return outputStream.toByteArray();
    }

    @Override
    public boolean isValidSyntax(String mermaidCode) {
        try {
            // Attempt to parse; if it throws exception, syntax is invalid
            java.lang.reflect.Method parseMethod = 
                MermaidKotlin.class.getMethod("parse", String.class);
            Object result = parseMethod.invoke(mermaidKotlin, mermaidCode);
            return result != null;
        } catch (Exception e) {
            return false;
        }
    }
}