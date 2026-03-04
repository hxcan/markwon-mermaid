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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
    private static final Pattern FENCE_PATTERN = Pattern.compile("^```\\s*" + FENCE_TYPE + "\\s*$", Pattern.MULTILINE);

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
                    // Validate syntax first
                    if (!engine.isValidSyntax(mermaidCode)) {
                        System.err.println("Invalid Mermaid syntax at position " + start);
                        clearSpans(spannable, start, end);
                        return;
                    }
                    
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
                System.err.println("Mermaid rendering failed at position " + start + ": " + e.getMessage());
                e.printStackTrace();
                clearSpans(spannable, start, end);
            }
        }

        private String extractCode(Syntax syntax, Spannable spannable) {
            // Get the raw text from the code block
            String text = spannable.subSequence(syntax.info.start, syntax.info.end).toString();
            
            // Find the fence line (```mermaid) and extract content after it
            Matcher matcher = FENCE_PATTERN.matcher(text);
            if (matcher.find()) {
                // Content starts after the fence line (usually newline)
                int contentStart = matcher.end();
                // Skip whitespace/newline after fence
                while (contentStart < text.length() && Character.isWhitespace(text.charAt(contentStart))) {
                    contentStart++;
                }
                // Find the closing fence or end of string
                int closingFence = text.indexOf("```", contentStart);
                if (closingFence != -1) {
                    return text.substring(contentStart, closingFence).trim();
                } else {
                    return text.substring(contentStart).trim();
                }
            }
            return "";
        }

        private void clearSpans(Spannable spannable, int start, int end) {
            Object[] spans = spannable.getSpans(start, end, Object.class);
            for (Object span : spans) {
                spannable.removeSpan(span);
            }
        }
    }
}