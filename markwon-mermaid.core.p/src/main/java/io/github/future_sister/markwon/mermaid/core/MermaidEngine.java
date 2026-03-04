package io.github.future_sister.markwon.mermaid.core;

import android.graphics.Bitmap;

/**
 * Mermaid rendering engine interface.
 * Encapsulates different rendering implementations (e.g., hggz/Mermaid-Android).
 */
public interface MermaidEngine {
    /**
     * Render Mermaid diagram code to a Bitmap.
     *
     * @param mermaidCode The Mermaid DSL string
     * @return Bitmap representation of the diagram
     * @throws IllegalArgumentException if code is invalid
     * @throws RuntimeException if rendering fails
     */
    Bitmap renderToBitmap(String mermaidCode);

    /**
     * Render Mermaid diagram code to PNG byte array.
     *
     * @param mermaidCode The Mermaid DSL string
     * @return PNG encoded bytes
     * @throws RuntimeException if rendering fails
     */
    byte[] renderToPNG(String mermaidCode);

    /**
     * Check if the provided Mermaid code is valid syntax.
     *
     * @param mermaidCode The Mermaid DSL string
     * @return true if valid, false otherwise
     */
    boolean isValidSyntax(String mermaidCode);
}