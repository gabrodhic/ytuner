package com.protogab.ytuner;

/*
 * Abstract class to be able to use different API version references, for a relative layout setBackground(API 16) and setBackgroundDrawable(API 1) 
 * http://stackoverflow.com/questions/11592820/writing-backwards-compatible-android-code
 * NOTE: This is not used for now as using deprecated methods its allowed in upper version (for this time last android API is 19).
 */
public abstract class CompatibleBackground {
    public abstract void setCompatibleBackgnd();
}
