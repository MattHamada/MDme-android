package com.MDmde.mobile;

/**
 * MDme Android application
 * Author:: Matt Hamada (maito:mattahamada@gmail.com)
 * Created on:: 4/10/14
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
class Doctor
{
    private String mPhotoUrl;
    private String mFullName;
    private int mId;

    public Doctor(String photoUrl, String fullName, int id)
    {
        mPhotoUrl = photoUrl;
        mFullName = fullName;
        mId = id;
    }

    public String getPhotoUrl()
    {
        return mPhotoUrl;
    }

    public void setPhotoUrl(String photoUrl)
    {
        mPhotoUrl = photoUrl;
    }

    public String getFullName()
    {
        return mFullName;
    }

    public void setFullName(String fullName)
    {
        mFullName = fullName;
    }

    public int getId()
    {
        return mId;
    }

    public void setId(int id)
    {
        mId = id;
    }
}
