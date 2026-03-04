package com.stupidbeauty.markwonmermaid;

import android.text.Spannable;
import android.text.style.ImageSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import io.noties.markwon.AbstractMarkwonPlugin;
import io.noties.markwon.MarkwonSpansFactory;
import io.noties.markwon.core.CoreSyntaxAdapter;
import io.noties.markwon.core.CoreSyntaxBlockParser;
import io.noties.markwon.syntax.Syntax;
import io.noties.markwon.syntax.SyntaxHighlighter;
import io.noties.markwon.utils.CodeBlocks;

import com.stupidbeauty.markwonmermaid.core.MermaidAndroidEngine;
import com.stupidbeauty.markwonmermaid.core.MermaidEngine;

/**
 * Markwon plugin to render Mermaid diagrams in Markdown code blocks.
 * 
 * Usage:
 * Add to Markwon builder:
 * {@code markwon.usePlugin(new MarkwonMermaidPlugin());}
 * 
 * Example Markdown:
 * ```mermaid
 * flowchart TD
 *   A[Start] --> B{Process?}
 *   B -->|Yes| C[Do it]
 *   B -->|No| D[Skip]
 * ```
 */
public class MarkwonMermaidPlugin extends AbstractMarkwonPlugin {

    private static final String FENCE_TYPE = "mermaid";
    private static final int MAX_BITMAP_DIMENSION = 2000; // Prevent OOM

    @Override
    public void configureScope(@NonNull ScopeConfig.Builder builder) {
        // No scope configuration needed
    }

    @Override
    public void configurePlugin(@NonNull PluginConfig.Builder builder) {
        builder.codeBlock(
            CoreSyntaxAdapter.fence(FENCE_TYPE),
            new CodeBlocks.Factory() {
                @Override
                public @Nullable SyntaxHighlighter create(@NonNull Syntax syntax, @NonNull SyntaxHighlighter.Factory factory) {
                    // Return a special highlighter that renders Mermaid
                    return new MermaidSyntaxHighlighter();
                }
            }
        );
    }

    /**
     * Custom highlighter that renders Mermaid diagrams as Images.
     */
    private static class MermaidSyntaxHighlighter implements SyntaxHighlighter {
        
        private final MermaidEngine engine = MermaidAndroidEngine.getInstance();

        @Override
        public void highlight(@NonNull Spannable spannable, @NonNull Syntax syntax, int start, int end) {
            try {
                // Extract the mermaid code between backticks
                String mermaidCode = extractCode(syntax, spannable);
                
                if (mermaidCode != null && !mermaidCode.trim().isEmpty()) {
                    // Render to bitmap
                    android.graphics.Bitmap bitmap = engine.renderToBitmap(mermaidCode);
                    
                    // Resize if too large
                    if (bitmap.getWidth() > MAX_BITMAP_DIMENSION || bitmap.getHeight() > MAX_BITMAP_DIMENSION) {
                        float scale = Math.min((float) MAX_BITMAP_DIMENSION / bitmap.getWidth(),
                                              (float) MAX_BITMAP_DIMENSION / bitmap.getHeight());
                        int newWidth = (int) (bitmap.getWidth() * scale);
                        int newHeight = (int) (bitmap.getHeight() * scale);
                        bitmap = android.graphics.Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true);
                    }
                    
                    // Create ImageSpan and insert at the beginning of the block
                    ImageSpan imageSpan = new ImageSpan(spannable.getContext(), bitmap);
                    spannable.setSpan(imageSpan, start, end, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                } else {
                    // Clear any existing spans if empty
                    clearSpans(spannable, start, end);
                }
            } catch (Exception e) {
                // Log error but don't crash
                System.err.println("Mermaid rendering failed: " + e.getMessage());
                clearSpans(spannable, start, end);
            }
        }

        private String extractCode(Syntax syntax, Spannable spannable) {
            // Extract content from the code block
            // This is simplified; real implementation needs proper parsing
            // For now, assume the text after fence type is the code
            return ""; // Placeholder - will be implemented properly
        }

        private void clearSpans(Spannable spannable, int start, int end) {
            Object[] spans = spannable.getSpans(start, end, Object.class);
            for (Object span : spans) {
                spannable.removeSpan(span);
            }
        }
    }
}