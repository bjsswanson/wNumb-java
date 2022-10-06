package com.bjsswanson;

import com.bjsswanson.WNumb;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

public class WNumbTest {

    @Test
    public void toDecimalMarkPrefix() {
        WNumb formatter = WNumb.builder().decimals(2).prefix("$").build();
        String result = formatter.to("1012345.2316");
        Assert.assertEquals("$1012345.23", result);
    }

    @Test
    public void toDecimalMarkPrefix2() {
        WNumb formatter = WNumb.builder().decimals(4).prefix("$").build();
        String result = formatter.to("1012345.23");
        Assert.assertEquals("$1012345.2300", result);
    }

    @Test
    public void toDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(2).mark(".").prefix("$").thousands(",").build();
        String result = formatter.to("1012345.23");
        Assert.assertEquals("$1,012,345.23", result);
    }

    @Test
    public void toNegativeDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(2).mark(".").prefix("$").negativeBefore("-").thousands(",").build();
        String result = formatter.to("-1012345.23");
        Assert.assertEquals("-$1,012,345.23", result);
    }

    @Test
    public void toNoDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(0).mark(".").prefix("$").thousands(",").build();
        String result = formatter.to("1012345.23");
        Assert.assertEquals("$1,012,345", result);
    }

    @Test
    public void toNoDecimalMarkSuffixThousands() {
        WNumb formatter = WNumb.builder().decimals(0).mark(".").suffix("$").thousands(",").build();
        String result = formatter.to("1012345.23");
        Assert.assertEquals("1,012,345$", result);
    }

    @Test
    public void fromDecimalMarkPrefix() {
        WNumb formatter = WNumb.builder().decimals(2).prefix("$").build();
        BigDecimal result = formatter.from("$1012345.23");
        Assert.assertEquals(new BigDecimal("1012345.23"), result);
    }

    @Test
    public void fromDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(2).mark(".").prefix("$").thousands(",").build();
        BigDecimal result = formatter.from("$1,012,345.23");
        Assert.assertEquals(new BigDecimal("1012345.23"), result);
    }

    @Test
    public void fromNegativeDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(2).mark(".").prefix("$").negativeBefore("-").thousands(",").build();
        BigDecimal result = formatter.from("-$1,012,345.23");
        Assert.assertEquals(new BigDecimal("-1012345.23"), result);
    }

    @Test
    public void fromNoDecimalMarkPrefixThousands() {
        WNumb formatter = WNumb.builder().decimals(0).mark(".").prefix("$").thousands(",").build();
        BigDecimal result = formatter.from("$1,012,345.23");
        Assert.assertEquals(new BigDecimal("1012345.23"), result);
    }

    @Test
    public void fromNoDecimalMarkSuffixThousands() {
        WNumb formatter = WNumb.builder().decimals(2).mark(".").suffix("$").thousands(",").build();
        BigDecimal result = formatter.from("1,012,345.23$");
        Assert.assertEquals(new BigDecimal("1012345.23"), result);
    }
}
