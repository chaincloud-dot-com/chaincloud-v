package com.chaincloud.chaincloudv.util;

import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.text.style.TypefaceSpan;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by songchenwen on 15/8/14.
 */
public class BitcoinUtil {
    public static final char CHAR_THIN_SPACE = '\u2009';
    public static final char CHAR_PUNCTUATION_SPACE = '\u2008';
    public static final char CHAR_MID_SPACE = '\u2005';
    public static final char CHAR_EN_SPACE = '\u2002';
    public static final BigInteger ONE_BTC = new BigInteger("100000000", 10);
    public static final BigInteger ONE_MBTC = new BigInteger("100000", 10);

    public static final int ONE_BTC_INT = ONE_BTC.intValue();
    public static final int ONE_MBTC_INT = ONE_MBTC.intValue();
    private static final Object SIGNIFICANT_SPAN = new StyleSpan(Typeface.BOLD);
    public static final RelativeSizeSpan SMALLER_SPAN = new RelativeSizeSpan(0.85f);


    private static final Pattern P_SIGNIFICANT = Pattern.compile("^([-+]" + '\u2009' + ")?\\d*(\\" +
            ".\\d{0,2})?");

    public static String formatValue(final long value, final int precision, final int shift) {
        return formatValue(value, "", "-", precision, shift);
    }

    public static String formatValue(long value, final String plusSign, final String minusSign,
                                     final int precision, final int shift) {

        final String sign = value < 0 ? minusSign : plusSign;

        if (shift == 0) {
            if (precision == 2) {
                value = value - value % 1000000 + value % 1000000 / 500000 * 1000000;
            } else if (precision == 4) {
                value = value - value % 10000 + value % 10000 / 5000 * 10000;
            } else if (precision == 6) {
                value = value - value % 100 + value % 100 / 50 * 100;
            } else if (precision == 8) {
                ;
            } else {
                throw new IllegalArgumentException("cannot handle precision/shift: " + precision
                        + "/" + shift);
            }

            final long absValue = Math.abs(value);
            final long coins = absValue / ONE_BTC_INT;
            final int satoshis = (int) (absValue % ONE_BTC_INT);

            if (satoshis % 1000000 == 0) {
                return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000000);
            } else if (satoshis % 10000 == 0) {
                return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10000);
            } else if (satoshis % 100 == 0) {
                return String.format(Locale.US, "%s%d.%06d", sign, coins, satoshis / 100);
            } else {
                return String.format(Locale.US, "%s%d.%08d", sign, coins, satoshis);
            }
        } else if (shift == 3) {
            if (precision == 2) {
                value = value - value % 1000 + value % 1000 / 500 * 1000;
            } else if (precision == 4) {
                value = value - value % 10 + value % 10 / 5 * 10;
            } else if (precision == 5) {
                ;
            } else {
                throw new IllegalArgumentException("cannot handle precision/shift: " + precision
                        + "/" + shift);
            }

            final long absValue = Math.abs(value);
            final long coins = absValue / ONE_MBTC_INT;
            final int satoshis = (int) (absValue % ONE_MBTC_INT);

            if (satoshis % 1000 == 0) {
                return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis / 1000);
            } else if (satoshis % 10 == 0) {
                return String.format(Locale.US, "%s%d.%04d", sign, coins, satoshis / 10);
            } else {
                return String.format(Locale.US, "%s%d.%05d", sign, coins, satoshis);
            }
        } else if (shift == 6) {
            if (precision != 2) {
                throw new IllegalArgumentException("cannot handle precision/shift: " + precision
                        + "/" + shift);
            }
            int coin = (ONE_BTC_INT / (int) Math.floor(Math.pow(10, shift)));
            final long absValue = Math.abs(value);
            final long coins = absValue / coin;
            final int satoshis = (int) (absValue % coin);
            return String.format(Locale.US, "%s%d.%02d", sign, coins, satoshis);
        } else {
            throw new IllegalArgumentException("cannot handle shift: " + shift);
        }
    }

    public static BigInteger toNanoCoins(final String value, final int shift) {
        final BigInteger nanoCoins = new BigDecimal(value).movePointRight(8 - shift)
                .toBigIntegerExact();

        if (nanoCoins.signum() < 0) {
            throw new IllegalArgumentException("negative amount: " + value);
        }

        return nanoCoins;
    }

    public static void formatSignificant(final Editable s, final RelativeSizeSpan
            insignificantRelativeSizeSpan) {
        s.removeSpan(SIGNIFICANT_SPAN);
        if (insignificantRelativeSizeSpan != null) {
            s.removeSpan(insignificantRelativeSizeSpan);
        }

        final Matcher m = P_SIGNIFICANT.matcher(s);
        if (m.find()) {
            final int pivot = m.group().length();
            s.setSpan(SIGNIFICANT_SPAN, 0, pivot, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (s.length() > pivot && insignificantRelativeSizeSpan != null) {
                s.setSpan(insignificantRelativeSizeSpan, pivot, s.length(), Spannable
                        .SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    public static Editable formatHash(@NonNull final String address, final int groupSize, final
    int lineSize) {
        return formatHash(null, address, groupSize, lineSize, CHAR_MID_SPACE);
    }

    public static Editable formatHash(@Nullable final String prefix, @NonNull final String
            address, final int groupSize, final int lineSize, final char groupSeparator) {
        final SpannableStringBuilder builder = prefix != null ? new SpannableStringBuilder
                (prefix) : new SpannableStringBuilder();

        final int len = address.length();
        for (int i = 0;
             i < len;
             i += groupSize) {
            final int end = i + groupSize;
            final String part = address.substring(i, end < len ? end : len);

            builder.append(part);
            builder.setSpan(new TypefaceSpan("monospace"), builder.length() - part.length(),
                    builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            if (end < len) {
                final boolean endOfLine = lineSize > 0 && end % lineSize == 0;
                builder.append(endOfLine ? '\n' : groupSeparator);
            }
        }

        return builder;
    }
}
