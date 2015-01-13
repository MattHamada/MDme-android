package com.MDmde.mobile;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;

/**
 * MDme Android application
 * Author:: ermacaz (maito:mattahamada@gmail.com)
 * Created on:: 4/8/14
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
 class Photo
{
    private static final String JSON_FILENAME = "filename";
    private static final String JSON_ORIENTATION = "orientation";

    //filename is really absolute path to file
    private String mFilename;
    private int mOrientation;

    public Photo(String filename, int orientation)
    {
        mFilename = filename;
        mOrientation = orientation;
    }

    public Photo(JSONObject json) throws JSONException
    {
        mFilename = json.getString(JSON_FILENAME);
        mOrientation = json.getInt(JSON_ORIENTATION);
    }

    public JSONObject toJSON() throws JSONException
    {
        JSONObject json = new JSONObject();
        json.put(JSON_FILENAME, mFilename);
        json.put(JSON_ORIENTATION, mOrientation);
        return json;
    }

    public int getOrientation()
    {
        return mOrientation;
    }

    public void setOrientation(int orientation)
    {
        this.mOrientation = orientation;
    }

    public String getFIlename()
    {
        return mFilename;
    }

    public void deletePhoto()
    {
        File f = new File(mFilename);
        f.delete();
    }
}
