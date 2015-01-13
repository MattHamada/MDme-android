package com.MDmde.mobile;

/**
 * MDme Android application
 * Author:: Matt Hamada (maito:mattahamada@gmail.com)
 * Created on:: 4/12/14
 * Copyright:: Copyright (c) 2014 MDme
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential.
 */
class Appointment
{
    private int id;
    private String appointment_delayed_time;
    private String description;
    private String doctor_full_name;
    private int doctor_id;

    public Appointment(int id, String appointment_delayed_time, String description,
                       String doctor_full_name, int doctor_id)
    {
        this.id = id;
        this.appointment_delayed_time = appointment_delayed_time;
        this.description = description;
        this.doctor_full_name = doctor_full_name;
        this.doctor_id = doctor_id;
    }

    @Override
    public String toString()
    {
        return appointment_delayed_time + " - " + doctor_full_name;
    }

    public int getId()
    {
        return id;
    }

    public void setId(int id)
    {
        this.id = id;
    }

    public String getAppointment_delayed_time()
    {
        return appointment_delayed_time;
    }

    public void setAppointment_delayed_time(String appointment_delayed_time)
    {
        this.appointment_delayed_time = appointment_delayed_time;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getDoctor_full_name()
    {
        return doctor_full_name;
    }

    public void setDoctor_full_name(String doctor_full_name)
    {
        this.doctor_full_name = doctor_full_name;
    }

    public int getDoctor_id()
    {
        return doctor_id;
    }

    public void setDoctor_id(int doctor_id)
    {
        this.doctor_id = doctor_id;
    }


}
