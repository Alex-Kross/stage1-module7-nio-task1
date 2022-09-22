package com.epam.mjc.nio;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class FileReader {
    private static final String WORD_STRING = "[a-zA-Z]+[^\n\r]";
    private static final String DIGIT_STRING = "[0-9]*[^\n\r]";
    private static final String NAME_STRING = "name:{1}[\\s]|Name:{1}[\\s]|name{1}[\\s]|Name{1}[\\s]";
    private static final  String AGE_STRING = "age:{1}[\\s]|Age:{1}[\\s]|age{1}[\\s]|Age{1}[\\s]";
    private static final  String EMAIL_STRING = "email:{1}[\\s]|Email:{1}[\\s]|email{1}[\\s]|Email{1}[\\s]";
    private static final  String PHONE_STRING = "phone:{1}[\\s]|Phone:{1}[\\s]|phone{1}[\\s]|Phone{1}[\\s]";

    private static final Pattern DIGIT_STRING_PATTERN = Pattern.compile(DIGIT_STRING);
    private static final Pattern WORD_STRING_PATTERN = Pattern.compile(WORD_STRING);
    private static final Pattern NAME_STRING_PATTERN = Pattern.compile(NAME_STRING);
    private static final  Pattern AGE_STRING_PATTERN = Pattern.compile(AGE_STRING);
    private static final  Pattern EMAIL_STRING_PATTERN = Pattern.compile(EMAIL_STRING);
    private static final  Pattern PHONE_STRING_PATTERN = Pattern.compile(PHONE_STRING);

    public Profile getDataFromFile(File file) {
        Profile profile = new Profile();

        try(RandomAccessFile aFile = new RandomAccessFile(file, "r")) {
            FileChannel channel = aFile.getChannel();
            int channelSize = 256;
            ByteBuffer buffer = ByteBuffer.allocate(channelSize);
            int bytesRead = channel.read(buffer);

            StringBuilder stringBuilder = new StringBuilder();
            while (bytesRead != -1) {
                buffer.flip();
                for (int i = 0; i < buffer.limit(); i++) {
                    char tempChar = (char) buffer.get();
                    stringBuilder.append(tempChar);
                    if (tempChar == '\n') {
                        profile = parseString(profile, stringBuilder.toString());
                        stringBuilder.delete(0, stringBuilder.length());
                    }
                }
                buffer.clear();
                bytesRead = channel.read(buffer);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return profile;
    }

    private Profile parseString(Profile profile, String data) {
        Matcher nameStringMather = NAME_STRING_PATTERN.matcher(data);
        Matcher ageStringMather = AGE_STRING_PATTERN.matcher(data);
        Matcher emailStringMather = EMAIL_STRING_PATTERN.matcher(data);
        Matcher phoneStringMather = PHONE_STRING_PATTERN.matcher(data);
        Matcher wordStringMather;
        Matcher digitStringMather;
        if (nameStringMather.find()) {
            wordStringMather = WORD_STRING_PATTERN.matcher(data.substring(nameStringMather.end()));
            if (wordStringMather.find()) {
                profile.setName(wordStringMather.group());
            }
        } else if (ageStringMather.find()) {
            String s =data.substring(ageStringMather.end());
            digitStringMather = DIGIT_STRING_PATTERN.matcher(data.substring(ageStringMather.end()));
            if (digitStringMather.find()) {
                profile.setAge(Integer.parseInt(digitStringMather.group()));
            }
        } else if (emailStringMather.find()) {
            wordStringMather = WORD_STRING_PATTERN.matcher(data.substring(emailStringMather.end()));
            if (wordStringMather.find()) {
                profile.setEmail(wordStringMather.group());
            }
        } else if (phoneStringMather.find()) {
            digitStringMather = DIGIT_STRING_PATTERN.matcher(data.substring(phoneStringMather.end()));
            if (digitStringMather.find()) {
                profile.setPhone((long) Integer.parseInt(digitStringMather.group()));
            }
        }
        return profile;
    }
}
