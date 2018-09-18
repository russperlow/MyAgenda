package com.russperlow.myagenda;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;


// General class that will be used for all item types
public class Item {
    // The type of this list item
    private String type;

    // The details of this item
    private String details;

    // The date this item is due
    private Calendar dueDate;


    public Item(String type, String details){
        this.type = type;
        this.details = details;
        this.dueDate = Calendar.getInstance();
    }

    public Item(String type, String details, Calendar dueDate){
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
    }

    /**
     * @return the dueDate as a formatted string
     */
    public String getDueDate(){
        return new SimpleDateFormat("MM/dd/yyyy 'at' hh:mm a").format(dueDate.getTime());
    }

    /**
     * @return the dueDate as a calendar object
     */
    public Calendar getCalendar(){
        return dueDate;
    }

    /**
     * @return the type of this item
     */
    public String getType(){
        return type;
    }

    /**
     * @return the details/description of this item
     */
    public String getDetails(){
        return details;
    }
}
