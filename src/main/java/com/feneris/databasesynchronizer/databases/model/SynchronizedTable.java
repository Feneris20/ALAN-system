package com.feneris.databasesynchronizer.databases.model;

import javax.persistence.*;

@Entity
@Table(name = "ds_synchronized_table", schema = "database_synchronizer", catalog = "")
@org.hibernate.annotations.NamedQueries({
        @org.hibernate.annotations.NamedQuery(name = "SynchronizedTable.findAll",
                query = "select st from SynchronizedTable as st")
})
public class SynchronizedTable implements Comparable {
    private String text = "";


    @Id
    @Column(name = "dst_text")
    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (text == null) {
            throw new NullPointerException("Text canot be null");
        }
        if (text.length() > 2000) {
            throw new IllegalArgumentException("Text must be shorter as 2000");
        }
        this.text = text;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SynchronizedTable that = (SynchronizedTable) o;

        if (text != null ? !text.equals(that.text) : that.text != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return text != null ? text.hashCode() : 0;
    }


    @Override
    public int compareTo(Object o) {
        return this.getText().compareTo(((SynchronizedTable) o).getText());
    }
}
