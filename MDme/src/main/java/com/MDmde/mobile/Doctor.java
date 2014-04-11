package com.MDmde.mobile;

/**
 * Created by Matt Hamada on 4/10/14.
 */
public class Doctor
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
