package com.bjsswanson;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class WNumb {
    private int decimals;
    private String thousands;
    private String mark;
    private String prefix;
    private String suffix;
    private Function<BigDecimal, BigDecimal> encoder;
    private Function<BigDecimal, BigDecimal> decoder;
    private String negativeBefore;
    private String negative;
    private BiFunction<String, String, String> edit;
    private Function<String, String> undo;

    public WNumb(int decimals, String thousands, String mark, String prefix, String suffix, Function<BigDecimal, BigDecimal> encoder, Function<BigDecimal, BigDecimal> decoder, String negativeBefore, String negative, BiFunction<String, String, String> edit, Function<String, String> undo) {
        this.decimals = decimals;
        this.thousands = thousands;
        this.mark = (mark == null && thousands != ".") ? "." : mark;
        this.prefix = prefix;
        this.suffix = suffix;
        this.encoder = encoder;
        this.decoder = decoder;
        this.negativeBefore = negativeBefore;
        this.negative = (negativeBefore == null && negative == null) ? "-" : negative;
        this.edit = edit;
        this.undo = undo;
    }

    public String strReverse(String a){
        return new StringBuilder(a).reverse().toString();
    }

    public boolean strStartsWith(String input, String match) {
        return input.startsWith(match);
    }

    public boolean strEndsWith(String input, String match) {
        return input.endsWith(match);
    }

    public boolean isValidNumber(String input) {
        if (input == null) { return false; }
        try {
            new BigDecimal(input);
        } catch (NumberFormatException nfe) { return false; }
        return true;
    }

    public BigDecimal toFixed(BigDecimal value, int exp) {
        return value.setScale(exp, RoundingMode.HALF_EVEN);
    }

    public String to(String input) {
        return formatTo(decimals, thousands, mark, prefix, suffix, encoder, decoder, negativeBefore, negative, edit, undo, input);
    }

    public String formatTo(
        Integer decimals,
        String thousand,
        String mark,
        String prefix,
        String suffix,
        Function<BigDecimal, BigDecimal> encoder,
        Function<BigDecimal, BigDecimal> decoder,
        String negativeBefore,
        String negative,
        BiFunction<String, String, String> edit,
        Function<String, String> undo,
        String input
        ) {
        String originalInput = input;
        boolean inputIsNegative = false;
        String[] inputPieces;
        String inputBase,
            inputDecimals = "",
            output = "";

        // Stop if no valid number was provided, the number is infinite or NaN.
        if (!isValidNumber(input)) {
            return null;
        }

        BigDecimal number = new BigDecimal(input);

        // Apply user encoder to the input.
        // Expected outcome: number.
        if (encoder != null) {
            number = encoder.apply(number);
        }

        // Formatting is done on absolute numbers,
        // decorated by an optional negative symbol.
        if (number.signum() == -1 ) {
            inputIsNegative = true;
            number = number.abs();
        }

        // Reduce the number of decimals to the specified option.
        if (decimals != null) {
            number = toFixed(number, decimals);
        }

        // Transform the number into a string, so it can be split.
        input = number.toString();

        // Break the number on the decimal separator.
        if (input.indexOf(".") != -1) {
            inputPieces = input.split("\\.");

            inputBase = inputPieces[0];

            if (mark != null && mark.length() > 0) {
                inputDecimals = mark + inputPieces[1];
            }
        } else {
            // If it isn't split, the entire number will do.
            inputBase = input;
        }

        // Group numbers in sets of three.
        if (thousand != null && thousand.length() > 0) {
            String[] arr = match(".{1,3}", strReverse(inputBase));
            inputBase = strReverse(String.join(strReverse(thousand), arr));
        }

        // If the number is negative, prefix with negation symbol.
        if (inputIsNegative && negativeBefore != null && negativeBefore.length() > 0) {
            output += negativeBefore;
        }

        // Prefix the number
        if (prefix != null && prefix.length() > 0) {
            output += prefix;
        }

        // Normal negative option comes after the prefix. Defaults to '-'.
        if (inputIsNegative && negative != null && negative.length() > 0) {
            output += negative;
        }

        // Append the actual number.
        output += inputBase;
        output += inputDecimals;

        // Apply the suffix.
        if (suffix != null && suffix.length() > 0) {
            output += suffix;
        }

        // Run the output through a user-specified post-formatter.
        if (edit != null) {
            output = edit.apply(output, originalInput);
        }

        // All done.
        return output;
    }

    public String[] match(String regex, String input) {
        List<String> allMatches = new ArrayList<>();
        Matcher m = Pattern.compile(regex).matcher(input);
        while (m.find()) {
            allMatches.add(m.group());
        }
        return allMatches.toArray(new String[0]);
    }

    public BigDecimal from(String input) {
        return formatFrom(decimals, thousands, mark, prefix, suffix, encoder, decoder, negativeBefore, negative, edit, undo, input);
    }

    public BigDecimal formatFrom(
        Integer decimals,
        String thousand,
        String mark,
        String prefix,
        String suffix,
        Function<BigDecimal, BigDecimal> encoder,
        Function<BigDecimal, BigDecimal> decoder,
        String negativeBefore,
        String negative,
        BiFunction<String, String, String> edit,
        Function<String, String> undo,
        String input
        ) {
        String originalInput = input;
        boolean inputIsNegative = false;
        String output = "";

        // User defined pre-decoder. Result must be a non empty string.
        if (undo != null) {
            input = undo.apply(input);
        }

        // Test the input. Can't be empty.
        if (input == null || input.length() <= 0) {
            return null;
        }

        // If the string starts with the negativeBefore value: remove it.
        // Remember is was there, the number is negative.
        if (negativeBefore != null && negativeBefore.length() > 0 && strStartsWith(input, negativeBefore)) {
            input = input.replace(negativeBefore, "");
            inputIsNegative = true;
        }

        // Repeat the same procedure for the prefix.
        if (prefix != null && prefix.length() > 0 && strStartsWith(input, prefix)) {
            input = input.replace(prefix, "");
        }

        // And again for negative.
        if (negative != null && negative.length() > 0 && strStartsWith(input, negative)) {
            input = input.replace(negative, "");
            inputIsNegative = true;
        }

        // Remove the suffix.
        // https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Global_Objects/String/slice
        if (suffix != null && suffix.length() > 0 && strEndsWith(input, suffix)) {
            input = input.substring(0, input.length() - suffix.length());
        }

        // Remove the thousand grouping.
        if (thousand != null && thousand.length() > 0) {
            input = String.join("", input.split(thousand));
        }

        // Set the decimal separator back to period.
        if (mark != null && mark.length() > 0) {
            input = input.replace(mark, ".");
        }

        // Prepend the negative symbol.
        if (inputIsNegative) {
            output += "-";
        }

        // Add the number
        output += input;

        // Trim all non-numeric characters (allow '.' and '-');
        output = output.replace("[^0-9\\.\\-]", "");

        // The value contains no parse-able number.
        if (output == "") {
            return null;
        }

        // Covert to number.
        BigDecimal number = new BigDecimal(output);

        // Run the user-specified post-decoder.
        if (decoder != null) {
            number = decoder.apply(number);
        }

        return number;
    }

    public static WNumb.WNumbBuilder builder() {
        return new WNumb.WNumbBuilder();
    }

    public static class WNumbBuilder {
        private int decimals;
        private String thousands;
        private String mark;
        private String prefix;
        private String suffix;
        private Function<BigDecimal, BigDecimal> encoder;
        private Function<BigDecimal, BigDecimal> decoder;
        private String negativeBefore;
        private String negative;
        private BiFunction<String, String, String> edit;
        private Function<String, String> undo;

        WNumbBuilder() {
        }

        public WNumb.WNumbBuilder decimals(int decimals) {
            this.decimals = decimals;
            return this;
        }

        public WNumb.WNumbBuilder thousands(String thousands) {
            this.thousands = thousands;
            return this;
        }

        public WNumb.WNumbBuilder mark(String mark) {
            this.mark = mark;
            return this;
        }

        public WNumb.WNumbBuilder prefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public WNumb.WNumbBuilder suffix(String suffix) {
            this.suffix = suffix;
            return this;
        }

        public WNumb.WNumbBuilder encoder(Function<BigDecimal, BigDecimal> encoder) {
            this.encoder = encoder;
            return this;
        }

        public WNumb.WNumbBuilder decoder(Function<BigDecimal, BigDecimal> decoder) {
            this.decoder = decoder;
            return this;
        }

        public WNumb.WNumbBuilder negativeBefore(String negativeBefore) {
            this.negativeBefore = negativeBefore;
            return this;
        }

        public WNumb.WNumbBuilder negative(String negative) {
            this.negative = negative;
            return this;
        }

        public WNumb.WNumbBuilder edit(BiFunction<String, String, String> edit) {
            this.edit = edit;
            return this;
        }

        public WNumb.WNumbBuilder undo(Function<String, String> undo) {
            this.undo = undo;
            return this;
        }

        public WNumb build() {
            return new WNumb(this.decimals, this.thousands, this.mark, this.prefix, this.suffix, this.encoder, this.decoder, this.negativeBefore, this.negative, this.edit, this.undo);
        }

        public String toString() {
            return "WNumb.WNumbBuilder(decimals=" + this.decimals + ", thousands=" + this.thousands + ", mark=" + this.mark + ", prefix=" + this.prefix + ", suffix=" + this.suffix + ", encoder=" + this.encoder + ", decoder=" + this.decoder + ", negativeBefore=" + this.negativeBefore + ", negative=" + this.negative + ", edit=" + this.edit + ", undo=" + this.undo + ")";
        }
    }
}
