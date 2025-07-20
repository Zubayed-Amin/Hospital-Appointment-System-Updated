/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hospitalappointmentsystem;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

/**
 *
 * @author zubay
 */
public class AdminAppointment {
    private SimpleIntegerProperty id;
    private SimpleStringProperty patientName;
    private SimpleStringProperty doctorName;
    private SimpleStringProperty date;
    private SimpleStringProperty time;
    private SimpleStringProperty status;

    public AdminAppointment(int id, String patientName, String doctorName, String date, String time, String status) {
        this.id = new SimpleIntegerProperty(id);
        this.patientName = new SimpleStringProperty(patientName);
        this.doctorName = new SimpleStringProperty(doctorName);
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.status = new SimpleStringProperty(status);
    }

    public int getId() {
        return id.get();
    }

    public String getPatientName() {
        return patientName.get();
    }

    public String getDoctorName() {
        return doctorName.get();
    }

    public String getDate() {
        return date.get();
    }

    public String getTime() {
        return time.get();
    }

    public String getStatus() {
        return status.get();
    }
}
