package com.instaclustr.picocli.typeconverter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.instaclustr.measure.Time;
import org.testng.Assert;
import org.testng.annotations.Test;

public class TimeTypeConverterTest {

    private final TimeMeasureTypeConverter converter = new TimeMeasureTypeConverter();

    @Test
    public void testTimeConversion() throws Exception {
        Time minutes = converter.convert("1m");
        Assert.assertEquals(Long.valueOf(1), minutes.value);
        Assert.assertEquals(Time.TimeUnit.MINUTES, minutes.unit);

        Time seconds = converter.convert("1s");
        Assert.assertEquals(Long.valueOf(1), seconds.value);
        Assert.assertEquals(Time.TimeUnit.SECONDS, seconds.unit);

        Time hours = converter.convert("1h");
        Assert.assertEquals(Long.valueOf(1), hours.value);
        Assert.assertEquals(Time.TimeUnit.HOURS, hours.unit);

        Time days = converter.convert("1d");
        Assert.assertEquals(Long.valueOf(1), days.value);
        Assert.assertEquals(Time.TimeUnit.DAYS, days.unit);
    }

    @Test
    public void testTimeJsonConversion() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();

        Time minute = converter.convert("1m");

        String minuteInJson = objectMapper.writer().writeValueAsString(minute);

        Time deserializedMinute = objectMapper.readValue(minuteInJson, Time.class);

        Assert.assertEquals(minute, deserializedMinute);
    }
}
