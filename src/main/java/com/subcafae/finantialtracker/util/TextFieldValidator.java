/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.subcafae.finantialtracker.util;

/**
 *
 * @author Jesus Gutierrez
 */
import javax.swing.*;
import javax.swing.text.*;

public class TextFieldValidator {
    
    // Método para validar solo números enteros
    public static void applyIntegerFilter(JTextField textField) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
                if (text.matches("\\d*")) { // Solo dígitos
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
                if (text.matches("\\d*")) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }
        });
    }

    // Método para validar números decimales con un solo punto
    public static void applyDecimalFilter(JTextField textField) {
        AbstractDocument doc = (AbstractDocument) textField.getDocument();
        doc.setDocumentFilter(new DocumentFilter() {
            @Override
            public void insertString(FilterBypass fb, int offset, String text, AttributeSet attr)
                throws BadLocationException {
                String newText = getNewText(fb, offset, text, 0);
                if (isValidDecimal(newText)) {
                    super.insertString(fb, offset, text, attr);
                }
            }

            @Override
            public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
                String newText = getNewText(fb, offset, text, length);
                if (isValidDecimal(newText)) {
                    super.replace(fb, offset, length, text, attrs);
                }
            }

            private String getNewText(FilterBypass fb, int offset, String text, int length) throws BadLocationException {
                StringBuilder sb = new StringBuilder();
                sb.append(fb.getDocument().getText(0, fb.getDocument().getLength()));
                sb.replace(offset, offset + length, text);
                return sb.toString();
            }

            private boolean isValidDecimal(String text) {
                int dotCount = 0;
                for (char c : text.toCharArray()) {
                    if (c == '.') {
                        if (++dotCount > 1) return false; // Máximo un punto
                    } else if (!Character.isDigit(c)) {
                        return false; // Solo dígitos y puntos
                    }
                }
                return true;
            }
        });
    }
}