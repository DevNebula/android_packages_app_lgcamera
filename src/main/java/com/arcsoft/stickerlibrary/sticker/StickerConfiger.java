package com.arcsoft.stickerlibrary.sticker;

public class StickerConfiger {
    public static final String BOOK = "book";
    public static final String BOOKS = "books";
    /* renamed from: ID */
    public static final String f72ID = "id";
    public static final String NAME = "name";
    public static final String PRICE = "price";
    /* renamed from: id */
    private int f73id;
    private String name;
    private float price;

    public int getId() {
        return this.f73id;
    }

    public void setId(int id) {
        this.f73id = id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public float getPrice() {
        return this.price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public String toString() {
        return "id:" + this.f73id + ",name:" + this.name + ",price:" + this.price;
    }
}
