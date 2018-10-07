package com.arcsoft.stickerlibrary.api;

public class FaceStatus {
    private static final int ASLFA_MAX_FACE_NUM = 4;
    int[] eye_blink;
    int[] eyebrow_raise;
    int faceCount;
    int[] head_pose_lr;
    int[] left_eye_closed;
    int[] mouth_open;
    int[] nod_head;
    int[] right_eye_closed;

    public FaceStatus() {
        this.left_eye_closed = null;
        this.right_eye_closed = null;
        this.mouth_open = null;
        this.eye_blink = null;
        this.eyebrow_raise = null;
        this.nod_head = null;
        this.head_pose_lr = null;
        this.faceCount = 0;
        this.left_eye_closed = new int[4];
        this.right_eye_closed = new int[4];
        this.mouth_open = new int[4];
        this.eye_blink = new int[4];
        this.eyebrow_raise = new int[4];
        this.nod_head = new int[4];
        this.head_pose_lr = new int[4];
    }

    public void clone(FaceStatus fs) {
        if (fs != null) {
            this.faceCount = fs.faceCount;
            for (int i = 0; i < 4; i++) {
                this.left_eye_closed[i] = fs.left_eye_closed[i];
                this.right_eye_closed[i] = fs.right_eye_closed[i];
                this.mouth_open[i] = fs.mouth_open[i];
                this.eye_blink[i] = fs.eye_blink[i];
                this.eyebrow_raise[i] = fs.eyebrow_raise[i];
                this.nod_head[i] = fs.nod_head[i];
                this.head_pose_lr[i] = fs.head_pose_lr[i];
            }
        }
    }
}
