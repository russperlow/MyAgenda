package com.russperlow.myagenda;

import java.util.Date;
import java.util.List;


// General class that will be used for all item types
public class Item {
    // The type of this list item
    public String type;

    // The details of this item
    public String details;

    // The date this item is due
    public Date dueDate;

    public Item(String type, String details){
        this.type = type;
        this.details = details;
        this.dueDate = new Date(2018, 9, 4);
    }

    public Item(String type, String details, Date dueDate){
        this.type = type;
        this.details = details;
        this.dueDate = dueDate;
    }
}
