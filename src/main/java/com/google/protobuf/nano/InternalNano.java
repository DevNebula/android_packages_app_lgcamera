package com.google.protobuf.nano;

import java.nio.charset.Charset;
import java.util.Arrays;

public final class InternalNano {
    protected static final Charset ISO_8859_1 = Charset.forName("ISO-8859-1");
    public static final Object LAZY_INIT_LOCK = new Object();
    protected static final Charset UTF_8 = Charset.forName("UTF-8");

    private InternalNano() {
    }

    public static String stringDefaultValue(String bytes) {
        return new String(bytes.getBytes(ISO_8859_1), UTF_8);
    }

    public static byte[] bytesDefaultValue(String bytes) {
        return bytes.getBytes(ISO_8859_1);
    }

    public static byte[] copyFromUtf8(String text) {
        return text.getBytes(UTF_8);
    }

    public static boolean equals(int[] field1, int[] field2) {
        if (field1 == null || field1.length == 0) {
            return field2 == null || field2.length == 0;
        } else {
            return Arrays.equals(field1, field2);
        }
    }

    public static boolean equals(long[] field1, long[] field2) {
        if (field1 == null || field1.length == 0) {
            return field2 == null || field2.length == 0;
        } else {
            return Arrays.equals(field1, field2);
        }
    }

    public static boolean equals(float[] field1, float[] field2) {
        if (field1 == null || field1.length == 0) {
            return field2 == null || field2.length == 0;
        } else {
            return Arrays.equals(field1, field2);
        }
    }

    public static boolean equals(double[] field1, double[] field2) {
        if (field1 == null || field1.length == 0) {
            return field2 == null || field2.length == 0;
        } else {
            return Arrays.equals(field1, field2);
        }
    }

    public static boolean equals(boolean[] field1, boolean[] field2) {
        if (field1 == null || field1.length == 0) {
            return field2 == null || field2.length == 0;
        } else {
            return Arrays.equals(field1, field2);
        }
    }

    public static boolean equals(byte[][] field1, byte[][] field2) {
        int index1 = 0;
        int length1 = field1 == null ? 0 : field1.length;
        int index2 = 0;
        int length2 = field2 == null ? 0 : field2.length;
        while (true) {
            if (index1 >= length1 || field1[index1] != null) {
                boolean atEndOf1;
                boolean atEndOf2;
                while (index2 < length2 && field2[index2] == null) {
                    index2++;
                }
                if (index1 >= length1) {
                    atEndOf1 = true;
                } else {
                    atEndOf1 = false;
                }
                if (index2 >= length2) {
                    atEndOf2 = true;
                } else {
                    atEndOf2 = false;
                }
                if (atEndOf1 && atEndOf2) {
                    return true;
                }
                if (atEndOf1 != atEndOf2 || !Arrays.equals(field1[index1], field2[index2])) {
                    return false;
                }
                index1++;
                index2++;
            } else {
                index1++;
            }
        }
    }

    public static boolean equals(Object[] field1, Object[] field2) {
        int index1 = 0;
        int length1 = field1 == null ? 0 : field1.length;
        int index2 = 0;
        int length2 = field2 == null ? 0 : field2.length;
        while (true) {
            if (index1 >= length1 || field1[index1] != null) {
                boolean atEndOf1;
                boolean atEndOf2;
                while (index2 < length2 && field2[index2] == null) {
                    index2++;
                }
                if (index1 >= length1) {
                    atEndOf1 = true;
                } else {
                    atEndOf1 = false;
                }
                if (index2 >= length2) {
                    atEndOf2 = true;
                } else {
                    atEndOf2 = false;
                }
                if (atEndOf1 && atEndOf2) {
                    return true;
                }
                if (atEndOf1 != atEndOf2 || !field1[index1].equals(field2[index2])) {
                    return false;
                }
                index1++;
                index2++;
            } else {
                index1++;
            }
        }
    }

    public static int hashCode(int[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(long[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(float[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(double[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(boolean[] field) {
        return (field == null || field.length == 0) ? 0 : Arrays.hashCode(field);
    }

    public static int hashCode(byte[][] field) {
        int result = 0;
        int i = 0;
        int size = field == null ? 0 : field.length;
        while (i < size) {
            byte[] element = field[i];
            if (element != null) {
                result = (result * 31) + Arrays.hashCode(element);
            }
            i++;
        }
        return result;
    }

    public static int hashCode(Object[] field) {
        int result = 0;
        int i = 0;
        int size = field == null ? 0 : field.length;
        while (i < size) {
            Object element = field[i];
            if (element != null) {
                result = (result * 31) + element.hashCode();
            }
            i++;
        }
        return result;
    }

    public static void cloneUnknownFieldData(ExtendableMessageNano original, ExtendableMessageNano cloned) {
        if (original.unknownFieldData != null) {
            cloned.unknownFieldData = original.unknownFieldData.clone();
        }
    }
}
