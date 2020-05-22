package es.upv.mastermoviles.intemasc.captura.model;

public class Photo {
    private  String urlPhoto;
    private long creation_date;
    private boolean labelled;

    public Photo() {
    }

    public Photo(long creation_date, boolean labelled, String urlPhoto) {
        this.creation_date = creation_date;
        this.labelled = labelled;
        this.urlPhoto = urlPhoto;
    }

    public long getCreation_date() {
        return creation_date;
    }

    public void setCreation_date(long creation_date) {
        this.creation_date = creation_date;
    }

    public boolean isLabelled() {
        return labelled;
    }

    public String getUrlPhoto() {
        return urlPhoto;
    }

    public void setUrlPhoto(String urlPhoto) {
        this.urlPhoto = urlPhoto;
    }
}
