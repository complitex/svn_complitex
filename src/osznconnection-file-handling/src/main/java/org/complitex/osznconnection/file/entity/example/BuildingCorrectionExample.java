package org.complitex.osznconnection.file.entity.example;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 03.12.10 18:01
 */
public class BuildingCorrectionExample extends CorrectionExample{
    //Correction parser

    private boolean isSmallCorrection(){
        return getWordsCount(getCorrection()) == 1;
    }

    private boolean isMediumCorrection(){
        return getWordsCount(getCorrection()) == 2;
    }

    private boolean isFullCorrection(){
        return getWordsCount(getCorrection()) == 3;
    }

    public String getCityCorrection(){
        if (isFullCorrection()){
            return getWord(getCorrection(), 0);
        }

        return null;
    }

    public String getStreetCorrection(){
        if (isMediumCorrection()){
            return getWord(getCorrection(), 0);
        }

        if (isFullCorrection()){
            return getWord(getCorrection(), 1);
        }

        return null;
    }

    public String getBuildingCorrection(){
        if (isMediumCorrection()){
            return getWord(getCorrection(), 1);
        }

        if (isFullCorrection()){
            return getWord(getCorrection(), 2);
        }

        return null;
    }

    //Object parser

    private boolean isSmallObject(){
        return getWordsCount(getInternalObject()) == 1;
    }

    private boolean isMediumObject(){
        return getWordsCount(getInternalObject()) == 2;
    }

    private boolean isFullObject(){
        return getWordsCount(getInternalObject()) == 3;
    }

     public String getCityObject(){
        if (isFullCorrection()){
            return getWord(getInternalObject(), 0);
        }

        return null;
    }

    public String getStreetObject(){
        if (isMediumObject()){
            return getWord(getInternalObject(), 0);
        }

        if (isFullObject()){
            return getWord(getInternalObject(), 1);
        }

        return null;
    }

    public String getBuildingObject(){
        if (isMediumObject()){
            return getWord(getInternalObject(), 1);
        }

        if (isFullObject()){
            return getWord(getInternalObject(), 2);
        }

        return null;
    }

    private String getWord(String s, int count){
        if (s != null){
            String[] words = s.split("\\s");

            if (count > -1 && count < words.length){
                return words[count];
            }
        }

        return null;
    }

    private int getWordsCount(String s){
        if (s != null){
            return s.split("\\s").length;
        }

        return -1;
    }

}
