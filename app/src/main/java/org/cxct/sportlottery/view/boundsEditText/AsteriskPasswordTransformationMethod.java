package org.cxct.sportlottery.view.boundsEditText;

import android.text.method.PasswordTransformationMethod;
import android.view.View;

import androidx.annotation.NonNull;

public class AsteriskPasswordTransformationMethod extends PasswordTransformationMethod {
    @Override
    public CharSequence getTransformation(CharSequence source, View view) {
        return new PasswordCharSequence(source);
    }

        private class PasswordCharSequence implements CharSequence {

            private CharSequence mSource;

            public PasswordCharSequence(CharSequence source) {

                mSource = source; // Store char sequence

            }

            @Override
            public int length() {
                return mSource.length(); // Return default
            }

            @Override
            public char charAt(int i) {
                return '*';
            }

            @NonNull
            @Override
            public CharSequence subSequence(int start, int end) {
                return mSource.subSequence(start, end); // Return default
            }
        }
    }
