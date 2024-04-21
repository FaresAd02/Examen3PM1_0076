package com.example.examen3pm1_0076;

public class Entrevista {
    private int ID;
    private String descricpion, periodista, imagenBase64, audioBase64;

    public Entrevista() {
    }

    public Entrevista(int ID, String descricpion, String periodista, String imagenBase64) {
        this.ID = ID;
        this.descricpion = descricpion;
        this.periodista = periodista;
        this.imagenBase64 = imagenBase64;
    }

    public Entrevista(int ID, String descricpion, String periodista) {
        this.ID = ID;
        this.descricpion = descricpion;
        this.periodista = periodista;
    }

    public Entrevista(int ID, String descricpion, String periodista, String imagenBase64, String audioBase64) {
        this.ID = ID;
        this.descricpion = descricpion;
        this.periodista = periodista;
        this.imagenBase64 = imagenBase64;
        this.audioBase64 = audioBase64;
    }

    public int getID() {
        return ID;
    }

    public void setID(int ID) {
        this.ID = ID;
    }

    public String getDescricpion() {
        return descricpion;
    }

    public void setDescricpion(String descricpion) {
        this.descricpion = descricpion;
    }

    public String getPeriodista() {
        return periodista;
    }

    public void setPeriodista(String periodista) {
        this.periodista = periodista;
    }

    public String getImagenBase64() {
        return imagenBase64;
    }

    public void setImagenBase64(String imagenBase64) {
        this.imagenBase64 = imagenBase64;
    }

    public String getAudioBase64() {
        return audioBase64;
    }

    public void setAudioBase64(String audioBase64) {
        this.audioBase64 = audioBase64;
    }
}
