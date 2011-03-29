/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.complitex.dictionary.util;

import com.google.common.collect.Maps;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Map;
import java.util.Random;
import org.testng.annotations.Test;
import static org.testng.Assert.*;
import static org.complitex.dictionary.util.StringUtil.*;

/**
 *
 * @author Artem
 */
public class StringUtilTest {

    @Test(groups = {"checkCharacterCodes"})
    public void testCharacterCodes() {
        System.out.println("TO_CYRILLIC_MAP: ");
        for (Map.Entry<Character, Character> entry : getToCyrillicMap().entrySet()) {
            System.out.println("key code: " + (int) entry.getKey() + ", value code: " + (int) entry.getValue());
        }
    }

    @Test
    public void testToCyrillic() {
        String testValue = "AaTxXkKMeEoOpPcCBH";
        String cyrillicVersion = toCyrillic(testValue);
        assertEquals(cyrillicVersion, "АаТхХкКМеЕоОрРсСВН");
    }

    @Test(groups = {"performance"})
    public void testToCyrillicPerformance() {
        Map<Integer, Double> measureResults = Maps.newLinkedHashMap();
        TestStringGenerator generator = new TestStringGenerator(1000);
        String generatedTestString = generator.generateTestString();
        for (int i = 0; i < 500; i++) {
            long start = System.currentTimeMillis();
            toCyrillic(generatedTestString);
            long end = System.currentTimeMillis();
            measureResults.put(i, (end - start) / 1000.0);
        }
        writeMeasureResults(measureResults);
    }

    private void writeMeasureResults(Map<Integer, Double> measureResults) {
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream("measureResults.txt"), "UTF-8"));
            for (Map.Entry<Integer, Double> entry : measureResults.entrySet()) {
                writer.write("Attempt " + entry.getKey() + " : " + entry.getValue());
                writer.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        TestStringGenerator generator = new TestStringGenerator(1000);
        String generatedTestString = generator.generateTestString();
        System.out.println(generatedTestString);
    }

    private static class TestStringGenerator {

        private Random random = new Random(System.currentTimeMillis());
        private int length;

        public TestStringGenerator(int length) {
            this.length = length;
        }

        private String generateTestString() {
            StringBuilder strBuilder = new StringBuilder();
            for (int i = 0; i < length; i++) {
                strBuilder.append(generateTestCharacter());
            }
            return strBuilder.toString();
        }

        //65-90, 97-122, 1040-1103
        private Character generateTestCharacter() {
            int characterCode = 65;
            int group = random.nextInt(2) + 1;
            switch (group) {
                case 1:
                    characterCode = random.nextInt(26) + 65;
                    break;
                case 2:
                    characterCode = random.nextInt(26) + 97;
                    break;
            }
            return (char) characterCode;
        }
    }
}
