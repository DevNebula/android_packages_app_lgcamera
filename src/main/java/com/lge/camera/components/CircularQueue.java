package com.lge.camera.components;

import java.util.Arrays;
import java.util.NoSuchElementException;

public class CircularQueue {
    private int mFront = 0;
    private boolean mFull = false;
    private Object[] mQueue = null;
    private int mRear = 0;
    private final int mSize;

    public CircularQueue(int size) {
        if (size <= 0) {
            throw new IllegalArgumentException("The size must be greater than 0");
        }
        this.mQueue = new Object[size];
        this.mSize = this.mQueue.length;
    }

    public void add(Object element) {
        if (element == null) {
            throw new NullPointerException("Attempted to add null object to queue");
        }
        if (isFull()) {
            remove();
        }
        Object[] objArr = this.mQueue;
        int i = this.mRear;
        this.mRear = i + 1;
        objArr[i] = element;
        if (this.mRear >= this.mSize) {
            this.mRear = 0;
        }
        if (this.mRear == this.mFront) {
            this.mFull = true;
        }
    }

    public void remove() {
        if (isEmpty()) {
            throw new NoSuchElementException("queue is empty");
        } else if (this.mQueue[this.mFront] != null) {
            Object[] objArr = this.mQueue;
            int i = this.mFront;
            this.mFront = i + 1;
            objArr[i] = null;
            if (this.mFront >= this.mSize) {
                this.mFront = 0;
            }
            this.mFull = false;
        }
    }

    public Object get(int index) {
        int sz = size();
        if (index < 0 || index >= sz) {
            throw new NoSuchElementException();
        }
        return this.mQueue[(this.mFront + index) % this.mSize];
    }

    public int size() {
        if (this.mRear < this.mFront) {
            return (this.mSize - this.mFront) + this.mRear;
        }
        if (this.mRear == this.mFront) {
            return this.mFull ? this.mSize : 0;
        } else {
            return this.mRear - this.mFront;
        }
    }

    public boolean isEmpty() {
        return size() == 0;
    }

    public boolean isFull() {
        return size() == this.mSize;
    }

    public void clear() {
        this.mFull = false;
        this.mFront = 0;
        this.mRear = 0;
        Arrays.fill(this.mQueue, null);
    }
}
