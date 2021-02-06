/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.unlaunch.engine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import io.unlaunch.exceptions.UnlaunchAttributeCastException;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jawad
 */
public class OperatorTest {
    
    @Test
    public void testEquals(){
        boolean result = Operator.EQUALS.apply("java, csharp,    node", new UnlaunchStringValue("java, csharp,    node"), AttributeType.STRING);
        
        Assert.assertEquals("Result EQ: true", true, result);
    }

    @Test(expected = UnlaunchAttributeCastException.class)
    public void testEqualsWrongDateAttribute(){
        Operator.EQUALS.apply("1612314129000", new UnlaunchStringValue("invalidDate"), AttributeType.DATE);
    }

    @Test(expected = UnlaunchAttributeCastException.class)
    public void testEqualsWrongSetAttribute(){
        Operator.EQUALS.apply("value", new UnlaunchBooleanValue(true), AttributeType.SET);
    }

    @Test(expected = UnlaunchAttributeCastException.class)
    public void testEqualsWrongNumberAttribute(){
        Operator.EQUALS.apply("value", new UnlaunchBooleanValue(true), AttributeType.NUMBER);
    }

    @Test
    public void testNotEquals(){
    
        boolean result = Operator.NOT_EQUALS.apply("Canada", new UnlaunchStringValue("USA"), AttributeType.STRING);
        
        Assert.assertEquals("Result NEQ: true", true, result);
    }
    
    @Test
    public void testEqualsInNumberAttribute(){
    
        Number userValue = 10.5;
        boolean result = Operator.EQUALS.apply(String.valueOf(userValue), new UnlaunchNumberValue(userValue), AttributeType.NUMBER);
        
        Assert.assertEquals("Result EQ: true", true, result);
    }

    @Test
    public void testNotEqualsInNumberAttribute(){

        boolean result = Operator.NOT_EQUALS.apply(String.valueOf(10.7), new UnlaunchNumberValue(10.5), AttributeType.NUMBER);
        
        Assert.assertEquals("Result NEQ: true", true, result);
    }
    
    @Test
    public void testGreaterThan(){
        boolean result = Operator.GREATER_THAN.apply(String.valueOf(10), new UnlaunchNumberValue(15), AttributeType.STRING);
        
        Assert.assertEquals("Result GT: true", true, result);
    }
    
    @Test
    public void testGreaterThanOrEquals(){
        boolean result = Operator.GREATER_THAN_OR_EQUALS.apply(String.valueOf(18), new UnlaunchNumberValue(18), AttributeType.STRING);
        
        Assert.assertEquals("Result GTE: true.", true, result);
    }
    
    @Test
    public void testLT(){
        boolean result = Operator.LESS_THAN.apply(String.valueOf(1800), new UnlaunchNumberValue(1500), AttributeType.STRING);
        
        Assert.assertEquals("Result LT: true", true, result);
    }
    
    @Test
    public void testLTE() {
        boolean result = Operator.LESS_THAN_OR_EQUALS.apply(String.valueOf(1800), new UnlaunchNumberValue(1800), AttributeType.STRING);

        Assert.assertEquals("Result LTE: true", true, result);
    }
    
    @Test
    public void testTimeLTE() {

        long currentTimeInMillis = System.currentTimeMillis();
        
        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        LocalDateTime dayBeforeDateTime = localDateTime.minusDays(1l);
        long dayBeforeEpoch = dayBeforeDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        
        boolean result = Operator.LESS_THAN_OR_EQUALS.apply(String.valueOf(dayBeforeEpoch), new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result LEQ: False", false, result);
    }
    
    @Test
    public void testTimeGTE() {

        long currentTimeInMillis = System.currentTimeMillis();
        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        LocalDateTime tomorrowDateTime = localDateTime.plusDays(1l);
        long tomorrowEpoch = tomorrowDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        boolean result = Operator.GREATER_THAN_OR_EQUALS.apply(String.valueOf(tomorrowEpoch), new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result GTE: false", false, result);
    }
    
    
    @Test
    public void testTimeGT() {

        long currentTimeInMillis = System.currentTimeMillis();
        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        
        LocalDateTime yesterdayDateTime = localDateTime.minusDays(1l);
        long dayBeforeEpoch = yesterdayDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        boolean result = Operator.GREATER_THAN.apply(String.valueOf(dayBeforeEpoch), new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result GT: True", true, result);
    }
    
    
    @Test
    public void testSW(){
        boolean result = Operator.STARTS_WITH.apply("java", new UnlaunchStringValue("java-sdk"), AttributeType.STRING);
        
        Assert.assertEquals("Result SW: true", true, result);
    }
    
    @Test
    public void testNSW(){
        boolean result = Operator.NOT_STARTS_WITH.apply("sdk", new UnlaunchStringValue("java-sdk"), AttributeType.STRING);
        
        Assert.assertEquals("Result NSW: true.", true, result);
    }
    
    @Test
    public void testEW(){
        boolean result = Operator.ENDS_WITH.apply("sdk", new UnlaunchStringValue("java-sdk"), AttributeType.STRING);
        
        Assert.assertEquals("Result EW: true", true, result);
    }
    
    @Test
    public void testNEW(){
        boolean result = Operator.NOT_ENDS_WITH.apply("java", new UnlaunchStringValue("java-sdk"), AttributeType.STRING);
        
        Assert.assertEquals("Result NEW: true.", true, result);
    }
    
    @Test
    public void testContains(){
        boolean result = Operator.CONTAINS.apply("java", new UnlaunchStringValue("sdk, java"), AttributeType.STRING);
        
        Assert.assertEquals("Result Contains: true.", true, result);
    }
    
    @Test
    public void testIsOneOf() {
        boolean result = Operator.IS_ONE_OF.apply("java-sdk", new UnlaunchStringValue("java-sdk"), AttributeType.STRING);

        Assert.assertEquals("Result IOF: true", true, result);
    }
}
