/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.unlaunch.engine;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

/**
 *
 * @author jawad
 */
public class OperatorTest {
    
    @Test
    public void testEquals(){
    
        String country = "USA";
        List<String> countryList = Arrays.asList("china","pak","india");
        
        boolean result = Operator.EQUALS.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result EQ: false", false ,result);
        
        System.out.println("Result EQ: " + result);
    }

    @Test
    public void testNotEquals(){
    
        String country = "USA";
        List<String> countryList = Arrays.asList("china","pak","india");
        
        boolean result = Operator.NOT_EQUALS.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result NEQ: true", true ,result);
        
        System.out.println("Result NEQ: " + result);
    }
    
    @Test
    public void testEqualsInNumberAttribute(){
    
        Number userValue = 10.5;
        List<String> numberList = Arrays.asList("10.5", "100.30", "82", "10");
        
        boolean result = Operator.EQUALS.apply(numberList, new UnlaunchNumberValue(userValue), AttributeType.NUMBER);
        
        Assert.assertEquals("Result EQ: true", true ,result);
       
    }

    @Test
    public void testNotEqualsInNumberAttribute(){
    
        Number userValue = 10.5;
        List<String> numberList = Arrays.asList("10.7", "100", "82.35", "17");
        
        boolean result = Operator.NOT_EQUALS.apply(numberList, new UnlaunchNumberValue(userValue), AttributeType.NUMBER);
        
        Assert.assertEquals("Result NEQ: true", true ,result);
        
    }
    
    @Test
    public void testGreaterThan(){
    
        int age = 18;
        List<String> ageList = Arrays.asList("15");
        
        boolean result = Operator.GREATER_THAN.apply(ageList, new UnlaunchNumberValue(age), AttributeType.STRING);
        
        Assert.assertEquals("Result GT: true", true ,result);
        System.out.println("Result GT: " + result);
    }
    
    @Test
    public void testGreaterThanOrEquals(){
    
        int age = 18;
        List<String> ageList = Arrays.asList("15","18","30");
        
        boolean result = Operator.GREATER_THAN_OR_EQUALS.apply(ageList, new UnlaunchNumberValue(age), AttributeType.STRING);
        
        Assert.assertEquals("Result GTE: true.", true ,result);
        System.out.println("Result GTE: " + result);
    }
    
    @Test
    public void testLT(){
    
        int amountSpend = 1500;
        List<String> amountSpendList = Arrays.asList("1800");
        
        boolean result = Operator.LESS_THAN.apply(amountSpendList, new UnlaunchNumberValue(amountSpend), AttributeType.STRING);
        
        Assert.assertEquals("Result LT: true", true ,result);
        System.out.println("Result LT: " + result);
    }
    
    @Test
    public void testLTE() {

        int amountSpend = 1800;
        List<String> amountSpendList = Arrays.asList("1800");;

        boolean result = Operator.LESS_THAN_OR_EQUALS.apply(amountSpendList, new UnlaunchNumberValue(amountSpend), AttributeType.STRING);

        Assert.assertEquals("Result LTE: true", true ,result);
        System.out.println("Result LTE: " + result);
    }
    
    @Test
    public void testTimeLTE() {

        long currentTimeInMillis = System.currentTimeMillis();
        
        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();
        System.out.println("User Datetime: " + localDateTime);
        
        LocalDateTime dayBeforeDateTime = localDateTime.minusDays(1l);
        long dayBeforeEpoch = dayBeforeDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                
        System.out.println("Rule Apply Datetime: " + dayBeforeDateTime);
        List<String> timeList = Arrays.asList(String.valueOf(dayBeforeEpoch));
        
        boolean result = Operator.LESS_THAN_OR_EQUALS.apply(timeList, new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result LEQ: False", false , result);
        
        System.out.println("Result LEQ: " + result);
    }
    
    @Test
    public void testTimeGTE() {

        long currentTimeInMillis = System.currentTimeMillis();

        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();

        System.out.println("User Datetime: " + localDateTime);
        
        LocalDateTime tomorrowDateTime = localDateTime.plusDays(1l);
        long tomorrowEpoch = tomorrowDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        System.out.println("Rule Apply Datetime: " + tomorrowDateTime);
        
        List<String> timeList = Arrays.asList(String.valueOf(tomorrowEpoch));

        boolean result = Operator.GREATER_THAN_OR_EQUALS.apply(timeList, new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result GTE: false", false, result);

        System.out.println("Result GTE: " + result);
    }
    
    
    @Test
    public void testTimeGT() {

        long currentTimeInMillis = System.currentTimeMillis();

        LocalDateTime localDateTime = Instant.ofEpochMilli(currentTimeInMillis).atZone(ZoneId.systemDefault()).toLocalDateTime();

        System.out.println("User Datetime: " + localDateTime);
        
        LocalDateTime yesterdayDateTime = localDateTime.minusDays(1l);
        long dayBeforeEpoch = yesterdayDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        System.out.println("Rule Apply: " + yesterdayDateTime);
        
        List<String> timeList = Arrays.asList(String.valueOf(dayBeforeEpoch));

        boolean result = Operator.GREATER_THAN.apply(timeList, new UnlaunchDateTimeValue(localDateTime), AttributeType.DATE_TIME);

        Assert.assertEquals("Result GT: True", true, result);

        System.out.println("Result GT: " + result);
    }
    
    
    @Test
    public void testSW(){
    
        String country = "pak";
        List<String> countryList = Arrays.asList("china","pak","india");
        
        boolean result = Operator.STARTS_WITH.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result SW: true", true ,result);
        System.out.println("Result SW: " + result);
    }
    
    @Test
    public void testNSW(){
    
        String country = "USA";
        List<String> countryList = Arrays.asList("ch","pak","ind");
        
        boolean result = Operator.NOT_STARTS_WITH.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result NSW: true.", true ,result);
        System.out.println("Result NSW: " + result);
    }
    
    @Test
    public void testEW(){
    
        String country = "pakistan";
        List<String> countryList = Arrays.asList("tan","dia");
        
        boolean result = Operator.ENDS_WITH.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result EW: true", true ,result);
        System.out.println("Result EW : " + result);
    }
    
    @Test
    public void testNEW(){
    
        String country = "USA";
        List<String> countryList = Arrays.asList("ina","tan","dia");
        
        boolean result = Operator.NOT_ENDS_WITH.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result NEW: true.", true ,result);
        System.out.println("Result NEW: " + result);
    }
    
    @Test
    public void testContains(){
    
        String country = "USA";
        List<String> countryList = Arrays.asList("S","is","dia");
        
        boolean result = Operator.CONTAINS.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);
        
        Assert.assertEquals("Result Contains: true.", true ,result);
        System.out.println("Result Contains : " + result);
    }
    
    @Test
    public void testIsOneOf() {

        String country = "China";
        List<String> countryList = Arrays.asList("USA", "Pak", "China");

        boolean result = Operator.IS_ONE_OF.apply(countryList, new UnlaunchStringValue(country), AttributeType.STRING);

        Assert.assertEquals("Result IOF: true", true ,result);
        System.out.println("Result IOF : " + result);
    }
    
   
}
