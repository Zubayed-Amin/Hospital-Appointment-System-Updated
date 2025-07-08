package hospitalappointmentsystem;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class PatAppointment {

    private SimpleIntegerProperty id;
    private SimpleStringProperty doctor;
    private SimpleStringProperty contact;
    private SimpleStringProperty date;
    private SimpleStringProperty time;
    private SimpleStringProperty status;
    
    public PatAppointment(int id, String doctor, String contact, String date, String time, String status, String string5) {
        this.id = new SimpleIntegerProperty(id);
        this.doctor = new SimpleStringProperty(doctor);
        this.contact = new SimpleStringProperty(contact);
        this.date = new SimpleStringProperty(date);
        this.time = new SimpleStringProperty(time);
        this.status = new SimpleStringProperty(status);
    }
    
    public int getId() {
        return id.get();
    }

    public String getDoctor() {
        return doctor.get();
    }
    
    public String getContact() {
        return contact.get();
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
