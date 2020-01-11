package net.tiny.naming;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

public class TestEntity {
    private static AtomicLong sequenceNumber;

    static long getNewId() {
        synchronized (TestEntity.class) {
            if(sequenceNumber == null) {
                sequenceNumber = new AtomicLong(1L);
            }
        }
        return sequenceNumber.getAndIncrement();
    }


    private long storageId;
    private String test1;
    private String test2;
    private Date test3;
    private Sample sample;

    public TestEntity() {
        this.storageId = getNewId();
    }

    public TestEntity(String test1, String test2) {
        this.storageId = getNewId();
        this.test1 = test1;
        this.test2 = test2;
        this.test3 = new Date();
    }

    public long getStorageId() {
        return storageId;
    }

    public void setStorageId(long id) {
        this.storageId = id;
    }

    public String getTest1() {
        return test1;
    }

    public void setTest1(String test1) {
        this.test1 = test1;
    }

    public String getTest2() {
        return test2;
    }

    public void setTest2(String test2) {
        this.test2 = test2;
    }

    public Date getTest3() {
        return test3;
    }

    public void setTest3(Date test3) {
        this.test3 = test3;
    }

    public Sample getSample() {
        return sample;
    }

    @Override
    public String toString() {
        final SimpleDateFormat FORMAT = new SimpleDateFormat(
                "yyyy.MM.dd HH:mm:ss");
        return getClass().getSimpleName() + " [storageId=" + storageId
                + ", test1="
                + test1 + ", test2=" + test2 + ", test3=" + FORMAT.format(test3) + "]";
    }

}
